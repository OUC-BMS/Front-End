package com.example.ibook.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
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

import java.io.Serializable;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private List<History> list;
    private Context context;


    public HistoryAdapter(List<History> list, Context context){
        this.list = list;
        this.context = context;
    }

    public HistoryAdapter(List<History> list) {
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_returnTime;
        TextView tv_borrowTime;
        View historyView;
        Button btn_borrow;


        public ViewHolder(View view) {
            super(view);
            historyView = view;
            btn_borrow = view.findViewById(R.id.btn_borrow);
//            checkBox = view.findViewById(R.id.cb_mission);
            tv_title = view.findViewById(R.id.tv_title);
            tv_borrowTime = view.findViewById(R.id.tv_borrowTime);
            tv_returnTime = view.findViewById(R.id.tv_returnTime);
//            tv_time = view.findViewById(R.id.tv_time);
//            relativeLayout = view.findViewById(R.id.relativeLayout);
//            iv_checked = view.findViewById(R.id.iv_checked);
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null){
            context = viewGroup.getContext();
        }
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.item_history, viewGroup, false);
        final HistoryAdapter.ViewHolder holder = new HistoryAdapter.ViewHolder(view);
        holder.historyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                History history = list.get(position);
            }
        });

        //添加：
        holder.historyView.setOnLongClickListener(new View.OnLongClickListener() {
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
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder viewHolder, int i) {
        History history = list.get(i);
        viewHolder.tv_title.setText(history.getBook_name());
        viewHolder.tv_borrowTime.setText("借阅时间：" + history.getBorrow_time());
        viewHolder.tv_returnTime.setText("归还时间：" + history.getGiveback_time());
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

    public void addData(int position,History history, HistoryAdapter.ViewHolder holder) {
        list.add(position,history);
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
}
