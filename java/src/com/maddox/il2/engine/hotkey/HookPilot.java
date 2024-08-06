// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) deadcode 
// Source File Name:   HookPilot.java

package com.maddox.il2.engine.hotkey;

import com.maddox.JGP.*;
import com.maddox.il2.ai.*;
import com.maddox.il2.engine.*;
import com.maddox.il2.game.AircraftHotKeys;
import com.maddox.il2.game.Main3D;
import com.maddox.il2.game.OpenVR;
import com.maddox.il2.game.VisibilityChecker;
import com.maddox.il2.net.NetMissionTrack;
import com.maddox.il2.objects.air.Aircraft;
import com.maddox.rts.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

// Referenced classes of package com.maddox.il2.engine.hotkey:
//            HookView

public class HookPilot extends HookRender
{	
    private class RubberBand
    {

        public float getValue(byte byte0)
        {
            switch(byte0)
            {
            case 1: // '\001'
                return rubberBandSide;

            case 2: // '\002'
                return rubberBandUpDown;

            case 3: // '\003'
                return rubberBandBackForward;
            }
            return 1.0F;
        }

        public void setValue(byte byte0, float f)
        {
            switch(byte0)
            {
            case 1: // '\001'
                rubberBandSide = f;
                return;

            case 2: // '\002'
                rubberBandUpDown = f;
                return;

            case 3: // '\003'
                rubberBandBackForward = f;
                return;
            }
        }

        public static final byte SIDE = 1;
        public static final byte UP_DOWN = 2;
        public static final byte BACK_FORWARD = 3;
        float rubberBandSide;
        float rubberBandUpDown;
        float rubberBandBackForward;

        private RubberBand()
        {
            rubberBandSide = 1.0F;
            rubberBandUpDown = 1.0F;
            rubberBandBackForward = 1.0F;
        }

    }


    public void resetGame()
    {
        enemy = null;
        bUp = false;
    }

    public Point3d pCamera()
    {
        if(bTubeSight && isAimReached())
            return pAim;
        if(bUp)
            return pUp;
        else
            return pCenter;
    }

    public float getAzimut()
    {
        return o.getAzimut();
    }

    public float getTangage()
    {
        return o.getTangage();
    }

    public boolean isAimReached()
    {
        if(!bAim)
            return false;
        if(bTubeSight)
        {
            if((double)Math.abs(_Azimut - Azimut) > 0.5D)
                return false;
            if((double)Math.abs(_Tangage - Tangage) > 0.5D)
                return false;
            if((double)Math.abs(_Roll - Roll) > 0.5D)
                return false;
        }
        if(_leanForward != leanForward || _leanSide != leanSide || _raise != raise)
            return false;
        if(bSimpleUse)
        {
            pCenter.set(pAim);
            o.set(Azimut, Tangage, Roll);
            le.set(pCenter, o);
        }
        return true;
    }

    public void setStabilizedBSUse(boolean flag)
    {
    }

    public void setSimpleUse(boolean flag)
    {
        bSimpleUse = flag;
        if(!bSimpleUse)
        {
            pCenter.set(pAimTube);
            _leanForward = pAimLeanF;
            _leanSide = pAimLeanS;
            _raise = pAimRaise;
            headMoveToDef();
            rprevTime = Time.currentReal();
        }
    }

    public void setSimpleAimOrient(float f, float f1, float f2)
    {
        if(f >= 180F)
            f -= 360F;
        if(f < -180F)
            f += 360F;
        if(f1 >= 180F)
            f1 -= 360F;
        if(f1 < -180F)
            f1 += 360F;
        _Azimut = f;
        _Tangage = f1;
        Roll = _Roll = f2;
    }

    public void setInstantOrient(float f, float f1, float f2)
    {
        /*if(f >= 180F)
            f -= 360F;
        if(f < -180F)
            f += 360F;
        if(f1 >= 180F)
            f1 -= 360F;
        if(f1 < -180F)
            f1 += 360F;
        _Azimut = Azimut = f;
        _Tangage = Tangage = f1;
        _Roll = Roll = f2;
        o.set(f, f1, f2);
        le.set(pCamera(), o);*/
    }

    public void setInstantAim(boolean flag)
    {
        if(flag)
            pCenter.set(pAim);
        else
            pCenter.set(pCenterOrig);
    }

    public void setCenter(Point3d point3d)
    {
        pCenter.set(point3d);
        pCenterOrig.set(point3d);
    }

