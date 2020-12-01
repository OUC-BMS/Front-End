package com.example.ibird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ibird.util.CheckNetUtil;

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

public class TestActivity extends AppCompatActivity {

    private Button btn_test;
    private Button btn_cancel;
    private String picpath;
    private String code;
    private JSONObject data;
    private String msg;
    private String path;
    private ImageView iv_bird;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Intent intent = getIntent();

        picpath = intent.getStringExtra("path");
        Log.e("intent结束", picpath);
        init();
    }
    private void init(){
        btn_test = findViewById(R.id.btn_test);
        btn_cancel = findViewById(R.id.btn_cancel);
        iv_bird = findViewById(R.id.iv_bird);


        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    new Thread(runnable).start();
                }
                btn_test.setClickable(false);

//                if(code.equals("20000")){
//                    Intent intent = new Intent(TestActivity.this, ResultActivity.class);
//                    intent.putExtra("status", code);
//                    intent.putExtra("path", path);
//                    startActivity(intent);
//                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Bitmap bitmap = BitmapFactory.decodeFile(picpath);
        if (bitmap != null) {
            iv_bird.setImageBitmap(bitmap);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.e("TAG","请求结果:" + val);
        }
    };

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            try {
                postImage(picpath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void postImage(String filePath) throws IOException {
        Log.e("DetailUserActivity", filePath);
//        if (imagePath != null) {
//            //这里可以上服务器;

        File file = new File(filePath);
        RequestBody image = RequestBody.create(MediaType.parse("image/png"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", file.getName(), image)
                .addFormDataPart("usage", "p")
                .build();

        final Request request = new Request.Builder()
                .url("https://weparallelines.top/api/upload")
                //.addHeader("Cookie", cookie)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response;
        try {
            response = client.newCall(request).execute();
            String jsonString = response.body().string();
            Log.e("响应内容"," upload jsonString ="+jsonString);
            if(response.isSuccessful()){
                JSONObject jsonObject = new JSONObject(jsonString);
                code = jsonObject.getString("code");
                data = jsonObject.getJSONObject("data");
                msg = jsonObject.getString("msg");
                path = data.getString("path");
                Log.e("data", data.toString());
                Log.e("path", path);
            }
            if(code.equals("20000")){
                Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                intent.putExtra("status", code);
                intent.putExtra("path", path);
                startActivity(intent);
                finish();
            }
        } catch (IOException | JSONException e) {
            Log.d("出错","upload IOException ",e);
        }
    }
}