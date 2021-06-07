package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ManagerActivity extends AppCompatActivity {

    CardView cv_search;
    CardView cv_addbook;
    TextView tv_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        initView();
        initListener();
    }
    private void initView(){
        cv_search = findViewById(R.id.cv_search);
        cv_addbook = findViewById(R.id.cv_addbook);
        tv_username = findViewById(R.id.tv_username);

        SharedPreferences sp = getSharedPreferences("login", 0);
        //String avatar = sp.getString("avatar", null);
        String username = sp.getString("sname", null);
        tv_username.setText(username);

    }

    private void initListener(){
        cv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerActivity.this, SearchActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        cv_addbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerActivity.this, NewBookActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}