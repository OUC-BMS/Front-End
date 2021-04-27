package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.view.EmptyRecyclerView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;

public class ResultActivity extends AppCompatActivity {

    private EmptyRecyclerView recyclerView;
    private List<Book> books = new ArrayList<>();
    private BookAdapter bookAdapter;
    private Toolbar toolbar;
    private View emptyView;
    private String result;
    private String cookie;
    private String user_id;

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

        books.add(new Book());
        books.add(new Book());books.add(new Book());


        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        bookAdapter = new BookAdapter(books, cookie, user_id);
        recyclerView.setAdapter(bookAdapter);
        recyclerView.setEmptyView(emptyView);
    }

    private void initView() throws JSONException {
        SharedPreferences sp = getSharedPreferences("login", 0);
        cookie = sp.getString("cookie", null);
        user_id = sp.getString("username", null);

        emptyView = findViewById(R.id.iv_emptyView);
        recyclerView = findViewById(R.id.recyclerview);

//        Intent intent = getIntent();
//        result = intent.getStringExtra("result");
//        assert result != null;
//        JSONObject jsonObject = new JSONObject(result);
//        JSONObject data = jsonObject.getJSONObject("data");
//        JSONArray bookarray = data.getJSONArray("result");
//        int nums = data.getInt("nums");
//
//        Gson gson = new Gson();
//        for (int i = 0; i < nums; i ++){
//            books.add(gson.fromJson(bookarray.get(i).toString(), Book.class));
//        }
    }

    private void initListener(){
    }
}