    public void setAim(Point3d point3d)
    {
        pAim.set(pCenter);
        if(point3d != null)
            pAim.set(point3d);
        setUp(point3d);
        findAimCoordinates(pAim);
    }

    public void setUp(Point3d point3d)
    {
        pUp.set(pCenter);
        if(point3d != null)
            pUp.set(point3d);
    }

    public void setSteps(float f, float f1)
    {
        stepAzimut = f;
        stepTangage = f1;
    }

    public void setMinMax(float f, float f1, float f2)
    {
        maxAzimut = f;
        minTangage = f1;
        maxTangage = f2;
    }

    public void setForward(boolean flag)
    {
    }

    public void endPadlock()
    {
    }

    private void _reset(boolean flag)
    {
        if(!AircraftHotKeys.bFirstHotCmd)
        {
            _Azimut = Azimut = 0.0F;
            _Tangage = Tangage = 0.0F;
            o.set(0.0F, 0.0F, 0.0F);
            le.set(pCamera(), o);
        }
        enemy = null;
        bPadlock = false;
        if(!Main3D.cur3D().isDemoPlaying())
            new MsgAction(64, 0.0D) {
                public void doAction()
                {
                    HotKeyCmd.exec("misc", "target_");
                }
            };
        headShift.set(0.0D, 0.0D, 0.0D);
        counterForce.set(0.0D, 0.0D, 0.0D);
        if(!flag)
        {
            bTubeSight = false;
            set6DoFLimits();
            findAimCoordinates(pAim);
            bRaiseUp = false;
            bRaiseDown = false;
            bLeanF = false;
            bLeanB = false;
            bLeanSideL = false;
            bLeanSideR = false;
        }
    }

    private void findAimCoordinates(Point3d point3d)
    {
        Point3d point3d1 = new Point3d();
        point3d1.set(point3d);
        point3d1.add(-faceL, 0.0D, 0.0D);
        pAimRaise = (float)point3d1.distance(pSpine) - spineL;
        pAimLeanS = (float)Math.asin((point3d1.y - pSpine.y) / (double)(pAimRaise + spineL));
        pAimLeanF = (float)Math.asin((point3d1.x - pSpine.x) / (double)(pAimRaise + spineL));
        if(bAim && !AircraftHotKeys.bFirstHotCmd)
        {
            leanForward = _leanForward = pAimLeanF;
            leanSide = _leanSide = pAimLeanS;
            raise = _raise = pAimRaise;
        } else
        {
            leanForward = _leanForward = leanForwardDefault;
            leanSide = _leanSide = 0.0F;
            raise = _raise = 0.0F;
        }
    }

    public boolean isTubeSight()
    {
        return bTubeSight;
    }

    public void setTubeSight(boolean flag)
    {
        bTubeSight = flag;
    }

    public void setTubeSight(Point3d point3d)
    {
        bTubeSight = true;
        pAimTube.set(point3d);
        pAimTube.add(pCenter);
        findAimCoordinates(pAimTube);
    }

    private void set6DoFLimits()
    {
        float af[] = Main3D.cur3D().cockpitCur.get6DoFLimits();
        spineL = af[0];
        faceL = af[1];
        spineOffsetX = af[2];
        leanSideMax = af[3];
        leanForwardMax = af[4];
        leanForwardMin = af[5];
        raiseMax = af[6];
        raiseMin = af[7];
        leanForwardDefault = (float)Math.asin(spineOffsetX / spineL);
        if(!AircraftHotKeys.bFirstHotCmd)
        {
            _leanForward = leanForward = leanForwardDefault;
            _leanSide = leanSide = 0.0F;
            _raise = raise = 0.0F;
        }
        pNeck.set(pCenterOrig);
        pNeck.add(-faceL, 0.0D, 0.0D);
        float f = -(float)Math.sqrt(spineL * spineL - spineOffsetX * spineOffsetX);
        pSpine.set(pNeck);
        pSpine.add(-spineOffsetX, 0.0D, f);
        leanForwardRange = leanForwardMax - leanForwardDefault;
        if(leanForwardRange < leanForwardDefault - leanForwardMin)
            leanForwardRange = leanForwardDefault - leanForwardMin;
        leanForwardRange = leanForwardRange * 1.1F;
        leanSideRange = leanSideMax * 1.1F;
        raiseRange = raiseMax;
        if(raiseRange < -raiseMin)
            raiseRange = -raiseMin;
        raiseRange = raiseRange * 1.1F;
    }

