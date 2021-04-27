package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class UserActivity extends AppCompatActivity {

    CardView cv_search;
    CardView cv_history;
    CardView cv_userbook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initView();
        initListener();
    }
    private void initView(){
        cv_search = findViewById(R.id.cv_search);
        cv_history = findViewById(R.id.cv_history);
        cv_userbook = findViewById(R.id.cv_userbook);
    }

    private void initListener(){
        cv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, SearchActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        cv_userbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, UserBookActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        cv_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, HistoryActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}