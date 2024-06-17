// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   ConsoleGL0.java

package com.maddox.il2.engine;

import com.maddox.opengl.gl;
import com.maddox.rts.*;
import java.util.List;

// Referenced classes of package com.maddox.il2.engine:
//            Render, GObj, Renders, ConsoleGL0, 
//            RendersMain, TTFont

class ConsoleGL0Render extends Render
{

    public void exlusiveDraw()
    {
        if(RTSConf.cur instanceof RTSConfWin)
            ((MainWin32)RTSConf.cur.mainWindow).loopMsgs();
        GObj.DeleteCppObjects();
        renders().paint(this);
    }

    public void exlusiveDrawStep(String s, int i)
    {
        sstep = s;
        istep = i;
        renders().paint(this);
    }

    public void render()
    {
        if(ConsoleGL0.backgroundMat != null)
        {
            if(RendersMain.getViewPortWidth() / RendersMain.getViewPortHeight() < 1)
            {
                float f = (float)RendersMain.getViewPortWidth() * 0.75F;
                float f2 = ((float)RendersMain.getViewPortHeight() - f) * 0.5F;
                Render.drawTile(0.0F, f2, RendersMain.getViewPortWidth(), f, 0.0F, ConsoleGL0.backgroundMat, -1, 0.0F, 1.0F, 1.0F, -1F);
            } else
            {
                Render.drawTile(0.0F, 0.0F, RendersMain.getViewPortWidth(), RendersMain.getViewPortHeight(), 0.0F, ConsoleGL0.backgroundMat, -1, 0.0F, 1.0F, 1.0F, -1F);
            }
            if(sstep != null)
            {
                float f1 = TTFont.font[2].width(sstep);
                int i = 0xff0000ff;
                TTFont.font[2].output(i, (float)RendersMain.getViewPortWidth() * 0.083F, (float)RendersMain.getViewPortHeight() * 0.12F, 0.0F, sstep);
                TTFont.font[2].output(i, (float)RendersMain.getViewPortWidth() * 0.083F, ((float)RendersMain.getViewPortHeight() * 0.12F + (float)TTFont.font[2].height()) - (float)TTFont.font[2].descender(), 0.0F, "V 4.13.4m - VR version");
            }
            return;
        }
        if(ConsoleGL0.bActive || ConsoleGL0.consoleListener != null)
        {
            List list = RTSConf.cur.console.historyOut();
            int j = RTSConf.cur.console.startHistoryOut();
            if(!RTSConf.cur.console.isShowHistoryOut())
            {
                list = RTSConf.cur.console.historyCmd();
                j = RTSConf.cur.console.startHistoryCmd();
            }
            int k = getViewPortHeight() / ConsoleGL0.font.height() - 1;
            if(ConsoleGL0.bActive)
            {
                int l = RTSConf.cur.console.editBuf.length();
                String s = RTSConf.cur.console.getPrompt();
                int l1 = s.length();
                int i2 = 0;
                if(l1 != 0)
                    s.getChars(0, l1, buf, 0);
                if(l + l1 > 0)
                {
                    if(l + l1 > buf.length)
                        buf = new char[l + l1 + 16];
                    if(l != 0)
                    {
                        RTSConf.cur.console.editBuf.getChars(0, l, buf, l1);
                        float f4 = (float)getViewPortWidth() - ConsoleGL0.typeOffset;
                        do
                        {
                            float f6 = ConsoleGL0.font.width(buf, 0, (RTSConf.cur.console.editPos - i2) + l1);
                            if(f6 < f4)
                                break;
                            i2++;
                            RTSConf.cur.console.editBuf.getChars(i2, l, buf, l1);
                        } while(true);
                    }
                    ConsoleGL0.font.output(-1, ConsoleGL0.typeOffset, -ConsoleGL0.font.descender(), 0.0F, buf, 0, (l - i2) + l1);
                }
                if((Time.endReal() / 500L) % 3L != 0L)
                {
                    float f5 = 0.0F;
                    if(RTSConf.cur.console.editPos + l1 > 0)
                        f5 = ConsoleGL0.font.width(buf, 0, (RTSConf.cur.console.editPos - i2) + l1);
                    buf[0] = '|';
                    ConsoleGL0.font.output(-1, (ConsoleGL0.typeOffset + f5) - 1.0F, -ConsoleGL0.font.descender(), 0.0F, buf, 0, 1);
                }
            }
            if(RTSConf.cur.console.bWrap)
            {
                int i1 = j;
                int k1 = 1;
                for(; k > 0 && i1 < list.size(); i1++)
                {
                    String s1 = (String)list.get(i1);
                    int j2;
                    for(j2 = s1.length() - 1; j2 >= 0; j2--)
                        if(s1.charAt(j2) >= ' ')
                            break;

                    if(j2 > 0)
                    {
                        j2++;
                        int l2 = 0;
                        int i3 = 0;
                        int j3 = j2;
                        for(ofs[l2] = i3; j3 > 0 && ConsoleGL0.font.width(s1, i3, j3) > 0.0F; ofs[l2] = i3)
                        {
                            int k3 = j3;
                            while(ConsoleGL0.font.width(s1, i3, j3) + ConsoleGL0.typeOffset > (float)getViewPortWidth()) 
                            {
                                while(--j3 > 0) 
                                    if(s1.charAt(i3 + j3) == ' ')
                                        break;
                                if(j3 == 0)
                                    break;
                            }
                            if(j3 == 0)
                                for(j3 = k3; ConsoleGL0.font.width(s1, i3, j3) + ConsoleGL0.typeOffset > (float)getViewPortWidth();)
                                    if(--j3 == 0)
                                        break;

                            if(l2 + 1 >= ofs.length)
                                break;
                            l2++;
                            if(j3 == 0)
                            {
                                ofs[l2] = j2;
                                break;
                            }
                            i3 += j3;
                            j3 = j2 - i3;
                        }

                        for(; l2 > 0 && k > 0; k--)
                        {
                            ConsoleGL0.font.output(-1, ConsoleGL0.typeOffset, ConsoleGL0.font.height() * k1 - ConsoleGL0.font.descender(), 0.0F, s1, ofs[l2 - 1], ofs[l2] - ofs[l2 - 1]);
                            k1++;
                            l2--;
                        }

                    }
                }

            } else
            {
                if(k > list.size() - j)
                    k = list.size() - j;
                for(int j1 = 0; j1 < k; j1++)
                {
                    float f3 = ConsoleGL0.font.height() * (j1 + 1) - ConsoleGL0.font.descender();
                    String s2 = (String)list.get(j1 + j);
                    int k2;
                    for(k2 = s2.length() - 1; k2 >= 0; k2--)
                        if(s2.charAt(k2) >= ' ')
                            break;

                    if(k2 > 0)
                        ConsoleGL0.font.output(-1, ConsoleGL0.typeOffset, f3, 0.0F, s2, 0, k2 + 1);
                }

            }
        }
    }

    public ConsoleGL0Render(float f)
    {
        super(f);
        buf = new char[128];
        ofs = new int[128];
        sstep = null;
        istep = 0;
        useClearDepth(false);
        useClearColor(false);
    }

    public static final int COLOR = -1;
    private char buf[];
    private int ofs[];
    public String sstep;
    private int istep;
}