    public void saveRecordedStates(PrintWriter printwriter)
        throws Exception
    {
        printwriter.println(Azimut);
        printwriter.println(_Azimut);
        printwriter.println(Tangage);
        printwriter.println(_Tangage);
        printwriter.println(o.azimut());
        printwriter.println(o.tangage());
        printwriter.println(Roll);
        printwriter.println(_Roll);
        printwriter.println(o.kren());
        printwriter.println(leanForward);
        printwriter.println(_leanForward);
        printwriter.println(leanSide);
        printwriter.println(_leanSide);
        printwriter.println(raise);
        printwriter.println(_raise);
        printwriter.println(pCenter.x);
        printwriter.println(pCenter.y);
        printwriter.println(pCenter.z);
    }

    public void loadRecordedStates(BufferedReader bufferedreader)
        throws Exception
    {
        Azimut = Float.parseFloat(bufferedreader.readLine());
        _Azimut = Float.parseFloat(bufferedreader.readLine());
        Tangage = Float.parseFloat(bufferedreader.readLine());
        _Tangage = Float.parseFloat(bufferedreader.readLine());
        o.set(Float.parseFloat(bufferedreader.readLine()), Float.parseFloat(bufferedreader.readLine()), 0.0F);
        if(NetMissionTrack.playingVersion() > 103)
        {
            Roll = Float.parseFloat(bufferedreader.readLine());
            _Roll = Float.parseFloat(bufferedreader.readLine());
            o.set(o.getAzimut(), o.getTangage(), Float.parseFloat(bufferedreader.readLine()));
            leanForward = Float.parseFloat(bufferedreader.readLine());
            _leanForward = Float.parseFloat(bufferedreader.readLine());
            leanSide = Float.parseFloat(bufferedreader.readLine());
            _leanSide = Float.parseFloat(bufferedreader.readLine());
            raise = Float.parseFloat(bufferedreader.readLine());
            _raise = Float.parseFloat(bufferedreader.readLine());
            pCenter.x = Float.parseFloat(bufferedreader.readLine());
            pCenter.y = Float.parseFloat(bufferedreader.readLine());
            pCenter.z = Float.parseFloat(bufferedreader.readLine());
        }
        le.set(pCamera(), o);
    }

    public void reset()
    {
        _reset(false);
    }

    public boolean isPadlock()
    {
        return bPadlock;
    }

    public Actor getEnemy()
    {
        return enemy;
    }

    public void stopPadlock()
    {
        if(!bPadlock)
        {
            return;
        } else
        {
            _reset(true);
            return;
        }
    }

    public boolean startPadlock(Actor actor)
    {
        if(!bUse || bSimpleUse)
            return false;
        if(!Actor.isValid(actor))
        {
            bPadlock = false;
            return false;
        }
        Aircraft aircraft = World.getPlayerAircraft();
        if(!Actor.isValid(aircraft))
        {
            bPadlock = false;
            return false;
        }
        enemy = actor;
        Azimut = _Azimut;
        Tangage = _Tangage;
        bPadlock = true;
        aircraft.pos.getAbs(pAbs, oAbs);
        Camera3D camera3d = (Camera3D)target2;
        camera3d.pos.getAbs(o);
        o.sub(oAbs);
        if(!Main3D.cur3D().isDemoPlaying())
            new MsgAction(64, 0.0D) {

                public void doAction()
                {
                    HotKeyCmd.exec("misc", "target_");
                }

            };
        return true;
    }

    public boolean isAim()
    {
        return bAim;
    }

    public void doAim(boolean flag)
    {
        if(bAim == flag)
            return;
        if(flag)
        {
            _leanForward = pAimLeanF;
            _leanSide = pAimLeanS;
            _raise = pAimRaise;
        } else
        {
            _leanForward = leanForwardDefault;
            _leanSide = 0.0F;
            _raise = 0.0F;
        }
        bAim = flag;
    }

    public boolean isUp()
    {
        return bUp;
    }

    public void doUp(boolean flag)
    {
        if(bUp == flag)
        {
            return;
        } else
        {
            bUp = flag;
            return;
        }
    }

    private float bvalue(float f, float f1, long l)
    {
        float f2 = (HookView.koofSpeed * (float)l) / 30F;
        if(f == f1)
            return f;
        if(f > f1)
            if(f < f1 + f2)
                return f;
            else
                return f1 + f2;
        if(f > f1 - f2)
            return f;
        else
            return f1 - f2;
    }

