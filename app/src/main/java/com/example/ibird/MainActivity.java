package com.example.ibird;

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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibird.util.CheckNetUtil;
import com.pedaily.yc.ycdialoglib.dialog.select.CustomSelectDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.gemus.openingstartanimation.NormalDrawStrategy;
import site.gemus.openingstartanimation.OpeningStartAnimation;
import site.gemus.openingstartanimation.RedYellowBlueDrawStrategy;

public class MainActivity extends AppCompatActivity {

    private Button btn_choosePic;
    private LinearLayout btn_login;
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
        Request();


        Resources resources = MainActivity.this.getResources();
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = resources.getDrawable(R.drawable.ibird_logo3);
        OpeningStartAnimation openingStartAnimation = new OpeningStartAnimation.Builder(this)
                .setDrawStategy(new RedYellowBlueDrawStrategy())
                .setAppIcon(drawable)
                .setAppStatement("没有人比我们更懂鸟")
                .setAnimationFinishTime(400)
                .create();
        openingStartAnimation.show(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init(){
        promptDialog = new PromptDialog(this);
        btn_choosePic = findViewById(R.id.btn_choosePic);
        btn_login = findViewById(R.id.btn_login);

        btn_camera.setListener(new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                picTyTakePhoto();
            }
        });

        btn_photo.setListener(new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                pickPhoto();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btn_choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SelectPicActivity.class);
//                startActivity(intent);
                PromptButton cancle = new PromptButton("取消", null);
                cancle.setTextColor(Color.parseColor("#0076ff"));
                //设置显示的文字大小及颜色
                //promptDialog.getAlertDefaultBuilder().textSize(12).textColor(Color.GRAY);
                //默认两个按钮为Alert对话框，大于三个按钮的为底部SHeet形式展现
                promptDialog.showAlertSheet("", true, cancle,
                        btn_camera, btn_photo);

            }
        });
    }


    /**
     * 拍照获取图片
     */
    private void picTyTakePhoto() {
        //判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
            /***
             * 使用照相机拍照，拍照后的图片会存放在相册中。使用这种方式好处就是：获取的图片是拍照后的原图，
             * 如果不使用ContentValues存放照片路径的话，拍照后获取的图片为缩略图有可能不清晰
             */
            ContentValues values = new ContentValues();
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        } else {
            Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
        }
    }

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, SELECT_PIC_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //从相册取图片，有些手机有异常情况，请注意
            if (requestCode == SELECT_PIC_CODE) {
                if (null != data && null != data.getData()) {
                    photoUri = data.getData();
                    picPath = uriToFilePath(photoUri);
                    File file = new File(picPath);
//                    if (!file.getParentFile().exists()) {
//                        file.getParentFile().mkdirs();
//                    }

                    Uri uri = getImageContentUri(context, file);

                    startPhotoZoom(uri, PHOTO_CROP_CODE);
                } else {
                    Toast.makeText(this, "图片选择失败", Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == TAKE_PHOTO_CODE) {
                String[] pojo = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(photoUri, pojo, null, null, null);
                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                    cursor.moveToFirst();
                    picPath = cursor.getString(columnIndex);
                    if (Build.VERSION.SDK_INT < 14) {
                        cursor.close();
                    }
                }
                if (picPath != null) {
                    photoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(picPath));
                    startPhotoZoom(photoUri, PHOTO_CROP_CODE);
                } else {
                    Toast.makeText(this, "图片选择失败", Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == PHOTO_CROP_CODE) {
                if (photoUri != null) {

                    Intent intent = new Intent(MainActivity.this, TestActivity.class);
                    //intent.putExtra();
                    startActivity(intent);
                    Bitmap bitmap = BitmapFactory.decodeFile(picPath);
                    if (bitmap != null) {
                        CheckNetUtil checkNetUtil = new CheckNetUtil(getApplicationContext());
                        if (checkNetUtil.initNet()) {
                            postImage(picPath);
                        }
                    }
                }
            }
        }
    }

    private void startPhotoZoom(Uri uri, int REQUE_CODE_CROP) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // 去黑边
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例，根据自己情况修改
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高像素
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //取消人脸识别功能
        intent.putExtra("noFaceDetection", true);
        //设置返回的uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //设置为不返回数据
        intent.putExtra("return-data", false);
        startActivityForResult(intent, REQUE_CODE_CROP);
    }

    private String uriToFilePath(Uri uri) {
        //获取图片数据
        String[] proj = {MediaStore.Images.Media.DATA};
        //查询
        Cursor cursor = managedQuery(uri, proj, null, null, null);
        //获得用户选择的图片的索引值
        int image_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        //返回图片路径
        return cursor.getString(image_index);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void Request() {             //获取相机拍摄读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//版本判断
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 1);
            }
        }
    }

    private void postImage(String filePath) {
        Log.e("DetailUserActivity", filePath);
//        if (imagePath != null) {
//            //这里可以上服务器;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        File file = new File(filePath);
        //File file = convertBitmapToFile(bitmap);
        Log.e("PCActivity", "ok1");
        //RequestBody image = RequestBody.create(MediaType.parse("image/png"), convertBitmapToFile(bitmap));
        RequestBody image = RequestBody.create(MediaType.parse("image/png"), file);
        Log.e("DetailFileName", file.getName());
        Log.e("DetailFilePath", file.getPath());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // .addFormDataPart("headImage", imagePath, image)
                .addFormDataPart("img", file.getName(), image)
                .addFormDataPart("usage", "p")
                .build();
        Log.e("PCActivity", "为啥传不上去" + image + "type:" + type + "userId:" + userId);
        final Request request = new Request.Builder()
                .url("http://139.199.84.147/mytieba.api/upload/photo")
                .addHeader("Cookie", cookie)
                .post(requestBody)
                .build();
        Log.e("PCActivity", "ok2");
        okHttpClient.newCall(request).enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DetailUserActivity", "获取数据失败");
                Log.e("DetailUserActivity", String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("DetailresponseData", responseData);
                if (response.isSuccessful()) {
                    try {
                        Log.e("DetailUserActivity", "ok3");

                        Log.e("DetailUserActivity", "ResponseData" + responseData);
                        JSONObject jsonObject = new JSONObject(responseData);
                        statu = jsonObject.getBoolean("status");
                        if (statu) {

                            String pic = "http://139.199.84.147" + jsonObject.getString("pic");
                            SharedPreferences sharedPreferences = getSharedPreferences("theUser", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            if (type.equals("avatar")) {
                                editor.putString("avater", pic);
                                editor.apply();
                            } else {
                                editor.putString("background", pic);
                                editor.apply();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("DetailUserActivity", response.body().string());
                        Log.e("DetailUserActivity", String.valueOf(e));
                    }

                }
            }
        });
    }
}