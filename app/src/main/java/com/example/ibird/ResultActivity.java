package com.example.ibird;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ibird.bean.RecoResult;
import com.example.ibird.util.CheckNetUtil;
import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
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
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity implements OnPermissionCallback {

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

    private static final String GPS_LOCATION_NAME = android.location.LocationManager.GPS_PROVIDER;
    private static final int REQUEST_PRESSMION_CODE = 10000;
    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private LocationManager locationManager;
    private boolean isGpsEnabled;
    private String locateType;
    private PermissionHelper mPermissionHelper;

    private Context context;
    private String cookie;
    double longitude;
    double latitude;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        context = this;
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

    public void init() {

        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        cookie = sharedPreferences.getString("cookie", "");
        //获取定位服务
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启GPS定位功能
        isGpsEnabled = locationManager.isProviderEnabled(GPS_LOCATION_NAME);
        //定位类型：GPS
        locateType = LocationManager.NETWORK_PROVIDER;
        //初始化PermissionHelper
        mPermissionHelper = PermissionHelper.getInstance(this);


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
                getLocation();
                CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                Log.e("检查网络状态结束", "ok");
                if (checkNetUtil.initNet()) {
                    new Thread(runnable2).start();

                }
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
        CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
        Log.e("检查网络状态结束", "ok");
        if (checkNetUtil.initNet()) {
            new Thread(runnable).start();

        } else {
            promptDialog.showError("识别失败");
        }

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
    /**
     * 判断定位服务是否开启
     *
     * @param context 上下文
     * @return true：开启；false：未开启
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void getLocation() {
        Log.e("服务是否开启", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ResultActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mPermissionHelper.request(MULTI_PERMISSIONS);
            return;
        }
        Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        while(location  == null)
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        }
        if (location != null) {
            updateUI(location);
        }
        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
//        locationManager.requestLocationUpdates(locateType, 100,0,
//                locationListener);
    }

    private void updateUI(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        Log.e("经度：", String.valueOf(longitude));
        Log.e("纬度：", String.valueOf(latitude));
    }

    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         *
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(ResultActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
            updateUI(location);
            Log.i("GPS", "时间：" + location.getTime());
            Log.i("GPS", "经度：" + location.getLongitude());
            Log.i("GPS", "纬度：" + location.getLatitude());
            Log.i("GPS", "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         *
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Toast.makeText(ResultActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(ResultActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(ResultActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

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

    Runnable runnable2 = new Runnable(){
        @Override
        public void run() {
            upload();
        }
    };

    public void upload(){
        JSONObject json = new JSONObject();
        try {
            json.put("path", path);
            json.put("longitude", Float.parseFloat(String.valueOf(longitude)));
            json.put("latitude", Float.parseFloat(String.valueOf(latitude)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

        final Request request = new Request.Builder()
                .url("https://weparallelines.top/api/gallery/save_in_gallery")
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

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        //getLocation();
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {

    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {

    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {

    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {

    }

    @Override
    public void onNoPermissionNeeded() {

    }

    class CustomViewHolder implements BannerViewHolder<RecoResult> {

        private TextView tv_bird;
        private TextView tv_possibility;
        private ImageView iv_bird;
        private List<RecoResult> list;
        private View rootview;
        private TextView tv_info;

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
            tv_info = view.findViewById(R.id.tv_info);

            tv_possibility.setText(String.format("%.2f", list.get(position).getProbability() * 100) + "%");
            tv_bird.setText(list.get(position).getLabel());
            tv_info.setText(list.get(position).getInfo());
            String url = "https://weparallelines.top/birds/" + list.get(position).getId() + ".jpg";

            Glide.with(ResultActivity.this).load(url).placeholder(R.drawable.loading3).into(iv_bird);
            return view;
        }
    }
}