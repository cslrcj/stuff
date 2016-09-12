package com.goodix.testtool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartTestActivity extends Activity {

    private Button mLunchBtn;

    private TextView mResult;

    private static final int START_REQUEST_CODE = 3;

    private static final int TIME_ID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luncher);
        loadView();
    }

    private void loadView() {
        mLunchBtn = (Button) findViewById(R.id.lunch_start);
        mResult = (TextView) findViewById(R.id.lunch_result);

        mLunchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {



                Intent intent = new Intent(StartTestActivity.this,
                        FingerprintTestToolActivity.class);
                intent.putExtra("timeid", TIME_ID);
                intent.putExtra("command", "cmd");
                intent.putExtra("result", "result");
                startActivityForResult(intent, START_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == START_REQUEST_CODE) {
            int timeid = intent.getIntExtra("timeid", -1);
            String result = intent.getStringExtra("result");
            String log = null;
            if (TIME_ID == timeid) {
                log = "THE LAST TEST RESULT:\ntimeid : " + timeid + "\n";
            }
            log += "test result: "+result;
            System.out.println("-----------------" + log);
            mResult.setText(log);
        }
    }

    static {
        System.loadLibrary("fp_gf_mp");
    }

}

