package com.example.ibook.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class CheckNetUtil {

    private Context context;

    public CheckNetUtil(Context context) {
        this.context = context;
    }

    public boolean initNet() {
        if (isNet())
            return true;
        else {
            Toast.makeText(context, "没有网络哦！", Toast.LENGTH_SHORT).show();

            return false;
        }
    }

    private boolean isNet() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

}
