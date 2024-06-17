/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_maddox_il2_game_OpenVR */

#ifndef _Included_com_maddox_il2_game_OpenVR
#define _Included_com_maddox_il2_game_OpenVR
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    init
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_init
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    shutdown
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_shutdown
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    initGL
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_maddox_il2_game_OpenVR_initGL
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    shutdownGL
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_shutdownGL
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    preRenderLeft
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_preRenderLeft
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    preRenderRight
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_preRenderRight
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    postRenderLeft
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_postRenderLeft
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    postRenderRight
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_postRenderRight
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    submitRender
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_submitRender
  (JNIEnv *, jclass);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    getHmdLocation
 * Signature: ([F[F[F)V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_getHmdLocation
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray);

/*
 * Class:     com_maddox_il2_game_OpenVR
 * Method:    resetHmdLocation
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_maddox_il2_game_OpenVR_resetHmdLocation
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