    public void leanForwardMove(float f)
    {
        if(!bUse || bSimpleUse || bAim) return;
        
        _leanForward = f * leanForwardRange + leanForwardDefault;
        _leanForward = smoothLimit(_leanForward, leanForwardMin, leanForwardMax, (byte)3, false);
    }

    public void leanSideMove(float f)
    {
        if(!bUse || bSimpleUse || bAim) return;
        
        _leanSide = f * leanSideRange;
        _leanSide = smoothLimit(_leanSide, -leanSideMax, leanSideMax, (byte)1, false);
    }

    public void raiseMove(float f)
    {
        if(!bUse || bSimpleUse || bAim) return;
        
        _raise = f * raiseRange;
        _raise = smoothLimit(_raise, raiseMin, raiseMax, (byte)2, false);
    }

    public void moveHead(float f, float f1, float f2)
    {
    }

    private float smoothLimit(float f, float f1, float f2, byte byte0, boolean flag)
    {
        /*float f6 = 0.05F;
        float f7 = rubberBand.getValue(byte0);
        f7 *= 0.98F - HookView.rubberBandStretch;
        if((double)f7 < 0.0001D)
            f7 = 0.0F;
        float f8 = f;
        if(f > f2)
        {
            float f4 = f - f2;
            f8 = f2 + f4 * f7;
            if(f8 > f2 + f6)
                f8 = f2 + f6;
        } else
        if(f < f1)
        {
            float f5 = f - f1;
            f8 = f1 + f5 * f7;
            if(f8 < f1 - f6)
                f8 = f1 - f6;
        } else
        {
            float f9 = f1 + f2;
            if((double)Math.abs(f - f9) < 0.01D)
                f7 = 1.0F;
        }
        rubberBand.setValue(byte0, f7);
        return f8;*/
    	return f;
    }

    public void headMoveToDef()
    {
        if(bSimpleUse)
        {
            return;
        } else
        {
            _leanForward = leanForwardDefault;
            _leanSide = 0.0F;
            _raise = 0.0F;
            _Tangage = 0.0F;
            _Azimut = 0.0F;
            Roll = _Roll = 0.0F;
            bAim = false;
            return;
        }
    }

    private float checkAzimut(float f)
    {
        /*if(f > 180F)
            f -= 360F;
        else
        if(f < -180F)
            f += 360F;*/
        return f;
    }
    
    public boolean computeRenderPos(Actor actor, Loc loc, Loc loc1)
    {
    	if(!bUse) return true;
        
        long nowTime = Time.currentReal();
        if(nowTime != rprevTime && !bSimpleUse)
        {
            rprevTime = nowTime;
            
            pNeck.set(pSpine);
            pNeck.add((double)spineL * Math.sin(leanForward), (double)spineL * Math.sin(leanSide), (double)spineL * Math.cos(leanForward) * Math.cos(leanSide));

            /*Point3d point3d = new Point3d(pNeck);
            point3d.add(OpenVR.hmdLocation[2], OpenVR.hmdLocation[0], OpenVR.hmdLocation[1]);
            pCenter.set(point3d);*/
            
            pCenter.set(pNeck);
        }
        
        le.set(pCamera(), o);
        loc1.add(le, loc);
        return true;
    }

    public void computePos(Actor actor, Loc loc, Loc loc1)
    {
        if(bUse)
        {
            if(Time.isPaused() && !bPadlock)
            {
                /*if(World.cur().diffCur.Head_Shake)
                {
                    pe.add(pCamera(), P);
                    le.set(pe, o);
                } else
                {
                    le.set(pCamera(), o);
                }*/
            	le.set(pCamera(), o);
                loc1.add(le, loc);
                return;
            }
            loc1.add(le, loc);
            if(bPadlock)
                loc1.set(op);
        } else
        {
            loc1.set(loc);
        }
    }

    private float cValue(float f, float f1, float f2, long l)
    {
        float f3 = (f2 * (float)l) / 150F;
        if(f1 > f)
            if(f1 < f + f3)
                return f1;
            else
                return f + f3;
        if(f1 > f - f3)
            return f1;
        else
            return f - f3;
    }

    public void checkPadlockState()
    {
        if(!bPadlock)
            return;
        if(!Actor.isAlive(enemy))
        {
            return;
        } else
        {
            VisibilityChecker.checkLandObstacle = true;
            VisibilityChecker.checkCabinObstacle = true;
            VisibilityChecker.checkPlaneObstacle = true;
            VisibilityChecker.checkObjObstacle = true;
            return;
        }
    }

