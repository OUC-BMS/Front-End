package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.adapter.HistoryAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.bean.History;
import com.example.ibook.util.CheckNetUtil;
import com.example.ibook.view.EmptyRecyclerView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity {

    private EmptyRecyclerView recyclerView;
    private List<History> histories = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private Toolbar toolbar;
    private View emptyView;
    private String cookie;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
        initListener();


        SharedPreferences sp = getSharedPreferences("login", 0);
        cookie = sp.getString("cookie", null);
        Log.e("我的借阅拿cookie", cookie);


        CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
        Log.e("检查网络状态结束", "ok");
        if (checkNetUtil.initNet()) {
            sendRequestWithOkHttp();
        }
        

    }

    private void initView(){
        emptyView = findViewById(R.id.iv_emptyView);
        recyclerView = findViewById(R.id.recyclerview);
    }

    private void initListener(){
//        floatActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent= new Intent(getActivity(), CreateMissionActivity.class);
//                startActivity(intent);
//            }
//        });

//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()){
//                    case R.id.setDisplay:
//                        break;
//                    case R.id.batchOperation:
//                        break;
//                }
//                return false;
//            }
//        });
    }

    private void sendRequestWithOkHttp(){
        //开启现线程发起网络请求
        new Thread(new Runnable(){
            @Override
            public void run(){
                try
                {
                    Request request=new Request.Builder()  //请求对象，设置的参数详细要看源码解释
                            .url("http://139.199.84.147/api/book/log")    //这里的url参数值是自己访问的api
                            .addHeader("cookie", cookie)
                            .build();
                    Response response = null;   //建立一个响应对象，一开始置为null

                    OkHttpClient client = new OkHttpClient();

                    Call call = client.newCall(request); //开始申请，发送网络请求。
                    response = call.execute();
                    String responseData = response.body().string();
                    Log.e("responseData我的借阅", responseData);
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONObject data = jsonObject.getJSONObject("data");
                            int num = data.getInt("num");
                            JSONArray result = data.getJSONArray("result");
                            Gson gson = new Gson();
                            for (int i = 0; i < num; i ++){
                                if(gson.fromJson(result.get(i).toString(), History.class).getStatus() == 2)
                                    histories.add(gson.fromJson(result.get(i).toString(), History.class));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LinearLayoutManager manager=new LinearLayoutManager(HistoryActivity.this);
                                    recyclerView.setLayoutManager(manager);

                                    historyAdapter = new HistoryAdapter(histories);
                                    recyclerView.setAdapter(historyAdapter);
                                    recyclerView.setEmptyView(emptyView);
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}