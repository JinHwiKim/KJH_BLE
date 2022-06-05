package com.kjh.ble.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kjh.ble.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Registration_success extends AppCompatActivity {
    static Context registration_success_context;
    private DBHelper dbHelper;

    private ArrayList<Device> device_array;
    private ArrayList<SOS> sos_array = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_registration_success);

        registration_success_context = this;

        dbHelper = new DBHelper(getApplicationContext(), "db", null, 1);
        dbHelper.getDB();

        sos_array = dbHelper.getAllSOS();
        device_array = dbHelper.getAllDeviceData();
        Collections.reverse(device_array);

        findViewById(R.id.registration_success_btn).setOnClickListener(btn_click);
    }

    Button.OnClickListener btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((MainActivity)MainActivity.mainContext).finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }
    };

}
