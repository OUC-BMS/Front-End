package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.util.CheckNetUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ren.qinc.numberbutton.NumberButton;

import static com.example.ibook.ResultActivity.JSON;

public class NewBookActivity extends AppCompatActivity {

    private NumberButton numberButton;
    private EditText et_title;
    private EditText et_author;
    private EditText et_id;
    private EditText et_press;
    private Button btn_add;
    private String cookie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        initView();
        initListener();
    }

    private void initView(){
        SharedPreferences sp = getSharedPreferences("login", 0);
        cookie = sp.getString("cookie", null);

        numberButton = (NumberButton) findViewById(R.id.number_button);
        et_author = findViewById(R.id.et_author);
        et_id = findViewById(R.id.et_booknum);
        et_press = findViewById(R.id.et_editor);
        et_title = findViewById(R.id.et_title);
        btn_add = findViewById(R.id.btn_add);
        numberButton.setBuyMax(99)
                .setInventory(100)
                .setCurrentNumber(1)
                .setOnWarnListener(new NumberButton.OnWarnListener() {
                    @Override
                    public void onWarningForInventory(int inventory) {
                        Toast.makeText(NewBookActivity.this, "当前库存:" + inventory, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWarningForBuyMax(int buyMax) {
                        Toast.makeText(NewBookActivity.this, "超过最大添加数:" + buyMax, Toast.LENGTH_SHORT).show();
                    }
                });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    sendRequestWithOkHttp();
                }
            }
        });
    }

    private void initListener(){

    }

    private void sendRequestWithOkHttp(){
        //开启现线程发起网络请求
        new Thread(new Runnable(){
            @Override
            public void run(){
                JSONObject json = new JSONObject();
                try {
                    json.put("book_id", et_id.getText());
                    json.put("book_name", et_title.getText());
                    json.put("book_author", et_author.getText());
                    json.put("book_press", et_press.getText());
                    json.put("book_num", numberButton.getNumber());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                final Request request = new Request.Builder()
                        .url("http://139.199.84.147/api/book/newbook")
                        .addHeader("Cookie", cookie)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();

                Response response;
                try {
                    response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.e("responseData", responseData);
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            JSONObject jsonObject = new JSONObject(responseData);
                            int code = jsonObject.getInt("code");
                            if(code == 20000){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(NewBookActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                finish();
                            }


                            else
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(NewBookActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}