package com.example.ibook.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibook.EditBookActivity;
import com.example.ibook.LoginActivity;
import com.example.ibook.MainActivity;
import com.example.ibook.NewBookActivity;
import com.example.ibook.R;
import com.example.ibook.ResultActivity;
import com.example.ibook.SearchActivity;
import com.example.ibook.bean.Book;
import com.example.ibook.util.CheckNetUtil;
import com.google.gson.Gson;
import com.hb.dialog.myDialog.MyAlertDialog;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.ibook.ResultActivity.JSON;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder>{

    private List<Book> list;
    private Context context;
    private String cookie;
    private String user_id;
    private boolean isManager;


    public BookAdapter(List<Book> list,Context context){
        this.list = list;
        this.context = context;
    }

    public BookAdapter(List<Book> list, String cookie, String user_id, boolean isManager) {
        this.list = list;
        this.cookie = cookie;
        this.user_id = user_id;
        this.isManager = isManager;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        View missionView;
        Button btn_borrow;
        TextView tv_author;
        TextView tv_available;


        public ViewHolder(View view) {
            super(view);
            missionView = view;
            btn_borrow = view.findViewById(R.id.btn_borrow);
            tv_author = view.findViewById(R.id.tv_author);
            tv_available = view.findViewById(R.id.tv_available);
            tv_title = view.findViewById(R.id.tv_title);
//            checkBox = view.findViewById(R.id.cb_mission);
//            tv_title = view.findViewById(R.id.tv_title);
//            tv_time = view.findViewById(R.id.tv_time);
//            relativeLayout = view.findViewById(R.id.relativeLayout);
//            iv_checked = view.findViewById(R.id.iv_checked);
        }
    }

    @NonNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null){
            context = viewGroup.getContext();
        }
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.item_searchresult, viewGroup, false);
        final BookAdapter.ViewHolder holder = new BookAdapter.ViewHolder(view);
        holder.missionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Book book = list.get(position);
            }
        });

        holder.btn_borrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                final Book book = list.get(position);
                if(!isManager){
                    if(book.getAvailable() > 0){
                        CheckNetUtil checkNetUtil = new CheckNetUtil(context);
                        Log.e("????????????????????????", "ok");
                        if (checkNetUtil.initNet()) {
                            sendRequestWithOkHttp(position,book,view,holder);
                        }
                    }
                    else {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context).builder()
                                .setTitle("???????????????????????????")
                                .setMsg("?????????????????????")
                                .setPositiveButton("??????", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CheckNetUtil checkNetUtil = new CheckNetUtil(context);
                                        Log.e("????????????????????????", "ok");
                                        if (checkNetUtil.initNet()) {
                                            evaluateDialog(position,book,view,holder);
                                        }
                                    }
                                }).setNegativeButton("??????", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                                });
                        myAlertDialog.show();
                    }
                }
                else {
                    Intent intent = new Intent(context, EditBookActivity.class);
                    intent.putExtra("book", book);
                    context.startActivity(intent);
                }
            }
        });

        //?????????
        holder.missionView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                Book book = list.get(position);
//                book.setSelected(true);
//                Intent intent = new Intent(context, BatchOperationActivity.class);
//                intent.putExtra("mList", (Serializable)list);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdapter.ViewHolder viewHolder, int i) {
        Book book = list.get(i);
        viewHolder.tv_title.setText(book.getBook_name());
        viewHolder.tv_available.setText("?????????" + book.getAvailable());
        viewHolder.tv_author.setText(book.getAuthor());
        if(isManager) viewHolder.btn_borrow.setText("??????");
//        if (mission.isFinished()){
//            viewHolder.iv_checked.setVisibility(View.VISIBLE);
//            viewHolder.checkBox.setChecked(true);
//            viewHolder.tv_time.setVisibility(View.GONE);
//            viewHolder.tv_title.setTextColor(Color.rgb(186,186,186));
//            viewHolder.tv_title.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); // ?????????
//        }else {
//            viewHolder.iv_checked.setVisibility(View.INVISIBLE);
//            viewHolder.checkBox.setChecked(false);
//            viewHolder.tv_time.setVisibility(View.VISIBLE);
//            viewHolder.tv_title.setTextColor(Color.BLACK);
//            viewHolder.tv_title.getPaint().setFlags(0);
//        }
    }

    @Override
    public int getItemCount() {
        if(null==list) return 0;
        else return list.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface onSwipeListener {
        void onDel(int pos);

    }

    private BookAdapter.onSwipeListener mOnSwipeListener;

    public BookAdapter.onSwipeListener getOnDelListener() {
        return mOnSwipeListener;
    }

    public void setOnDelListener(BookAdapter.onSwipeListener mOnDelListener) {
        this.mOnSwipeListener = mOnDelListener;
    }

    public void addData(int position,Book book, BookAdapter.ViewHolder holder) {
        list.add(position,book);
        //????????????
        notifyItemInserted(position);
        notifyItemRangeChanged(position,1);
    }

    // ????????????
    public void removeData(int position) {
        list.remove(position);
        //????????????
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,1);
    }

    private void sendRequestWithOkHttp(int position, final Book book, final View view, BookAdapter.ViewHolder holder){
        //?????????????????????????????????
        new Thread(new Runnable(){
            @Override
            public void run(){
                JSONObject json = new JSONObject();
                try {
                    json.put("book_id", book.getBook_id());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                final Request request = new Request.Builder()
                        .url("http://139.199.84.147/api/book/checkout")
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
                            Looper.prepare();
                            if(code == 46000)

                                Toast.makeText(view.getContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
                            else if(code == 20000)
                                Toast.makeText(view.getContext(), "????????????", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(view.getContext(), "????????????", Toast.LENGTH_SHORT).show();
                            Looper.loop();
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
    private static String JSONTokener(String in) {
        // consume an optional byte order mark (BOM) if it exists
        if (in != null && in.startsWith("\ufeff")) {
            in = in.substring(1);
        }
        return in;
    }

    public void evaluateDialog(int position, final Book book, final View view, BookAdapter.ViewHolder holder) {
        //?????????????????????????????????
        new Thread(new Runnable(){
            @Override
            public void run(){
                JSONObject json = new JSONObject();
                try {
                    json.put("book_id", book.getBook_id());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                final Request request = new Request.Builder()
                        .url("http://139.199.84.147/api/book/appointment")
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
                            Looper.prepare();

                            if(code == 20000)
                                Toast.makeText(view.getContext(), "????????????", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(view.getContext(), "????????????", Toast.LENGTH_SHORT).show();

                            Looper.loop();
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