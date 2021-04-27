package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.util.CheckNetUtil;
import com.example.ibook.view.EmptyRecyclerView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
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


public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    private JSONObject data;
    private String searchstring;
    private String responseData;
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initListener();

    }

    private void initView(){
        searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);//设为true则搜索栏 缩小成俄日一个图标点击展开
        //设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //设置默认提示文字
        searchView.setQueryHint("输入您想查找的内容");

        SharedPreferences sp = getSharedPreferences("login", 0);
        cookie = sp.getString("cookie", null);
    }

    private void initListener(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //点击搜索按钮时触发
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchstring = query;
                Log.e("输入框的文字", query);
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    Intent intent = new Intent(SearchActivity.this, ResultActivity.class);
                    //intent.putExtra("result", responseData);
                    startActivity(intent);
                    new Thread(runnable).start();
                }

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            //search(searchstring);

//            Message msg = new Message();
//            Bundle data = new Bundle();
//            data.putString("value","请求结果");
//            msg.setData(data);
//            handler.sendMessage(msg);
        }
    };

    private void search(String search){
        JSONObject json = new JSONObject();
        try {
            json.put("search", search);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("https://139.199.84.147/api/book")
                .addHeader("Cookie", cookie)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response;
        try {
            response = client.newCall(request).execute();
            responseData = response.body().string();
            Log.e("responseData", responseData);
            if (response.isSuccessful()) {
                try {
                    Gson gson = new Gson();
                    JSONObject jsonObject = new JSONObject(responseData);
                    data = jsonObject.getJSONObject("data");
                    Intent intent = new Intent(SearchActivity.this, ResultActivity.class);
                    intent.putExtra("result", responseData);

//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}