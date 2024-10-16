#include <OpenVR.h>
#include <Logger.hpp>
#include <openvr/openvr.h>
#include <glm/glm.hpp>
#include <glew/glew.h>
#include <sstream>

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

/*glm::mat4 projectionLeft, projectionRight;
glm::mat4 eyePosLeft, eyePosRight;*/

bool shouldResetOrigin;
float tmp[6];
vr::TrackedDevicePose_t hmdPose;
vr::HmdMatrix34_t originPose;

glm::mat4x3 leftEyeMatrix, rightEyeMatrix;

inline glm::mat3x4 GetHmdMatrix(vr::HmdMatrix34_t& m)
{
	return glm::mat3x4(
		m.m[0][0], m.m[0][1], m.m[0][2], 0.0f,
		m.m[1][0], m.m[1][1], m.m[1][2], 0.0f,
		m.m[2][0], m.m[2][1], m.m[2][2], 0.0f
	);
}

inline glm::mat4x3 GetEyeMatrix(vr::HmdMatrix34_t m)
{
	return glm::mat4x3(
		m.m[0][0], m.m[1][0], m.m[2][0],
		m.m[0][1], m.m[1][1], m.m[2][1],
		m.m[0][2], m.m[1][2], m.m[2][2],
		m.m[0][3], m.m[1][3], m.m[2][3]
	);
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
	glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, framebufferDesc.renderTextureId);
	glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA8, renderWidth, renderWidth, true);
	glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, framebufferDesc.renderTextureId, 0);

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

void GetFov()
{
}

JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_init(JNIEnv *env, jclass self)
{
	if (!vr::VR_IsHmdPresent()) return 1;
	if (vr::VR_Init(nullptr, vr::EVRApplicationType::VRApplication_Scene) == nullptr) return 2;

	vr::VRSystem()->GetRecommendedRenderTargetSize(&renderWidth, &renderHeight);
	env->SetStaticIntField(self, env->GetStaticFieldID(self, "renderWidth", "I"), renderWidth);
	env->SetStaticIntField(self, env->GetStaticFieldID(self, "renderHeight", "I"), renderHeight);

	vr::VRSystem()->GetProjectionRaw(vr::Eye_Left, &tmp[0], &tmp[1], &tmp[2], &tmp[3]);
	env->SetStaticFloatField(self, env->GetStaticFieldID(self, "fov", "F"), glm::degrees(glm::abs(atanf(tmp[0])) + glm::abs(atanf(tmp[1]))));

	leftEyeMatrix = GetEyeMatrix(vr::VRSystem()->GetEyeToHeadTransform(vr::Eye_Left));
	rightEyeMatrix = GetEyeMatrix(vr::VRSystem()->GetEyeToHeadTransform(vr::Eye_Right));

	/*projectionLeft = GetHMDMatrixProjectionEye(vr::Eye_Left);
	projectionRight = GetHMDMatrixProjectionEye(vr::Eye_Right);
	eyePosLeft = GetHMDMatrixPoseEye(vr::Eye_Left);
	eyePosRight = GetHMDMatrixPoseEye(vr::Eye_Right);*/

	shouldResetOrigin = true;

	return 0;
}

JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_initGL(JNIEnv* env, jclass self)
{
	if (glewInit() != GLEW_OK) return 1;

	if (!CreateFrameBuffer(leftEyeDesc)) return 2;
	if (!CreateFrameBuffer(rightEyeDesc)) return 2;

	return 0;
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_shutdown(JNIEnv *env, jclass self)
{
	vr::VR_Shutdown();

	bhe::Logger::Shutdown();
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

    //glBlitFramebuffer(0, 0, renderWidth, renderHeight, 0, 0, renderWidth, renderHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);
    glBlitFramebuffer(0, 0, 1920, 1080, 0, 512, 0 + 1920, 512 + 1080, GL_COLOR_BUFFER_BIT, GL_LINEAR);

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_postRenderRight(JNIEnv *env, jclass self)
{
 	glBindFramebuffer(GL_FRAMEBUFFER, 0);
	
 	glBindFramebuffer(GL_READ_FRAMEBUFFER, rightEyeDesc.renderFramebufferId);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, rightEyeDesc.resolveFramebufferId);

    //glBlitFramebuffer(0, 0, renderWidth, renderHeight, 0, 0, renderWidth, renderHeight, GL_COLOR_BUFFER_BIT, GL_LINEAR);
    glBlitFramebuffer(0, 0, 1920, 1080, 0, 512, 0 + 1920, 512 + 1080, GL_COLOR_BUFFER_BIT, GL_LINEAR);

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_submitRender(JNIEnv *env, jclass self)
{
	vr::Texture_t leftEyeTexture = { (void*)(uintptr_t)leftEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Left, &leftEyeTexture);
	//vr::VRCompositor()->Submit(vr::Eye_Right, &leftEyeTexture);
	vr::Texture_t rightEyeTexture = { (void*)(uintptr_t)rightEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Right, &rightEyeTexture);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_getHmdLocation(JNIEnv *env, jclass self, jfloatArray hmdObject, jfloatArray leftObject, jfloatArray rightObject)
{
	vr::VRCompositor()->WaitGetPoses(&hmdPose, 1, nullptr, 0);
	
	if (!hmdPose.bPoseIsValid) return;
	
	vr::HmdMatrix34_t hmd = hmdPose.mDeviceToAbsoluteTracking;

	if (shouldResetOrigin)
	{
		originPose = hmd;
		shouldResetOrigin = false;
	}

	tmp[0] = originPose.m[0][3] - hmd.m[0][3];
	tmp[1] = hmd.m[1][3] - originPose.m[1][3];
	tmp[2] = originPose.m[2][3] - hmd.m[2][3];
	tmp[3] = glm::degrees(atan2(hmd.m[2][1] , hmd.m[2][2]));
	tmp[4] = -glm::degrees(atan2(-hmd.m[2][0], glm::sqrt(hmd.m[0][0] * hmd.m[0][0] + hmd.m[1][0] * hmd.m[1][0])));
	tmp[5] = -glm::degrees(atan2(hmd.m[1][0] , hmd.m[0][0]));
	env->SetFloatArrayRegion(hmdObject, 0, 6, tmp);

	glm::mat3x4 hmdRotation = GetHmdMatrix(hmd);

	glm::mat3x4 m = hmdRotation * leftEyeMatrix;
	tmp[0] = m[0][3];
	tmp[1] = m[1][3];
	tmp[2] = m[2][3];
	tmp[3] = glm::degrees(atan2(m[2][1], m[2][2]));
	tmp[4] = -glm::degrees(atan2(-m[2][0], glm::sqrt(m[0][0] * m[0][0] + m[1][0] * m[1][0])));
	tmp[5] = -glm::degrees(atan2(m[1][0], m[0][0]));
	env->SetFloatArrayRegion(leftObject, 0, 6, tmp);

	m = hmdRotation * rightEyeMatrix;
	tmp[0] = m[0][3];
	tmp[1] = m[1][3];
	tmp[2] = m[2][3];
	tmp[3] = glm::degrees(atan2(m[2][1], m[2][2]));
	tmp[4] = -glm::degrees(atan2(-m[2][0], glm::sqrt(m[0][0] * m[0][0] + m[1][0] * m[1][0])));
	tmp[5] = -glm::degrees(atan2(m[1][0], m[0][0]));
	env->SetFloatArrayRegion(rightObject, 0, 6, tmp);
}

JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_resetHmdLocation(JNIEnv*, jclass)
{
	shouldResetOrigin = true;
}

/*void test()
{
	// Init

	// Send textures to VR head
	vr::Texture_t leftEyeTexture = {(void*)(uintptr_t)leftEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Left, &leftEyeTexture );
	vr::Texture_t rightEyeTexture = {(void*)(uintptr_t)rightEyeDesc.resolveTextureId, vr::TextureType_OpenGL, vr::ColorSpace_Gamma };
	vr::VRCompositor()->Submit(vr::Eye_Right, &rightEyeTexture );
}

void TEST_RENDER_WINDOW()
{
	glDisable(GL_DEPTH_TEST);
	glViewport( 0, 0, m_nCompanionWindowWidth, m_nCompanionWindowHeight );

	glBindVertexArray( m_unCompanionWindowVAO );
	glUseProgram( m_unCompanionWindowProgramID );

	// render left eye (first half of index array )
	glBindTexture(GL_TEXTURE_2D, leftEyeDesc.m_nResolveTextureId );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
	glDrawElements( GL_TRIANGLES, m_uiCompanionWindowIndexSize/2, GL_UNSIGNED_SHORT, 0 );

	// render right eye (second half of index array )
	glBindTexture(GL_TEXTURE_2D, rightEyeDesc.m_nResolveTextureId  );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
	glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
	glDrawElements( GL_TRIANGLES, m_uiCompanionWindowIndexSize/2, GL_UNSIGNED_SHORT, (const void *)(uintptr_t)(m_uiCompanionWindowIndexSize) );

	glBindVertexArray( 0 );
	glUseProgram( 0 );
}

void TEST_RENDER_TARGET()
{
	glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
	glEnable( GL_MULTISAMPLE );

	// Left Eye
	glBindFramebuffer( GL_FRAMEBUFFER, leftEyeDesc.m_nRenderFramebufferId );
 	glViewport(0, 0, m_nRenderWidth, m_nRenderHeight );
 	RenderScene( vr::Eye_Left );
 	glBindFramebuffer( GL_FRAMEBUFFER, 0 );
	
	glDisable( GL_MULTISAMPLE );
	 	
 	glBindFramebuffer(GL_READ_FRAMEBUFFER, leftEyeDesc.m_nRenderFramebufferId);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, leftEyeDesc.m_nResolveFramebufferId );

    glBlitFramebuffer( 0, 0, m_nRenderWidth, m_nRenderHeight, 0, 0, m_nRenderWidth, m_nRenderHeight, 
		GL_COLOR_BUFFER_BIT,
 		GL_LINEAR );

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0 );	

	glEnable( GL_MULTISAMPLE );

	// Right Eye
	glBindFramebuffer( GL_FRAMEBUFFER, rightEyeDesc.m_nRenderFramebufferId );
 	glViewport(0, 0, m_nRenderWidth, m_nRenderHeight );
 	RenderScene( vr::Eye_Right );
 	glBindFramebuffer( GL_FRAMEBUFFER, 0 );
 	
	glDisable( GL_MULTISAMPLE );

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, rightEyeDesc.m_nRenderFramebufferId );
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, rightEyeDesc.m_nResolveFramebufferId );
	
    glBlitFramebuffer( 0, 0, m_nRenderWidth, m_nRenderHeight, 0, 0, m_nRenderWidth, m_nRenderHeight, 
		GL_COLOR_BUFFER_BIT,
 		GL_LINEAR  );

 	glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0 );
}

Matrix4 CMainApplication::GetCurrentViewProjectionMatrix( vr::Hmd_Eye nEye )
{
	Matrix4 matMVP;
	if( nEye == vr::Eye_Left )
	{
		matMVP = m_mat4ProjectionLeft * m_mat4eyePosLeft * m_mat4HMDPose;
	}
	else if( nEye == vr::Eye_Right )
	{
		matMVP = m_mat4ProjectionRight * m_mat4eyePosRight *  m_mat4HMDPose;
	}

	return matMVP;
}*/