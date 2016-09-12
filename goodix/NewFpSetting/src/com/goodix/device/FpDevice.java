package com.goodix.device;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FpDevice {

    public static FpDevice mDevice = null;
    public static Handler handler = null;
    private int mNativeContext;

    private FpDevice(Handler handler)
    {
        this.handler = handler;
        WeakReference<FpDevice> ref = new WeakReference<FpDevice>(this);
        try {
            native_setup(ref);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public static final FpDevice open(Handler handler)
    {
        synchronized (FpDevice.class) {

            if (null == mDevice) {
                mDevice = new FpDevice(handler);
            }

            return mDevice;
        }
    }

    private static void postEventFromNative(Object fpdevice_ref, int msgType_what, int caseNum_arg1, int resultNum_arg2, Object obj) {

        Log.d("XYF0909", String.format("postEventFromNative msgType_what: %s; caseNum_arg1 = %d; resultNum_arg2 = %d \n", MessageType.getString(msgType_what), caseNum_arg1, resultNum_arg2));
        if(msgType_what == 4500) {
            handler.sendMessage(handler.obtainMessage(caseNum_arg1, resultNum_arg2, msgType_what));
        }
    }


    public int SendCmd(int cmd, String arg1, String arg2) {
        return SendCmdLegac(cmd, arg1, arg2);
    }

    private native final void native_setup(Object fpdevice);

    private native final void native_release();

    public native int query();

    public native int register();

    public native int cancelRegister();

    public native int resetRegister();

    public native int saveRegister(int index);

    public native int setMode(int cmd);

    public native int SendCmdLegac(int cmd, String arg1, String arg2);

    public native int recognize();

    public native int delete(int index);

    public native String getInfo();

    public native int checkPassword(String password);

    public native int getPermission(String password);

    public native int changePassword(String oldPwd, String newPwd);

    public native int cancelRecognize();

    // zhaoyi add 20140731, get screen state for set chip work mode
    public native int sendScreenState(int state);

}