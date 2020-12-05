package com.example.ibird;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ibird.bean.RecoResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ms.banner.Banner;
import com.ms.banner.BannerConfig;
import com.ms.banner.Transformer;
import com.ms.banner.holder.BannerViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity {

    private PromptDialog promptDialog;
    private String code;
    private String path;
    private Button btn_return;
    private Button btn_upload;

    private String picPath;
    private JSONObject data;
    private JSONArray result;
    private List<RecoResult> list = new ArrayList<>();
    private Banner banner;
    private RelativeLayout rl_root;
    private View rootview;
    private int lastPosition = 0;
    LinearLayout indicator;
    private int mIndicatorSelectedResId = R.drawable.indicator;
    private int mIndicatorUnselectedResId = R.drawable.indicator2;
    private List<ImageView> indicatorImages = new ArrayList<>();

    public static final MediaType JSON= MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        rootview = LayoutInflater.from(this).inflate(R.layout.banner_item, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        Intent intent = getIntent();
        code = intent.getStringExtra("status");
        path = intent.getStringExtra("path");
        Log.e("传过来的path", path);

        init();


    }

    public void init(){
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btn_upload = findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        banner = findViewById(R.id.banner);
        indicator = (LinearLayout) findViewById(R.id.indicator);
        indicatorImages.clear();
        promptDialog = new PromptDialog(this);

        promptDialog.showLoading("正在识别");
        for (int i = 0; i < 5; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams custom_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            custom_params.leftMargin = 2;
            custom_params.rightMargin = 2;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            indicatorImages.add(imageView);
            indicator.addView(imageView, custom_params);
        }

        new Thread(runnable).start();
        banner.setAutoPlay(false)
                .setPages(list, new CustomViewHolder(list, rootview))
                .setBannerStyle(BannerConfig.NOT_INDICATOR)
                .setBannerAnimation(Transformer.Scale)
                .start();
        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicatorImages.get((lastPosition + 5) % 5).setImageResource(mIndicatorUnselectedResId);
                indicatorImages.get((position + 5) % 5).setImageResource(mIndicatorSelectedResId);
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.e("TAG","请求结果:" + val);
        }
    };

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            recoPic();

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value","请求结果");
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    public void recoPic() {
        JSONObject json = new JSONObject();
        try {
            json.put("path", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("https://weparallelines.top/api/prediction/predict")
                //.addHeader("Cookie", cookie)
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
                    data = jsonObject.getJSONObject("data");
                    result = data.getJSONArray("result");

                    list.add(gson.fromJson(result.get(0).toString(), RecoResult.class));
                    list.add(gson.fromJson(result.get(1).toString(), RecoResult.class));
                    list.add(gson.fromJson(result.get(2).toString(), RecoResult.class));
                    list.add(gson.fromJson(result.get(3).toString(), RecoResult.class));
                    list.add(gson.fromJson(result.get(4).toString(), RecoResult.class));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            promptDialog.showSuccess("识别完成");
                            banner.update(list);
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

    class CustomViewHolder implements BannerViewHolder<RecoResult> {

        private TextView tv_bird;
        private TextView tv_possibility;
        private ImageView iv_bird;
        private List<RecoResult> list;
        private View rootview;

            public CustomViewHolder(List<RecoResult> list, View rootview) {
                this.list = list;
                this.rootview = rootview;
            }

            @Override
            public View createView(Context context, int position, RecoResult data) {
            @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.banner_item, null);
            tv_bird = view.findViewById(R.id.tv_bird);
            tv_possibility = view.findViewById(R.id.tv_possibility);
            iv_bird = view.findViewById(R.id.iv_bird);

            tv_possibility.setText(String.format("%.2f", list.get(position).getProbability() * 100) + "%");
            tv_bird.setText(list.get(position).getLabel());
            String url = "https://weparallelines.top/birds/" + list.get(position).getId() + ".jpg";

            Glide.with(ResultActivity.this).load(url).into(iv_bird);
            return view;
        }
    }
}