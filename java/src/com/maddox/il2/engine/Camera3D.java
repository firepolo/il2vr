// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   Camera3D.java

package com.maddox.il2.engine;

import com.maddox.JGP.Point3d;
import com.maddox.JGP.Point3f;
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
            
            /*final float a = (float)Math.toRadians(-Camera.tmpO.azimut());
            final float t = (float)Math.toRadians(Camera.tmpO.tangage());
            final float r = (float)Math.toRadians(-Camera.tmpO.kren());*/
            final float a = (float)Math.toRadians(Camera.tmpO.Yaw);
            final float t = (float)Math.toRadians(Camera.tmpO.Pitch);
            final float r = (float)Math.toRadians(Camera.tmpO.Roll);
            
            final float cy = (float)Math.cos(r);
            final float sy = (float)Math.sin(r);
            final float cb = (float)Math.cos(a);
            final float sb = (float)Math.sin(a);
            final float ca = (float)Math.cos(t);
            final float sa = (float)Math.sin(t);

            final float x = OpenVR.currentEyeLocation.x * (cb*cy) + OpenVR.currentEyeLocation.y * (sa*sb*cy-ca*sy) + OpenVR.currentEyeLocation.z * (ca*sb*cy+sa*sy);
            final float y = OpenVR.currentEyeLocation.x * (cb*sy) + OpenVR.currentEyeLocation.y * (sa*sb*sy+ca*cy) + OpenVR.currentEyeLocation.z * (ca*sb*sy-sa*cy);
            final float z = OpenVR.currentEyeLocation.x * (-sb) + OpenVR.currentEyeLocation.y * (sa*cb) + OpenVR.currentEyeLocation.z * (ca*cb);
            
            Point3d p = new Point3d(pos.getCurrentPoint());
            p.sub(pos.getPrev().P);
            if (OpenVR.currentEyeLocation.x > 0.001f) EventLog.type("fix("+new Point3f(x, y, z).toString()+"), prev("+p.toString()+")");

            Camera.tmpd[0] = Camera.tmpP.x + x;
            Camera.tmpd[1] = Camera.tmpP.y + y;
            Camera.tmpd[2] = Camera.tmpP.z + z;
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
}
