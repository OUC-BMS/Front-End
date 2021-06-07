package com.example.ibook.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibook.R;
import com.example.ibook.bean.Book;
import com.example.ibook.bean.History;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.ibook.ResultActivity.JSON;

public class UserBookAdapter extends RecyclerView.Adapter<UserBookAdapter.ViewHolder>{

    private List<History> list;
    private Context context;
    private String cookie;


    public UserBookAdapter(List<History> list,Context context){
        this.list = list;
        this.context = context;
    }

    public UserBookAdapter(List<History> list, String cookie) {
        this.list = list;
        this.cookie = cookie;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_borrowTime;
        TextView tv_returnTime;
        TextView tv_status;
        View userBookView;
        Button btn_return;


        public ViewHolder(View view) {
            super(view);
            userBookView = view;
            btn_return = view.findViewById(R.id.btn_return);
            tv_borrowTime = view.findViewById(R.id.tv_borrowTime);
            tv_returnTime = view.findViewById(R.id.tv_returnTime);
            tv_title = view.findViewById(R.id.tv_title);
            tv_status = view.findViewById(R.id.tv_status);
//            checkBox = view.findViewById(R.id.cb_mission);
//            tv_title = view.findViewById(R.id.tv_title);
//            tv_time = view.findViewById(R.id.tv_time);
//            relativeLayout = view.findViewById(R.id.relativeLayout);
//            iv_checked = view.findViewById(R.id.iv_checked);
        }
    }

    @NonNull
    @Override
    public UserBookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null){
            context = viewGroup.getContext();
        }
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.item_userbook, viewGroup, false);
        final UserBookAdapter.ViewHolder holder = new UserBookAdapter.ViewHolder(view);
        holder.userBookView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                History history = list.get(position);
            }
        });

        holder.btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                History history = list.get(position);
                returnBook(history, view);
                if(history.getStatus() == 0){
                    Toast.makeText(view.getContext(), "已取消预约", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(view.getContext(), "已归还", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //添加：
        holder.userBookView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                History history = list.get(position);
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
    public void onBindViewHolder(@NonNull UserBookAdapter.ViewHolder viewHolder, int i) {
        History history = list.get(i);
        viewHolder.tv_title.setText(history.getBook_name());
        viewHolder.tv_borrowTime.setText("借阅时间：" + history.getBorrow_time());
        viewHolder.tv_returnTime.setText("");
        switch (history.getStatus()){
            case 0:
                viewHolder.tv_status.setText("状态：预约");
                break;
            case 1:
                viewHolder.tv_status.setText("状态：未还");
                break;
            case 2:
                viewHolder.tv_status.setText("状态：已还");
                break;
        }
//        if (mission.isFinished()){
//            viewHolder.iv_checked.setVisibility(View.VISIBLE);
//            viewHolder.checkBox.setChecked(true);
//            viewHolder.tv_time.setVisibility(View.GONE);
//            viewHolder.tv_title.setTextColor(Color.rgb(186,186,186));
//            viewHolder.tv_title.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); // 中划线
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

    private UserBookAdapter.onSwipeListener mOnSwipeListener;

    public UserBookAdapter.onSwipeListener getOnDelListener() {
        return mOnSwipeListener;
    }

    public void setOnDelListener(UserBookAdapter.onSwipeListener mOnDelListener) {
        this.mOnSwipeListener = mOnDelListener;
    }

    public void addData(int position,History history, BookAdapter.ViewHolder holder) {
        list.add(position, history);
        //添加动画
        notifyItemInserted(position);
        notifyItemRangeChanged(position,1);
    }

    // 删除数据
    public void removeData(int position) {
        list.remove(position);
        //删除动画
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,1);
    }

    private void returnBook(final History history, final View view){
        //开启现线程发起网络请求
        new Thread(new Runnable(){
            @Override
            public void run(){
                JSONObject json = new JSONObject();
                try {
                    json.put("book_id", history.getBook_id());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                final Request request = new Request.Builder()
                        .url("http://139.199.84.147/api/book/return")
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

                            if(code == 20000){
                                if(history.getStatus() == 0){
                                    Toast.makeText(view.getContext(), "已取消预约", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(view.getContext(), "已归还", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                                Toast.makeText(view.getContext(), "归还失败", Toast.LENGTH_SHORT).show();
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
