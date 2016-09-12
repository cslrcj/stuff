package com.goodix.testtool;

import java.lang.ref.WeakReference;
import java.util.Vector;

import com.goodix.device.FpDevice;
import com.goodix.testcase.TestCaseBase;
import com.goodix.testcase.fpFingerDownCase;
import com.goodix.testcase.fpFingerUpCase;
import com.goodix.testcase.fpPerformanceCase;
import com.goodix.testcase.fpQualityCase;
import com.goodix.testcase.fpScenceCase;
import com.goodix.testcase.fpSelfCase;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;



public class FingerprintTestToolActivity extends Activity {

    private static Vector<TestCaseItem> mCaseList = new Vector<TestCaseItem>();
    private static TestCaseItem downTestCastItem = null;
    private static TestCaseItem upTestCastItem = null;
	private static FpDevice mDevice;

    private static Context activity;

	private LayoutInflater mInflater;
	private MyHandler mHandler;
	private Button mTest;




	private static Button mBack = null;

    private static boolean enableKeyBack = false;

    private static Button bt_notice;

	private ViewGroup mContainerRoot;

	private boolean mTestResult = true;

	private static int mTimeID = -1;

    private static boolean theLastRes = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fingerprinttest);

        activity = this;

        enableKeyBack = true;

		Intent intent = getIntent();
		mTimeID = intent.getIntExtra("timeid", -1);

        mHandler = new MyHandler(this);
		mDevice = FpDevice.open(mHandler);
        bt_notice = (Button)findViewById(R.id.bt_notice);

		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mInflater = LayoutInflater.from(this);

		loadTestCase();
		loadView();
	}


    private void loadTestCase() {
        TestCaseItem testCastItem = null;
        testCastItem = new TestCaseItem(mInflater,new fpSelfCase(this, mDevice));
        mCaseList.add(testCastItem);
        downTestCastItem = new TestCaseItem(mInflater, new fpFingerDownCase(this,mDevice));
        mCaseList.add(downTestCastItem);
        testCastItem = new TestCaseItem(mInflater, new fpPerformanceCase(this,mDevice));
        mCaseList.add(testCastItem);
        upTestCastItem = new TestCaseItem(mInflater, new fpFingerUpCase(this,mDevice));
        mCaseList.add(upTestCastItem);
        testCastItem = new TestCaseItem(mInflater, new fpQualityCase(this,mDevice));
        mCaseList.add(testCastItem);
        testCastItem = new TestCaseItem(mInflater, new fpScenceCase(this,mDevice));
        mCaseList.add(testCastItem);
    }


	private void loadView() {

		mContainerRoot = (ViewGroup) findViewById(R.id.case_root_container);

		mTest = (Button) findViewById(R.id.case_test);
		mTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                mHandler.sendMessage(mHandler.obtainMessage( MyHandler.MSG_CHECK, 0, 0, mCaseList.get(0)));

                FingerprintTestToolActivity.enableKeyBack = false;

                new Thread(){
                    public void run(){
                        mDevice.SendCmd(26, 3000 + " ", null);
                    }
                }.start();

                mTest.setEnabled(false);
                mBack.setEnabled(false);
            }
        });

        mBack = (Button) findViewById(R.id.case_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setRegisterResult(mTimeID, ((theLastRes == true) ? "PASS" : "FAIL"));
                mCaseList.clear();  //important
                finish();
            }
        });

        for (TestCaseItem testCaseItem : mCaseList) {
            mContainerRoot.addView(testCaseItem.getView());
        }

	}


    private static class MyHandler extends Handler { //Handler

        public static final int MSG_SELFTEST = 0;
        public static final int MSG_FINGER_DOWN = 1;
        public static final int  MSG_PERFORMACE = 2;
        public static final int MSG_FINGER_UP = 3;
        public static final int MSG_IAMGE_QUALITY  = 4;
        public static final int MSG_SCENE = 5;

		public static final int MSG_RESULT = 11;
		public static final int MSG_CHECK = 12;
		//msg for finger print case dialog
		public static final int MSG_FINGER_PRINT_DIALOG = 13;
		//msg for finger up case dialog
		public static final int MSG_FINGER_UP_DIALOG = 14;
		public static final int MSG_CLEAN_AFTER_TEST = 15;

		private WeakReference<FingerprintTestToolActivity> ref;
		public MyHandler(FingerprintTestToolActivity activity) {
			ref = new WeakReference<FingerprintTestToolActivity>(activity);
		}


		public void handleMessage(Message msg) {

            System.out.println("XYF0910: handler msg: --" + msg.what +";"+ msg.arg1 +";"+ msg.arg2 +"; " + (TestCaseItem)msg.obj);
            System.out.println("XYF0910: ref.get() = " + ref.get());


			switch (msg.what) {

                case MSG_SELFTEST:
                case MSG_FINGER_DOWN:
                case MSG_PERFORMACE:
                case MSG_FINGER_UP:
                case MSG_IAMGE_QUALITY:
                case MSG_SCENE:
                {
                    mCaseList.get(msg.what).setResultFromCallback(msg.what, msg.arg1);  //caseNum, resultNum:
                    break;
                }

                case MSG_RESULT: {
                    TestCaseItem item = (TestCaseItem) msg.obj;
                    item.update((msg.arg2 == 0) ? true : false);
                    break;
                }

                case MSG_CHECK: {
                    TestCaseItem item = (TestCaseItem) msg.obj;
                    item.mResult.setText(R.string.case_checking);
                    item.mResult.setTextColor(activity.getResources().getColor(R.color.green));
                    break;
                }

                case MSG_FINGER_PRINT_DIALOG: {
                    if(msg.arg1 == 0){
                        System.out.println("-----------XYF"+ msg.what +";"+ msg.arg1 +";"+ msg.arg2 +"; " + ((TestCaseItem)msg.obj).mNameTxt);
                        downTestCastItem.mDescriptionTxt.setText(R.string.fp_finger_down_case_message);
                        downTestCastItem.mDescriptionTxt.setTextSize(20);
                        downTestCastItem.mDescriptionTxt.setTextColor(activity.getResources().getColor(R.color.red));
                    }
                    break;
                }
                case MSG_FINGER_UP_DIALOG: {
                    if(msg.arg1 == 2){
                        upTestCastItem.mDescriptionTxt.setText(R.string.fp_finger_up_case_message);
                        upTestCastItem.mDescriptionTxt.setTextSize(20);
                        upTestCastItem.mDescriptionTxt.setTextColor(activity.getResources().getColor(R.color.red));
                    }
                    break;
                }

                case MSG_CLEAN_AFTER_TEST: {
                    if(msg.arg1 == 1){
                        downTestCastItem.mDescriptionTxt.setText(downTestCastItem.mCase.getDescription());
                        downTestCastItem.mDescriptionTxt.setTextColor(activity.getResources().getColor(
                                R.color.gray_level_three));
                        downTestCastItem.mDescriptionTxt.setTextSize(15);
                    }
                    if(msg.arg1 == 3){
                        upTestCastItem.mDescriptionTxt.setText(upTestCastItem.mCase.getDescription());
                        upTestCastItem.mDescriptionTxt.setTextColor(activity.getResources().getColor(
                                R.color.gray_level_three));
                        upTestCastItem.mDescriptionTxt.setTextSize(15);
                    }
                    break;
                }

                default:
                    break;
			}


            if(msg.obj == null){
                if(msg.arg1 != 0 || msg.what == MSG_SCENE){
                    mBack.setEnabled(true);
                    enableKeyBack = true;
                    bt_notice.setVisibility(View.INVISIBLE);
                }
            }

		}

	}


	private class TestCaseItem {

		public ViewGroup mContainerItem;
		public TextView mNameTxt;
		public TextView mDescriptionTxt;
		public TextView mFailReason;
		public TextView mResult;
		public TestCaseBase mCase;
		public TextView mTimeText;
		public long mTime;

		public TestCaseItem(LayoutInflater inflater, TestCaseBase testCase) {

            mContainerItem = (ViewGroup) inflater.inflate(R.layout.testcase, null);
			mNameTxt = (TextView) mContainerItem.findViewById(R.id.case_name);
			mDescriptionTxt = (TextView) mContainerItem.findViewById(R.id.case_description);
			mFailReason = (TextView) mContainerItem.findViewById(R.id.case_fail_reason);
			mTimeText = (TextView) mContainerItem.findViewById(R.id.time_spend);
			mResult = (TextView) mContainerItem.findViewById(R.id.case_result);
			mResult.setTextColor(getResources().getColor(R.color.gray_level_three));
			mResult.setText(R.string.show_case_result);

			this.mCase = testCase;
			mNameTxt.setText(mCase.getName());
			mDescriptionTxt.setText(mCase.getDescription());
		}

		public View getView() {
            return mContainerItem;
		}

		public boolean setResultFromCallback(int caseNum, int resultNum) {

            if(resultNum==0 && caseNum<5){
                mHandler.sendMessage(mHandler.obtainMessage( MyHandler.MSG_CHECK, caseNum, resultNum, mCaseList.get(caseNum+1)));
            }

			if (caseNum == 0 && resultNum==0) {
				mHandler.sendMessage(mHandler.obtainMessage( MyHandler.MSG_FINGER_PRINT_DIALOG,caseNum,resultNum,this));
			}

			if (caseNum == 2 && resultNum==0) {
				mHandler.sendMessage(mHandler.obtainMessage( MyHandler.MSG_FINGER_UP_DIALOG,caseNum,resultNum,this));
			}

            boolean result = false;
            if(resultNum == 0){
                result = true;
            }else{
                result = false;
            }

            theLastRes = result;

			mHandler.sendMessage(mHandler.obtainMessage(MyHandler.MSG_RESULT, caseNum,  resultNum,  this));

            if(caseNum==1 || caseNum==3){
			    mHandler.sendMessage(mHandler.obtainMessage(MyHandler.MSG_CLEAN_AFTER_TEST, caseNum, resultNum, this));  //arg1
            }

			return result;
		}


		public void update(boolean result) {

			if (result) {
				mResult.setTextColor(getResources().getColor(R.color.green));
                mResult.setText(R.string.case_pass);
            } else {
				mResult.setTextColor(getResources().getColor(R.color.red));
				mResult.setText(R.string.case_fail);
			}

		}

	}


    @Override
    public void onBackPressed() {
        if(enableKeyBack){
            setRegisterResult(mTimeID, ((theLastRes == true)? "PASS":"FAIL"));
            mCaseList.clear();
            super.onBackPressed();
            return;
        }
        System.out.println("XYF: 按下了安卓back键！ enableKeyBack = " + enableKeyBack);
        bt_notice.setVisibility(View.VISIBLE);
        bt_notice.setText(R.string.notice_cannotback_message);
        bt_notice.setTextColor(0xffff0000);
    }


    private void setRegisterResult(int timeid, String result) {
        Intent intent = new Intent(FingerprintTestToolActivity.this, StartTestActivity.class);
        intent.putExtra("timeid", mTimeID);
        intent.putExtra("result", result);
        setResult(RESULT_OK, intent);
    }

}
