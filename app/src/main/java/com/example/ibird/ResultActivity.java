package com.example.ibird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import me.leefeng.promptlibrary.PromptDialog;

public class ResultActivity extends AppCompatActivity {

    private PromptDialog promptDialog = new PromptDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        promptDialog.showLoading("正在识别");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                promptDialog.showSuccess("识别完成");
            }
        },2000);
    }
}