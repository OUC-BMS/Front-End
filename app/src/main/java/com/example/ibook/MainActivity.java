package com.example.ibook;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ibook.R;
import com.example.ibook.util.CheckNetUtil;
import com.hb.dialog.myDialog.MyAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import site.gemus.openingstartanimation.OpeningStartAnimation;
import site.gemus.openingstartanimation.RedYellowBlueDrawStrategy;

public class MainActivity extends AppCompatActivity {

    private Button btn_choosePic;
    private LinearLayout btn_login;
    private TextView tv_username;
    private ImageView btn_logout;
    private ImageView iv_avatar;
    private PromptDialog promptDialog;
    private PromptButton btn_camera = new PromptButton("拍照", null);
    private PromptButton btn_photo = new PromptButton("相册", null);
    //使用照相机拍照获取图片
    public static final int TAKE_PHOTO_CODE = 1;
    //使用相册中的图片
    public static final int SELECT_PIC_CODE = 2;
    //图片裁剪
    private static final int PHOTO_CROP_CODE = 3;
    //定义图片的Uri
    private Uri photoUri;
    //图片文件路径
    private String picPath;
    private Context context;
    private String cookie;
    private boolean isManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        init();
        //Request();


        Resources resources = MainActivity.this.getResources();
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = resources.getDrawable(R.drawable.icon_book);
        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setDrawStategy(new RedYellowBlueDrawStrategy())
                .setAppIcon(drawable)
                .setAppStatement("没有人比我们更懂图书管理")
                .setAnimationFinishTime(400)
                .create();
        openingStartAnimation.show(this);
    }

    @Override
    protected void onResume() {
        super.onResume();


        SharedPreferences sp = getSharedPreferences("login", 0);
        //String avatar = sp.getString("avatar", null);
        String username = sp.getString("sname", null);
        isManager = sp.getBoolean("isManager", false);
        cookie = sp.getString("cookie", null);
        if (username != null){
            //Glide.with(MainActivity.this).load("https://weparallelines.top" + avatar).into(iv_avatar);
            tv_username.setText(username);
            btn_logout.setVisibility(View.VISIBLE);
        }else btn_logout.setVisibility(View.INVISIBLE);
        if(cookie != null){
            CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
            Log.e("检查网络状态结束", "ok");
            if (checkNetUtil.initNet()) {
                //new Thread(runnable).start();
            }
        }

    }

    private void init(){
        promptDialog = new PromptDialog(this);
        btn_choosePic = findViewById(R.id.btn_choosePic);
        btn_login = findViewById(R.id.btn_login);
        iv_avatar = findViewById(R.id.iv_user);
        tv_username = findViewById(R.id.tv_username);
        btn_logout = findViewById(R.id.btn_logout);

        SharedPreferences sp = getSharedPreferences("login", 0);
        //String avatar = sp.getString("avatar", null);
        final String username = sp.getString("sname", null);
        cookie = sp.getString("cookie", null);
        isManager = sp.getBoolean("isManager", false);
        if (username != null){
            //Glide.with(MainActivity.this).load("https://weparallelines.top" + avatar).into(iv_avatar);
            tv_username.setText(username);
            tv_username.setClickable(false);
            btn_logout.setVisibility(View.VISIBLE);
        }else btn_logout.setVisibility(View.INVISIBLE);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyAlertDialog myAlertDialog = new MyAlertDialog(context).builder()
                                .setTitle("退出")
                                .setMsg("确定要退出登录吗？")
                                .setPositiveButton("确认", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        SharedPreferences sp = getSharedPreferences("login", 0);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("username", null);
                                        editor.putString("password", null);
                                        editor.putString("avatar", null);
                                        editor.putString("cookie", null);
                                        editor.putBoolean("isManager", false);
                                        editor.putString("sname", null);
                                        editor.commit();

                                        Toast.makeText(context, "已退出", Toast.LENGTH_SHORT).show();

                                        tv_username.setText("登录");
                                        btn_logout.setVisibility(View.INVISIBLE);
                                        Glide.with(MainActivity.this).load(R.drawable.avater3).into(iv_avatar);
                                        tv_username.setClickable(true);
                                    }
                                }).setNegativeButton("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                        myAlertDialog.show();
                    }
                });
            }
        });

        tv_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btn_choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username != null){
                    if(!isManager){
                        Intent intent = new Intent(MainActivity.this, UserActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
                        startActivity(intent);
                    }
                }
                else runOnUiThread(new Runnable() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "您还没有登录", Toast.LENGTH_SHORT);
                    }
                });
            }
        });

        if (cookie != null){
            CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
            Log.e("检查网络状态结束", "ok");
            if (checkNetUtil.initNet()) {
                //new Thread(runnable).start();
            }
        }
    }
}