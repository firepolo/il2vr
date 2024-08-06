// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   GLContext.java

package com.maddox.opengl;

import com.maddox.il2.game.OpenVR;
import com.maddox.rts.*;

// Referenced classes of package com.maddox.opengl:
//            MsgGLContext, GLContextException, Provider, GLCaps, 
//            gl, GLInitCaps

public class GLContext
    implements MsgAddListenerListener, MsgRemoveListenerListener, MsgMainWindowListener
{

    public static boolean isValid(GLContext glcontext)
    {
        if(glcontext != null)
            return glcontext.isCreated();
        else
            return false;
    }

    public GLContext(GLInitCaps glinitcaps)
    {
        bEnableMessages = true;
        listeners = new Listeners();
        msg = new MsgGLContext();
        caps = glinitcaps;
        bCreated = false;
        width = 0;
        height = 0;
    }

    public static Object lockObject()
    {
        return Provider.lockObject();
    }

    public static boolean makeCurrent(GLContext glcontext)
    {
        if(glcontext != null)
        {
            if(glcontext != current)
                if(glcontext.isCreated() && glcontext.MakeCurrent())
                {
                    current = glcontext;
                } else
                {
                    if(current != null)
                    {
                        MakeNotCurrent();
                        current = null;
                    }
                    return false;
                }
        } else
        if(current != null)
        {
            MakeNotCurrent();
            current = null;
        }
        return true;
    }

    public static GLContext getCurrent()
    {
        return current;
    }

    public GLCaps getCaps()
    {
        return caps;
    }

    public boolean isCreated()
    {
        return bCreated;
    }

    public boolean isMain()
    {
        return bMain;
    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    public int hWnd()
    {
        return iWND;
    }

    public void createWin32(int i, boolean flag, int j, int k)
        throws GLContextException
    {
        synchronized(lockObject())
        {
            makeCurrent(null);
            if(isCreated())
                destroy();
            width = j;
            height = k;
            int ai[] = caps.getCaps();
            CreateWin32(i, ai);
            Provider.contextCreated();
            caps.setCaps(ai);
            bCreated = true;
            bDoubleBuffer = caps.isDoubleBuffered();
            if(flag)
                RTSConf.cur.mainWindow.msgAddListener(this, null);
            makeCurrent(this);
            int ai1[] = new int[1];
            ai1[0] = 0;
            gl.GetIntegerv(3379, ai1);
            if(ai1[0] <= 256)
                caps.stencilBits = 0;
            sendAction(1);
            
            if (OpenVR.initGL() != 0) throw new GLContextException("Failed to init GL OpenVR");
        }
    }

    public void changeWin32(GLInitCaps glinitcaps, int i, boolean flag, int j, int k)
        throws GLContextException
    {
        if(bCreated)
            destroy();
        caps = glinitcaps;
        createWin32(i, flag, j, k);
    }

    public void destroy()
    {
        synchronized(lockObject())
        {
        	OpenVR.shutdownGL();
        	
            makeCurrent(null);
            if(!isCreated())
                return;
            Destroy();
            Provider.contextDestroyed();
            bCreated = false;
            current = null;
            RTSConf.cur.mainWindow.msgRemoveListener(this, null);
            sendAction(2);
        }
    }

    public boolean swapBuffers()
    {
        if(isCreated() && getCurrent() == this)
        {
            if(bDoubleBuffer)
            {
                return SwapBuffers();
            } else
            {
                gl.Flush();
                return true;
            }
        } else
        {
            return false;
        }
    }

    public void setSize(int i, int j)
    {
        width = i;
        height = j;
        sendAction(4);
    }

    public void msgMainWindow(int i)
    {
        switch(i)
        {
        case 2: // '\002'
            destroy();
            break;

        case 4: // '\004'
            setSize(RTSConf.cur.mainWindow.width(), RTSConf.cur.mainWindow.height());
            break;
        }
    }

    public Object[] getListeners()
    {
        return listeners.get();
    }

    public void msgAddListener(Object obj, Object obj1)
    {
        listeners.addListener(obj);
    }

    public void msgRemoveListener(Object obj, Object obj1)
    {
        listeners.removeListener(obj);
    }

    public boolean isMessagesEnable()
    {
        return bEnableMessages;
    }

    public void setMessagesEnable(boolean flag)
    {
        bEnableMessages = flag;
    }

    public void sendAction(int i)
    {
        if(!bEnableMessages)
            return;
        Object aobj[] = listeners.get();
        if(aobj == null)
            return;
        for(int j = 0; j < aobj.length; j++)
            msg.Send(i, aobj[j]);

    }

    private GLContext()
    {
        bEnableMessages = true;
        listeners = new Listeners();
        msg = new MsgGLContext();
    }

    private native void CreateWin32(int i, int ai[])
        throws GLContextException;

    private native void Change(int ai[])
        throws GLContextException;

    private native void Destroy();

    private native boolean SwapBuffers();

    private native boolean MakeCurrent();

    private static native void MakeNotCurrent();

    private static GLContext current = null;
    private GLCaps caps;
    private boolean bEnableMessages;
    private boolean bCreated;
    private boolean bDoubleBuffer;
    private int width;
    private int height;
    private int iWND;
    private int iDC;
    private int iRC;
    private Listeners listeners;
    private MsgGLContext msg;
    private boolean bMain;

    static 
    {
        gl.loadNative();
    }
}
