package com.goodix.testcase;

import android.content.Context;

import com.goodix.device.FpDevice;

public abstract class TestCaseBase
{
    protected static final String TAG = "TestCase";
    
    protected Context  context;
    
    protected FpDevice device;
    
    protected String mReason = null; 
    
    public TestCaseBase(Context context,FpDevice device)
    {
        this.context = context;
        this.device  = device;

    }
    
    public abstract String getName();
    
    public abstract String getDescription();

    
}
