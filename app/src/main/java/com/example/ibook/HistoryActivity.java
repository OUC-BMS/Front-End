package com.example.ibook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.ibook.adapter.BookAdapter;
import com.example.ibook.adapter.HistoryAdapter;
import com.example.ibook.bean.Book;
import com.example.ibook.bean.History;
import com.example.ibook.view.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;

public class HistoryActivity extends AppCompatActivity {

    private EmptyRecyclerView recyclerView;
    private List<History> histories = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private Toolbar toolbar;
    private View emptyView;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
        initListener();

        histories.add(new History());
        histories.add(new History());
        histories.add(new History());
        histories.add(new History());
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        historyAdapter = new HistoryAdapter(histories);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setEmptyView(emptyView);
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
}