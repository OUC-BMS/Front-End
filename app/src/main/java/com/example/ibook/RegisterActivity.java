package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.ibook.ResultActivity.JSON;

public class RegisterActivity extends AppCompatActivity {

    private Button btn_login;
    private Button btn_signup;
    private EditText et_username;
    private EditText et_password;
    private EditText et_usernum;
    private int code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        init();
    }
    private void init(){
        et_usernum = findViewById(R.id.et_usernum);
        et_password = findViewById(R.id.et_password);
        et_username = findViewById(R.id.et_username);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    new Thread(runnable).start();
                }


            }
        });
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            signup();

//            Message msg = new Message();
//            Bundle data = new Bundle();
//            data.putString("value","请求结果");
//            msg.setData(data);
//            handler.sendMessage(msg);
        }
    };

    @SuppressLint("ShowToast")
    private void signup(){
        JSONObject json = new JSONObject();
        try {
            json.put("name", et_username.getText());
            json.put("pwd", et_password.getText());
            json.put("userid", et_usernum.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("http://139.199.84.147/api/usr/register")
                //.addHeader("Cookie", cookie)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response;
        try {
            response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.e("responseData注册", responseData);
            if (response.isSuccessful()) {
                try {
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(responseData);
                    code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    //byte[] converttoBytes = msg.getBytes("UTF-8");
                    final String s2 = decodeUnicode(msg);
                    Log.e("msg:", s2);
                    if(code == 20000){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, s2, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Unicode转中文
    public static String decodeUnicode(final String unicode) {
        StringBuffer string = new StringBuffer();

        String[] hex = unicode.split("\\\\u");

        for (int i = 0; i < hex.length; i++) {

            try {
                // 汉字范围 \u4e00-\u9fa5 (中文)
                if(hex[i].length()>=4){//取前四个，判断是否是汉字
                    String chinese = hex[i].substring(0, 4);
                    try {
                        int chr = Integer.parseInt(chinese, 16);
                        boolean isChinese = isChinese((char) chr);
                        //转化成功，判断是否在  汉字范围内
                        if (isChinese){//在汉字范围内
                            // 追加成string
                            string.append((char) chr);
                            //并且追加  后面的字符
                            String behindString = hex[i].substring(4);
                            string.append(behindString);
                        }else {
                            string.append(hex[i]);
                        }
                    } catch (NumberFormatException e1) {
                        string.append(hex[i]);
                    }

                }else{
                    string.append(hex[i]);
                }
            } catch (NumberFormatException e) {
                string.append(hex[i]);
            }
        }

        return string.toString();
    }

    /**
     * 判断是否为中文字符
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
}