    public void setTarget(Actor actor)
    {
        target = actor;
    }

    public void setTarget2(Actor actor)
    {
        target2 = actor;
    }

    public boolean use(boolean flag)
    {
        boolean flag1 = bUse;
        bUse = flag;
        if(Actor.isValid(target))
            target.pos.inValidate(true);
        if(Actor.isValid(target2))
            target2.pos.inValidate(true);
        return flag1;
    }

    public boolean useMouse(boolean flag)
    {
        boolean flag1 = bUseMouse;
        bUseMouse = flag;
        return flag1;
    }

    public void mouseMoveHead(int x, int y, int wheel)
    {
    }

    public void mouseMove(int x, int y, int wheel)
    {
    }

    public void viewSet(float f, float f1, float f2)
    {
        /*if(!bUse || bPadlock || bSimpleUse)
            return;
        if(bTubeSight && bAim)
            return;
        if(bUseMouse)
        {
            f %= 360F;
            if(f > 180F)
                f -= 360F;
            else
            if(f < -180F)
                f += 360F;
            f1 %= 360F;
            if(f1 > 180F)
                f1 -= 360F;
            else
            if(f1 < -180F)
                f1 += 360F;
            f2 %= 360F;
            if(f2 > 180F)
                f2 -= 360F;
            else
            if(f2 < -180F)
                f2 += 360F;
            if(f < -maxAzimut)
                f = -maxAzimut;
            else
            if(f > maxAzimut)
                f = maxAzimut;
            if(f1 > maxTangage)
                f1 = maxTangage;
            else
            if(f1 < minTangage)
                f1 = minTangage;
            if(f2 > maxRoll)
                f2 = maxRoll;
            else
            if(f2 < minRoll)
                f2 = minRoll;
            _Azimut = Azimut = f;
            _Tangage = Tangage = f1;
            _Roll = Roll = f2;
            o.set(f, f1, f2);
            if(Actor.isValid(target))
                target.pos.inValidate(true);
            if(Actor.isValid(target2))
                target2.pos.inValidate(true);
        }*/
    }

    public void snapSet(float f, float f1)
    {
        /*if(!bUse || bPadlock || bSimpleUse)
            return;
        _Azimut = 45F * f;
        _Tangage = 44F * f1;
        Azimut = o.azimut() % 360F;
        if(Azimut > 180F)
            Azimut -= 360F;
        else
        if(Azimut < -180F)
            Azimut += 360F;
        Tangage = o.tangage() % 360F;
        if(Tangage > 180F)
            Tangage -= 360F;
        else
        if(Tangage < -180F)
            Tangage += 360F;
        if(Actor.isValid(target))
            target.pos.inValidate(true);
        if(Actor.isValid(target2))
            target2.pos.inValidate(true);*/
    }

    public void panSet(int i, int j)
    {
        /*if(!bUse || bPadlock || bSimpleUse)
            return;
        if(i == 0 && j == 0)
        {
            _Azimut = 0.0F;
            _Tangage = 0.0F;
        }
        if(_Azimut == -maxAzimut)
        {
            int k = (int)(_Azimut / stepAzimut);
            if(-_Azimut % stepAzimut > 0.01F * stepAzimut)
                k--;
            _Azimut = (float)k * stepAzimut;
        } else
        if(_Azimut == maxAzimut)
        {
            int l = (int)(_Azimut / stepAzimut);
            if(_Azimut % stepAzimut > 0.01F * stepAzimut)
                l++;
            _Azimut = (float)l * stepAzimut;
        }
        _Azimut = (float)i * stepAzimut + _Azimut;
        if(_Azimut < -maxAzimut)
            _Azimut = -maxAzimut;
        if(_Azimut > maxAzimut)
            _Azimut = maxAzimut;
        _Tangage = (float)j * stepTangage + _Tangage;
        if(_Tangage < minTangage)
            _Tangage = minTangage;
        if(_Tangage > maxTangage)
            _Tangage = maxTangage;
        Azimut = o.azimut() % 360F;
        if(Azimut > 180F)
            Azimut -= 360F;
        else
        if(Azimut < -180F)
            Azimut += 360F;
        Tangage = o.tangage() % 360F;
        if(Tangage > 180F)
            Tangage -= 360F;
        else
        if(Tangage < -180F)
            Tangage += 360F;
        if(Actor.isValid(target))
            target.pos.inValidate(true);
        if(Actor.isValid(target2))
            target2.pos.inValidate(true);*/
    }

