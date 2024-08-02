// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   Camera3D.java

package com.maddox.il2.engine;

import com.maddox.JGP.Point3d;
import com.maddox.JGP.Point3f;
import com.maddox.JGP.Tuple3f;
import com.maddox.il2.ai.EventLog;
import com.maddox.il2.game.OpenVR;

// Referenced classes of package com.maddox.il2.engine:
//            Camera, Render, ActorPos, Orient

public class Camera3D extends Camera
{
    public float FOV()
    {
        return FOV;
    }

    public float aspect()
    {
        return aspect;
    }

    protected void setViewPortWidth(int i)
    {
        viewPortWidth = i;
    }

    public void set(float f, float f1, float f2)
    {
        FOV = f;
        ZNear = f1;
        ZFar = f2;
    }

    public void set(float f, float f1, float f2, float f3)
    {
        FOV = f;
        ZNear = f1;
        ZFar = f2;
        aspect = f3;
    }

    public void set(float f)
    {
        FOV = f;
    }

    public void set(float f, float f1)
    {
        FOV = f;
        aspect = f1;
    }

    public boolean activate(float f, int i, int j, int k, int l, int i1, int j1, 
            int k1, int l1, int i2, int j2)
    {
        if(FOV <= 0.0F)
        {
            return false;
        } else
        {
            Camera.SetZOrder(Render.current().getZOrder());
            Camera.SetViewportCrop(f, i, j, k, l, i1, j1, k1, l1, i2, j2);
            Camera.SetFOV(FOV, ZNear, ZFar);
            pos.getRender(Camera.tmpP, Camera.tmpO);
            
            tmpR.set(OpenVR.currentEyeLocation.z, -OpenVR.currentEyeLocation.x, OpenVR.currentEyeLocation.y);
            Camera.tmpO.transform(tmpR);

            Camera.tmpd[0] = Camera.tmpP.x + tmpR.x;
            Camera.tmpd[1] = Camera.tmpP.y + tmpR.y;
            Camera.tmpd[2] = Camera.tmpP.z + tmpR.z;
            Camera.tmpd[3] = -Camera.tmpO.azimut();
            Camera.tmpd[4] = Camera.tmpO.tangage();
            Camera.tmpd[5] = -Camera.tmpO.kren();
            
            Camera.SetCameraPos(Camera.tmpd);
            Camera.GetVirtOrigin(Camera.tmpOr);
            XOffset = Camera.tmpOr[0];
            YOffset = Camera.tmpOr[1];
            return true;
        }
    }

    public Camera3D()
    {
        FOV = 90F;
        aspect = 1.333333F;
        viewPortWidth = 640;
    }

    private float FOV;
    private float aspect;
    private int viewPortWidth;
    private Point3f tmpR;
}
