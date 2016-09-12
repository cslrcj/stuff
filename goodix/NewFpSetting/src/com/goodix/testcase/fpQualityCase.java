package com.goodix.testcase;

import android.content.Context;

import com.goodix.device.FpDevice;
import com.goodix.testtool.R;

public final class fpQualityCase extends TestCaseBase {

	public fpQualityCase(Context context, FpDevice device) {
		super(context, device);
	}

	@Override
	public String getName() {
		return context.getText(R.string.fp_quality_case_name).toString();
	}

	@Override
	public String getDescription() {
		return context.getText(R.string.fp_quality_case_detail).toString();
	}


}
