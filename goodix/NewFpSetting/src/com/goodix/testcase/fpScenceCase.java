package com.goodix.testcase;

import android.content.Context;

import com.goodix.device.FpDevice;
import com.goodix.testtool.R;

public final class fpScenceCase extends TestCaseBase {

	public fpScenceCase(Context context, FpDevice device) {
		super(context, device);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return context.getText(R.string.fp_scence_case_name).toString();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return context.getText(R.string.fp_scence_case_detail).toString();
	}


}
