package com.example.ibird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity {

    private PromptDialog promptDialog;
    private String code;
    private String path;

    private String picPath;
    private JSONObject data;
    private JSONArray result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        Intent intent = getIntent();
        code = intent.getStringExtra("status");
        path = intent.getStringExtra("path");

        init();


    }

    public void init(){
         promptDialog = new PromptDialog(this);
         promptDialog.showLoading("正在识别");

         //recoPic();

         new Handler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 promptDialog.showSuccess("识别完成");
             }
         },2000);
    }

    public void recoPic(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // .addFormDataPart("headImage", imagePath, image)
                .addFormDataPart("path", path)
                .build();

        final Request request = new Request.Builder()
                .url("https://weparallelines.top/api/prediction/predict")
                //.addHeader("Cookie", cookie)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ResultActivity", "获取数据失败");
                Log.e("ResultActivity", String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("responseData", responseData);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        data = jsonObject.getJSONObject("data");
                        result = data.getJSONArray("result");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                        //msg = jsonObject.getString("msg");
                        //Log.e("返回数据", "code:" + code + " msg:" + msg + " path:" + path);

//                        if (statu) {
//
//                            String pic = "http://139.199.84.147" + jsonObject.getString("pic");
//                            SharedPreferences sharedPreferences = getSharedPreferences("theUser", Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            if (type.equals("avatar")) {
//                                editor.putString("avater", pic);
//                                editor.apply();
//                            } else {
//                                editor.putString("background", pic);
//                                editor.apply();
//                            }
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("DetailUserActivity", response.body().string());
                        Log.e("DetailUserActivity", String.valueOf(e));
                    }

                }
            }
        });
    }
}