    private HookPilot()
    {
        stepAzimut = 45F;
        stepTangage = 30F;
        maxAzimut = 155F;
        maxTangage = 89F;
        minTangage = -60F;
        maxRoll = 45F;
        minRoll = -45F;
        Azimut = 0.0F;
        Tangage = 0.0F;
        Roll = 0.0F;
        _Azimut = 0.0F;
        _Tangage = 0.0F;
        _Roll = 0.0F;
        rprevTime = 0L;
        o = new Orient();
        op = new Orient();
        le = new Loc();
        pe = new Point3d();
        pAbs = new Point3d();
        oAbs = new Orient();
        target = null;
        target2 = null;
        enemy = null;
        bUse = false;
        bPadlock = true;
        bAim = false;
        bUp = false;
        bTubeSight = false;
        bSimpleUse = false;
        pCenter = new Point3d();
        pAim = new Point3d();
        pUp = new Point3d();
        rubberBand = new RubberBand();
        bUseMouse = true;
        spineL = 0.5F;
        faceL = 0.08F;
        spineOffsetX = -0.05F;
        leanSideMax = 0.15F;
        leanForwardMax = 0.2F;
        leanForwardMin = -0.2F;
        leanForwardRange = 1.0F;
        leanSideRange = 0.4F;
        raiseRange = 0.15F;
        raiseMax = 0.05F;
        raiseMin = -0.05F;
        pCenterOrig = new Point3d();
        pSpine = new Point3d();
        pNeck = new Point3d();
        pAimTube = new Point3d();
        bRaiseUp = false;
        bRaiseDown = false;
        bLeanF = false;
        bLeanB = false;
        bLeanSideL = false;
        bLeanSideR = false;
        bHeadMouseMove = false;
    }

    public static HookPilot New()
    {
        if(current == null)
            current = new HookPilot();
        return current;
    }

    public static HookPilot cur()
    {
        return New();
    }

    public float getZNearOffsetX()
    {
        return (float)((pCenter.x - pCenterOrig.x) + (double)spineOffsetX);
    }

    public float getZNearOffsetY()
    {
        return (float)(pCenter.y - pCenterOrig.y);
    }

    private float stepAzimut;
    private float stepTangage;
    private float maxAzimut;
    private float maxTangage;
    private float minTangage;
    public static final float gyroStabBSMaxRoll = 20F;
    public static final float gyroStabBSMaxPitch = 20F;
    private float maxRoll;
    private float minRoll;
    private float Azimut;
    private float Tangage;
    private float Roll;
    private float _Azimut;
    private float _Tangage;
    private float _Roll;
    private long rprevTime;
    private Orient o;
    private Orient op;
    private Loc le;
    private Point3d pe;
    private Point3d pAbs;
    private Orient oAbs;
    private Actor target;
    private Actor target2;
    private Actor enemy;
    private boolean bUse;
    private boolean bPadlock;
    private boolean bAim;
    private boolean bUp;
    private boolean bTubeSight;
    private boolean bSimpleUse;
    private Point3d pCenter;
    private Point3d pAim;
    private Point3d pUp;
    private RubberBand rubberBand;
    private static Point3d P = new Point3d();
    private static Vector3d headShift = new Vector3d();
    private static Vector3d counterForce = new Vector3d();
    private boolean bUseMouse;
    public static HookPilot current;
    private Point3d pCenterOrig;
    private float leanForward;
    private float leanSide;
    private float raise;
    private float _leanForward;
    private float _leanSide;
    private float _raise;
    private float spineL;
    private float faceL;
    private float spineOffsetX;
    private float leanForwardDefault;
    private float leanSideMax;
    private float leanForwardMax;
    private float leanForwardMin;
    private float leanForwardRange;
    private float leanSideRange;
    private float raiseRange;
    private float raiseMax;
    private float raiseMin;
    private float pAimRaise;
    private float pAimLeanS;
    private float pAimLeanF;
    private Point3d pSpine;
    private Point3d pNeck;
    private Point3d pAimTube;
    public boolean bRaiseUp;
    public boolean bRaiseDown;
    public boolean bLeanF;
    public boolean bLeanB;
    public boolean bLeanSideL;
    public boolean bLeanSideR;
    public boolean bHeadMouseMove;
}
