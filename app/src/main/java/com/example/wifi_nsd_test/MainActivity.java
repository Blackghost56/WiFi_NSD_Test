package com.example.wifi_nsd_test;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    private NSDHelper nsdHelper;
    private TextView textView;
    private TextView textViewIp;
    private Button buttonDiscovery;
    private Button buttonClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        textViewIp = findViewById(R.id.textViewIP);
        buttonDiscovery = findViewById(R.id.buttonDiscovery);
        buttonDiscovery.setOnClickListener(v -> {
            nsdHelper.tearDown();
            nsdHelper.discoverServices();
        });
        buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(v -> {
            textView.setText("");
        });

        nsdHelper = new NSDHelper(this);
        nsdHelper.registerCallback(new NSDHelper.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onServiceFound(NsdServiceInfo service) {
                String name = service.getServiceName();
                new Handler(Looper.getMainLooper()).post(() -> {
                    textView.setText(textView.getText() + "\n" + name);
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                String ip = serviceInfo.getHost().getCanonicalHostName();
                new Handler(Looper.getMainLooper()).post(() -> {
                    textViewIp.setText("IP: " + ip);
                });
            }
        });
//        nsdHelper.initializeDiscoveryListener();
//        nsdHelper.initializeResolveListener();

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, permission, 0);
            requestMultiplePermission.launch(permission);
        } else {
            permissionIsGranted();
        }

    }


    private void permissionIsGranted(){
        // Todo

    }


    @Override
    protected void onPause() {
        if (nsdHelper != null) {
            nsdHelper.tearDown();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nsdHelper != null) {
            //nsdHelper.registerService(connection.getLocalPort());
            nsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        nsdHelper.tearDown();
        //connection.tearDown();
        super.onDestroy();
    }





    String[] permission = new String[] {Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.ACCESS_WIFI_STATE
            , Manifest.permission.CHANGE_WIFI_STATE
            , Manifest.permission.INTERNET
            , Manifest.permission.CHANGE_NETWORK_STATE
            , Manifest.permission.ACCESS_COARSE_LOCATION
    };
    ActivityResultLauncher<String[]> requestMultiplePermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

        Log.d(TAG, "requestMultiplePermission result");
        boolean flag = true;
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (!entry.getValue()) {
                Log.e(TAG, "Permission is not granted: " + entry.getKey() + "    value: " + entry.getValue());
                flag = false;
                break;
            }
        }

            if (flag) {
                permissionIsGranted();
            } else {
                Log.e(TAG, "Permissions is not granted");
            }
    });




}