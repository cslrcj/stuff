package com.goodix.testcase;

import android.content.Context;

import com.goodix.device.FpDevice;
import com.goodix.testtool.R;

public final class fpFingerUpCase extends TestCaseBase {

	public fpFingerUpCase(Context context, FpDevice device) {
		super(context, device);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return context.getText(R.string.fp_finger_up_case_name).toString();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return context.getText(R.string.fp_finger_up_case_name).toString();
	}

	/*@Override
	public String getFailReason() {
		// TODO Auto-generated method stub
		return mReason;
	}*/

	/*@Override
	public boolean test() {
		int result = device.up(3000);

		if (result == FpDevice.ERR_SUCCESS) {
			mReason = null;
			return true;
		} else {
			// TODO add fail reason depends on the result code.
			switch (result){
				case FpDevice.ERR_TIMEOUT:
					mReason = "upCase:GX_ERR_TIMEOUT"; break;
				case FpDevice.ERR_OPEN_DEVICEFAIL:
					mReason = "upCase:GX_ERR_OPEN_DEVICEFAIL"; break;
				case FpDevice.ERR_NEEDTRY:
					mReason = "upCase:GX_ERR_NEEDTRY"; break;
				case  FpDevice.ERR_FAILED:
					mReason = "upCase:GX_ERR_FAILED"; break;
				default:
					mReason = result + " ----"; break;
			}
			return false;
		}
	}*/
}
