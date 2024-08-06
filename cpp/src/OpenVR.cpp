#include <OpenVR.h>
#include <openvr/openvr.h>
#include <glm/glm.hpp>
#include <glm/gtc/quaternion.hpp>
#include <glew/glew.h>
#include <fstream>
#include <iomanip>

struct FramebufferDesc
{
	GLuint depthBufferId;
	GLuint renderTextureId;
	GLuint renderFramebufferId;
	GLuint resolveTextureId;
	GLuint resolveFramebufferId;
};

FramebufferDesc leftEyeDesc;
FramebufferDesc rightEyeDesc;
GLuint framebuffer, renderbuffer;
uint32_t renderWidth, renderHeight;
uint32_t adaptedWidth, adaptedHeight;

bool shouldResetOrigin;
float tmp[6];
vr::TrackedDevicePose_t hmdPose;
vr::HmdMatrix34_t originPose;

inline glm::vec3 GetEyeVector(vr::HmdMatrix34_t m)
{
	return glm::vec3(m.m[0][3], m.m[1][3], m.m[2][3]);
}

glm::mat4 GetHMDMatrixProjectionEye(vr::Hmd_Eye eye)
{
	vr::HmdMatrix44_t m = vr::VRSystem()->GetProjectionMatrix(eye, 0.1f, 30.0f);
	return glm::mat4(
		m.m[0][0], m.m[1][0], m.m[2][0], m.m[3][0],
		m.m[0][1], m.m[1][1], m.m[2][1], m.m[3][1], 
		m.m[0][2], m.m[1][2], m.m[2][2], m.m[3][2], 
		m.m[0][3], m.m[1][3], m.m[2][3], m.m[3][3]
	);
}

glm::mat4 GetHMDMatrixPoseEye(vr::Hmd_Eye eye)
{
	vr::HmdMatrix34_t m = vr::VRSystem()->GetEyeToHeadTransform(eye);
	return glm::inverse(glm::mat4(
		m.m[0][0], m.m[1][0], m.m[2][0], 0.0, 
		m.m[0][1], m.m[1][1], m.m[2][1], 0.0,
		m.m[0][2], m.m[1][2], m.m[2][2], 0.0,
		m.m[0][3], m.m[1][3], m.m[2][3], 1.0f
	));
}

bool CreateFrameBuffer(FramebufferDesc &framebufferDesc)
{
	glGenFramebuffers(1, &framebufferDesc.renderFramebufferId);
	glBindFramebuffer(GL_FRAMEBUFFER, framebufferDesc.renderFramebufferId);

	glGenRenderbuffers(1, &framebufferDesc.depthBufferId);
	glBindRenderbuffer(GL_RENDERBUFFER, framebufferDesc.depthBufferId);
	glRenderbufferStorageMultisample(GL_RENDERBUFFER, 4, GL_DEPTH_COMPONENT, renderWidth, renderHeight);
	glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,	framebufferDesc.depthBufferId);

	glGenTextures(1, &framebufferDesc.renderTextureId);
	//glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, framebufferDesc.renderTextureId);
	//glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA8, renderWidth, renderHeight, true);
	//glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, framebufferDesc.renderTextureId, 0);
	glBindTexture(GL_TEXTURE_2D, framebufferDesc.renderTextureId);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, renderWidth, renderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
	glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferDesc.renderTextureId, 0);

	glGenFramebuffers(1, &framebufferDesc.resolveFramebufferId);
	glBindFramebuffer(GL_FRAMEBUFFER, framebufferDesc.resolveFramebufferId);

	glGenTextures(1, &framebufferDesc.resolveTextureId);
	glBindTexture(GL_TEXTURE_2D, framebufferDesc.resolveTextureId);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, renderWidth, renderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
	glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferDesc.resolveTextureId, 0);

	GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
	if (status != GL_FRAMEBUFFER_COMPLETE) return false;

	glBindFramebuffer(GL_FRAMEBUFFER, 0);

	return true;
}

void DeleteFrameBuffer(FramebufferDesc& framebufferDesc)
{
	glDeleteTextures(1, &framebufferDesc.resolveTextureId);
	glDeleteTextures(1, &framebufferDesc.renderTextureId);

	glDeleteRenderbuffers(1, &framebufferDesc.depthBufferId);

	glDeleteFramebuffers(1, &framebufferDesc.resolveFramebufferId);
	glDeleteFramebuffers(1, &framebufferDesc.renderFramebufferId);
}

JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_init(JNIEnv *env, jclass self, jfloat factor)
{
	if (!vr::VR_IsHmdPresent()) return 1;
	if (vr::VR_Init(nullptr, vr::EVRApplicationType::VRApplication_Scene) == nullptr) return 2;

	vr::VRSystem()->GetRecommendedRenderTargetSize(&renderWidth, &renderHeight);
	env->SetStaticIntField(self, env->GetStaticFieldID(self, "renderWidth", "I"), renderWidth);
	env->SetStaticIntField(self, env->GetStaticFieldID(self, "renderHeight", "I"), renderHeight);

	adaptedWidth = static_cast<uint32_t>(renderWidth * factor);
	adaptedHeight = static_cast<uint32_t>(renderHeight * factor);

	vr::VRSystem()->GetProjectionRaw(vr::Eye_Left, &tmp[0], &tmp[1], &tmp[2], &tmp[3]);
	env->SetStaticFloatField(self, env->GetStaticFieldID(self, "fov", "F"), glm::degrees(glm::abs(atanf(tmp[0])) + glm::abs(atanf(tmp[1]))));

	glm::vec3 leftEyeLocation = GetEyeVector(vr::VRSystem()->GetEyeToHeadTransform(vr::Eye_Left));
	env->SetFloatArrayRegion(reinterpret_cast<jfloatArray>(env->GetStaticObjectField(self, env->GetStaticFieldID(self, "leftEyeLocation", "[F"))), 0, 3, &leftEyeLocation.x);

	glm::vec3 rightEyeLocation = GetEyeVector(vr::VRSystem()->GetEyeToHeadTransform(vr::Eye_Right));
	env->SetFloatArrayRegion(reinterpret_cast<jfloatArray>(env->GetStaticObjectField(self, env->GetStaticFieldID(self, "rightEyeLocation", "[F"))), 0, 3, &rightEyeLocation.x);

	shouldResetOrigin = true;

	return 0;
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_shutdown(JNIEnv* env, jclass self)
{
	vr::VR_Shutdown();
}

JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_initGL(JNIEnv* env, jclass self)
{
	if (glewInit() != GLEW_OK) return 1;

	if (!CreateFrameBuffer(leftEyeDesc)) return 2;
	if (!CreateFrameBuffer(rightEyeDesc)) return 2;

	return 0;
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_shutdownGL(JNIEnv* env, jclass self)
{
	DeleteFrameBuffer(leftEyeDesc);
	DeleteFrameBuffer(rightEyeDesc);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_preRenderLeft(JNIEnv *env, jclass self)
{
	glBindFramebuffer(GL_FRAMEBUFFER, leftEyeDesc.renderFramebufferId);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_preRenderRight(JNIEnv *env, jclass self)
{
	glBindFramebuffer(GL_FRAMEBUFFER, rightEyeDesc.renderFramebufferId);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_postRenderLeft(JNIEnv *env, jclass self)
{
 	glBindFramebuffer(GL_FRAMEBUFFER, 0);
	
 	glBindFramebuffer(GL_READ_FRAMEBUFFER, leftEyeDesc.renderFramebufferId);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, leftEyeDesc.resolveFramebufferId);

	glBlitFramebuffer(0, 0, adaptedWidth, adaptedHeight, 0, 0, renderWidth, renderHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_postRenderRight(JNIEnv *env, jclass self)
{
 	glBindFramebuffer(GL_FRAMEBUFFER, 0);
	
 	glBindFramebuffer(GL_READ_FRAMEBUFFER, rightEyeDesc.renderFramebufferId);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, rightEyeDesc.resolveFramebufferId);

	glBlitFramebuffer(0, 0, adaptedWidth, adaptedHeight, 0, 0, renderWidth, renderHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_submitRender(JNIEnv *env, jclass self)
{
	vr::Texture_t leftEyeTexture = { (void*)(uintptr_t)leftEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Left, &leftEyeTexture);
	vr::Texture_t rightEyeTexture = { (void*)(uintptr_t)rightEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Right, &rightEyeTexture);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_getHmdLocation(JNIEnv *env, jclass self, jfloatArray hmdObject)
{
	vr::VRCompositor()->WaitGetPoses(&hmdPose, 1, nullptr, 0);
	
	if (!hmdPose.bPoseIsValid) return;
	
	vr::HmdMatrix34_t hmd = hmdPose.mDeviceToAbsoluteTracking;

	if (shouldResetOrigin)
	{
		originPose = hmd;
		shouldResetOrigin = false;
	}

	/*tmp[0] = originPose.m[0][3] - hmd.m[0][3];
	tmp[1] = hmd.m[1][3] - originPose.m[1][3];
	tmp[2] = originPose.m[2][3] - hmd.m[2][3];
	tmp[3] = glm::degrees(atan2(hmd.m[2][1] , hmd.m[2][2]));
	tmp[4] = -glm::degrees(atan2(-hmd.m[2][0], glm::sqrt(hmd.m[0][0] * hmd.m[0][0] + hmd.m[1][0] * hmd.m[1][0])));
	tmp[5] = glm::degrees(atan2(hmd.m[1][0] , hmd.m[0][0]));*/

	/*float f3 = -hmd.m[2][0];
	float f2 = sqrtf(1.0f - f3 * f3);
	float f;
	float f1;
	float f4;
	float f5;
	if (f2 > 0.001F)
	{
		f4 = hmd.m[2][2];
		f5 = hmd.m[2][1];
		f = hmd.m[0][0];
		f1 = hmd.m[1][0];
	}
	else
	{
		f2 = 0.0f;
		f = 1.0f;
		f1 = 0.0f;
		f4 = hmd.m[1][1];
		f5 = -hmd.m[1][2];
	}

	tmp[0] = originPose.m[0][3] - hmd.m[0][3];
	tmp[1] = hmd.m[1][3] - originPose.m[1][3];
	tmp[2] = originPose.m[2][3] - hmd.m[2][3];
	tmp[3] = glm::degrees(atan2(f1, f));
	tmp[4] = glm::degrees(atan2(f3, f2));
	tmp[5] = glm::degrees(atan2(f5, f4));*/

	glm::vec3 angles = glm::degrees(glm::eulerAngles(glm::quat_cast(glm::mat3(
		hmd.m[0][0], hmd.m[0][1], hmd.m[0][2],
		hmd.m[1][0], hmd.m[1][1], hmd.m[1][2],
		hmd.m[2][0], hmd.m[2][1], hmd.m[2][2]
	))));

	tmp[0] = originPose.m[0][3] - hmd.m[0][3];
	tmp[1] = hmd.m[1][3] - originPose.m[1][3];
	tmp[2] = originPose.m[2][3] - hmd.m[2][3];
	tmp[3] = angles.x;
	tmp[4] = angles.y;
	tmp[5] = angles.z;

	env->SetFloatArrayRegion(hmdObject, 0, 6, tmp);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_resetHmdLocation(JNIEnv*, jclass)
{
	shouldResetOrigin = true;
}