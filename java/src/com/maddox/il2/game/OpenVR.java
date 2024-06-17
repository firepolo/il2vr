package com.maddox.il2.game;

public class OpenVR
{
	/*
	0 = x;
	1 = y;
	2 = z;
	3 = pitch;
    4 = yaw;
	5 = roll;
	*/
	
	public static float[] hmdLocation = new float[6];
	public static float[] leftEyeLocation = new float[6];
	public static float[] rightEyeLocation = new float[6];
	public static float[] currentEyeLocation = new float[6];
	
	public static float fov;
	
	public static int renderWidth;
	public static int renderHeight;
	
	public static native int init();
	public static native void shutdown();
	
	public static native int initGL();
	public static native void shutdownGL();
	
	public static native int preRenderLeft();
	public static native int preRenderRight();
	
	public static native void postRenderLeft();
	public static native void postRenderRight();
	
	public static native int submitRender();
	
	public static native void getHmdLocation(float[] location, float[] leftLocation, float[] rightLocation);
	public static native void resetHmdLocation();

	static
	{
		System.loadLibrary("il2vr");
	}
}
