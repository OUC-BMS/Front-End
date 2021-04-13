package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ibook.R;
import com.example.ibook.util.CheckNetUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.ibook.ResultActivity.JSON;

public class LoginActivity extends AppCompatActivity {

    private Button btn_register;
    private Button btn_login;
    private Context context;
    private EditText et_username;
    private EditText et_password;
    private String cookie;
    int code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        init();
    }
    private void init(){
        context = this;


        et_password = findViewById(R.id.et_password);
        et_username = findViewById(R.id.et_username);

        btn_register = findViewById(R.id.btn_register);
        btn_login = findViewById(R.id.btn_login);

        SharedPreferences sp = getSharedPreferences("login", 0);
        String avatar = sp.getString("avatar", null);
        String password = sp.getString("password", null);
        String username = sp.getString("username", null);
        if (password != null && username != null){
            et_username.setText(username);
            et_password.setText(password);
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    new Thread(runnable).start();
                }


            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, com.example.ibook.RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

//    Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle data = msg.getData();
//            String val = data.getString("value");
//            Log.e("TAG","请求结果:" + val);
//        }
//    };

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            register();

//            Message msg = new Message();
//            Bundle data = new Bundle();
//            data.putString("value","请求结果");
//            msg.setData(data);
//            handler.sendMessage(msg);
        }
    };


    private void register(){
        JSONObject json = new JSONObject();
        try {
            json.put("username", et_username.getText());
            json.put("password", et_password.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("https://weparallelines.top/api/account/login")
                //.addHeader("Cookie", cookie)
                .post(requestBody)
                .build();

        OkHttpClient client = (OkHttpClient) new OkHttpClient.Builder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
                Log.e("PostWithCookie","保存cookie");
                Log.e("PostCookie", String.valueOf(cookies));
                for (int i = 0;i<cookies.size();i++){
                    Log.e("Cookie"+ i, String.valueOf(cookies.get(i)));
                    cookie = String.valueOf(cookies.get(i));
                }

                SharedPreferences sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cookie",cookie);
                editor.apply();
            }
            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                List<Cookie> cookies = new ArrayList<>();
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        })
        .build();

        Response response;
        try {
            response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.e("responseData登录", responseData);
            if (response.isSuccessful()) {
                try {
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(responseData);
                    code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    byte[] converttoBytes = msg.getBytes("UTF-8");
                    final String s2 = new String(converttoBytes, "UTF-8");
                    if(code == 20000){
                        SharedPreferences sp = getSharedPreferences("login", 0);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("username", String.valueOf(et_username.getText()));
                        editor.putString("password", String.valueOf(et_password.getText()));
                        editor.commit();

                        //new Thread(runnable2).start();
                        finish();

                    }else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, s2, Toast.LENGTH_SHORT).show();
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}