package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.util.CheckNetUtil;
import com.example.ibook.view.EmptyRecyclerView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.ibook.RegisterActivity.decodeUnicode;

public class ResultActivity extends AppCompatActivity {

    private EmptyRecyclerView recyclerView;
    private List<Book> books = new ArrayList<>();
    private BookAdapter bookAdapter;
    private Toolbar toolbar;
    private View emptyView;
    private String result;
    private String cookie;
    private String user_id;
    private String search;
    private String responseData;
    private JSONObject data;
    private boolean isManager;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        try {
            initView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initListener();


        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        bookAdapter = new BookAdapter(books, cookie, user_id, isManager);
        recyclerView.setAdapter(bookAdapter);
        recyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
        Log.e("检查网络状态结束", "ok");
        if (checkNetUtil.initNet()) {
            new Thread(runnable).start();
        }

    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            search(search);

//            Message msg = new Message();
//            Bundle data = new Bundle();
//            data.putString("value","请求结果");
//            msg.setData(data);
//            handler.sendMessage(msg);
        }
    };

    private void initView() throws JSONException {
        SharedPreferences sp = getSharedPreferences("login", 0);
        cookie = sp.getString("cookie", null);
        user_id = sp.getString("username", null);
        isManager = sp.getBoolean("isManager", false);

        emptyView = findViewById(R.id.iv_emptyView);
        recyclerView = findViewById(R.id.recyclerview);

        Intent intent = getIntent();
        result = intent.getStringExtra("result");
        search = intent.getStringExtra("search");
        assert result != null;
        JSONObject jsonObject = new JSONObject(result);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray bookarray = data.getJSONArray("result");
        int nums = data.getInt("total_num");

        Gson gson = new Gson();
        for (int i = 0; i < nums; i ++){
            books.add(gson.fromJson(bookarray.get(i).toString(), Book.class));
        }
    }

    private void initListener(){
    }

    private void search(String search){
        JSONObject json = new JSONObject();
        try {
            json.put("search", search);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("http://139.199.84.147/api/book" + "?search=" + search)
                .addHeader("cookie", cookie)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response;
        try {
            response = client.newCall(request).execute();
            responseData = response.body().string();
            Log.e("responseData", responseData);
            if (response.isSuccessful()) {
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    String msg = jsonObject.getString("msg");
                    //byte[] converttoBytes = msg.getBytes("UTF-8");
                    final String s2 = decodeUnicode(msg);
                    Log.e("msg:", s2);
                    if(code == 20000){
                        data = jsonObject.getJSONObject("data");

                        assert responseData != null;
                        JSONArray bookarray = data.getJSONArray("result");
                        int nums = data.getInt("total_num");

                        books.clear();
                        Gson gson = new Gson();
                        for (int i = 0; i < nums; i ++){
                            books.add(gson.fromJson(bookarray.get(i).toString(), Book.class));
                        }

                        bookAdapter = new BookAdapter(books, cookie, user_id, isManager);
//                        recyclerView.setAdapter(bookAdapter);
//                        recyclerView.setEmptyView(emptyView);

                    }else runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ResultActivity.this, s2, Toast.LENGTH_SHORT).show();
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
}