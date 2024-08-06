// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   MainWin3D.java

package com.maddox.il2.game;

import com.maddox.il2.ai.EventLog;
import com.maddox.il2.ai.World;
import com.maddox.il2.engine.*;
import com.maddox.il2.objects.effects.ForceFeedback;
import com.maddox.opengl.GLContext;
import com.maddox.rts.*;

// Referenced classes of package com.maddox.il2.game:
//            Main3D, Main

public class MainWin3D extends Main3D
{
    private class Background extends BackgroundLoop
    {

        protected void step()
        {
            checkFocus();
            RTSConf.cur.loopMsgs();
            try
            {
                Thread.sleep(1L);
            }
            catch(Exception exception) { }
            RTSConf.cur.loopMsgs();
        }

        public Background()
        {
            setThisAsCurrent();
        }
    }

    class ConsoleExec extends com.maddox.rts.Console.Exec
    {

        public void doExec(String s)
        {
            RTSConf.cur.console.getEnv().exec(s);
            if(consoleServer != null)
                consoleServer.typeNum();
        }

        public String getPrompt()
        {
            return RTSConf.cur.console._getPrompt();
        }

        ConsoleExec()
        {
        }
    }


    public MainWin3D()
    {
        bChangedScreenMode = false;
        bTryChangedScreenMode = false;
        bChangeTimerPause = false;
    }

    public static MainWin3D curWin3D()
    {
        return (MainWin3D)Main.cur();
    }

    private void checkFocus()
    {
        if(bDrawIfNotFocused || ((MainWin32)RTSConf.cur.mainWindow).IsFocused())
        {
            if(!RendersMain.isShow())
            {
                if(Time.isEnableChangePause() && bChangeTimerPause)
                {
                    Time.setPause(false);
                    bChangeTimerPause = false;
                }
                if(bChangedScreenMode)
                {
                    ((MainWin32)MainWindow.adapter()).loopMsgs();
                    MainWindow.adapter().setMessagesEnable(false);
                    ScreenMode.set(saveMode);
                    if(MainWindow.adapter().isIconic())
                        MainWindow.adapter().showNormal();
                    ((MainWin32)MainWindow.adapter()).loopMsgs();
                    MainWindow.adapter().setFocus();
                    ((MainWin32)MainWindow.adapter()).loopMsgs();
                    ScreenMode screenmode = ScreenMode.readCurrent();
                    boolean flag = saveMode.width() != screenmode.width() || saveMode.height() != screenmode.height();
                    if(flag)
                        MainWindow.adapter().setMessagesEnable(true);
                    MainWindow.adapter().setPosSize(0, 0, screenmode.width(), screenmode.height());
                    ((MainWin32)MainWindow.adapter()).loopMsgs();
                    if(!flag)
                        MainWindow.adapter().setMessagesEnable(true);
                    RTSConf.cur.setUseMouse(saveMouseMode);
                    bChangedScreenMode = false;
                }
                RendersMain.setShow(true);
                RendersMain.bSwapBuffersResult = true;
                bTryChangedScreenMode = false;
            } else
            if(!RendersMain.bSwapBuffersResult && !RTSConf.isRequestExitApp() && Config.cur.windowChangeScreenRes && Config.cur.windowFullScreen)
                if(bTryChangedScreenMode)
                {
                    Main.doGameExit();
                } else
                {
                    bTryChangedScreenMode = true;
                    CmdEnv.top().exec("window " + Config.cur.windowWidth + " " + Config.cur.windowHeight + " " + Config.cur.windowColourBits + " " + Config.cur.windowDepthBits + " " + Config.cur.windowStencilBits + " PROVIDER " + Config.cur.glLib + " FULL");
                    RendersMain.bSwapBuffersResult = true;
                }
        } else
        if(RendersMain.isShow())
        {
            if(Time.isEnableChangePause() && !Time.isPaused())
            {
                Time.setPause(true);
                bChangeTimerPause = true;
            }
            RendersMain.setShow(false);
            if(!bChangedScreenMode && Config.cur.windowChangeScreenRes)
            {
                saveMouseMode = RTSConf.cur.getUseMouse();
                RTSConf.cur.setUseMouse(0);
                saveMode = ScreenMode.readCurrent();
                if(!MainWindow.adapter().isIconic())
                    MainWindow.adapter().showIconic();
                ScreenMode.restore();
                bChangedScreenMode = true;
            }
            RendersMain.bSwapBuffersResult = true;
            bTryChangedScreenMode = false;
        }
    }

    public void loopApp()
    {
        if(bUseStartLog)
            ConsoleGL0.exclusiveDraw(false);
        while(!RTSConf.isRequestExitApp()) 
            synchronized(RTSConf.lockObject())
            {
                if(BackgroundTask.isExecuted())
                {
                    BackgroundTask.doRun();
                } else
                {
                    checkFocus();
                    boolean flag;
                    String s;
                    synchronized(oCommandSync)
                    {
                        flag = bCommand;
                        s = sCommand;
                        bCommand = false;
                    }
                    if(flag)
                    {
                        if(consoleServer != null)
                            consoleServer.bEnableType = false;
                        System.out.println(RTSConf.cur.console._getPrompt() + s);
                        if(consoleServer != null)
                            consoleServer.bEnableType = true;
                        RTSConf.cur.console.getEnv().exec(s);
                        if(consoleServer != null)
                            consoleServer.typeNum();
                    }
                    RTSConf.cur.loopMsgs();
                }
            }
    }

    public void endApp()
    {
        if(Config.cur != null)
        {
            viewSet_Save();
            Config.cur.save();
            World.cur().save();
        }
        if(Config.cur != null)
            Config.cur.endSound();
        
        ForceFeedback.stop();
        GLContext glcontext = RendersMain.glContext();
        if(GLContext.isValid(glcontext))
            glcontext.destroy();
        if(ScreenMode.current() != ScreenMode.startup())
            ScreenMode.restore();
        if(RTSConf.cur != null)
        {
            RTSConf.cur.stop();
            if(RTSConf.cur.mainWindow.isCreated() && (RTSConf.cur instanceof RTSConfWin))
                ((MainWin32)RTSConf.cur.mainWindow).destroy();
        }
        EventLog.close();
        
        OpenVR.shutdown();
    }

    public boolean beginApp(String s, String s1, int i)
    {
    	IniFile inifile = new IniFile(s);
    	float factor = inifile.get("VR", "factor", 0.5f);
    	
    	if (OpenVR.init(factor) != 0) return false;
    	Main3D.FOVX = OpenVR.fov;
    	
        RTSConf.cur = new RTSConfWin(inifile, "rts", i);
        RTSConf.cur.console.exec = new ConsoleExec();
        Config.cur = new Config(inifile, true);
        Config.cur.windowWidth = (int)(OpenVR.renderWidth * factor);
        Config.cur.windowHeight = (int)(OpenVR.renderHeight * factor);
        Config.cur.windowFullScreen = false;
        Config.cur.windowChangeScreenRes = false;
        
        new Background();
        if("RU".equals(Config.LOCALE))
            MainWin32.GetAppPath();
        if(!super.beginApp(s, s1, i))
        {
            return false;
        } else
        {        	
            ForceFeedback.start();
            return true;
        }
    }

    private ScreenMode saveMode;
    private int saveMouseMode;
    private boolean bChangedScreenMode;
    private boolean bTryChangedScreenMode;
    private boolean bChangeTimerPause;

}
