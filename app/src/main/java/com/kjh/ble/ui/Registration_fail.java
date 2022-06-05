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
import android.util.DisplayMetrics;
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
import java.util.List;
import java.util.Locale;

public class Registration_fail extends AppCompatActivity {
    int device_kind;
    String device_nickname;
    static Context registration_fail_context;

    private ImageView fail_img;
    private ImageView fail_img1;
    private ImageView fail_img2;
    private ImageView fail_img3;

    static final String FILE_LANGUAGE = "trackerlanguage.dat";
    String languageToLoad  = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration_fail);

        TextView registration_fail_text1 = (TextView)findViewById(R.id.registration_fail_text1);
        TextView registration_fail_text2 = (TextView)findViewById(R.id.registration_fail_text2);
        Button registration_fail_re_btn = (Button)findViewById(R.id.registration_fail_re_btn);
        Button registration_fail_cancel_btn = (Button)findViewById(R.id.registration_fail_cancel_btn);

        fail_img = (ImageView)findViewById(R.id.registration_fail_img);
        fail_img1 = (ImageView)findViewById(R.id.registration_fail_img1);
        fail_img2 = (ImageView)findViewById(R.id.registration_fail_img2);
        fail_img3 = (ImageView)findViewById(R.id.registration_fail_img3);

        registration_fail_context = this;

        Intent getintent = getIntent();
        device_kind = getintent.getIntExtra("device_kind",0);
        String error_code = getintent.getStringExtra("error");
        device_nickname = getintent.getStringExtra("device_name");


        findViewById(R.id.registration_fail_cancel_btn).setOnClickListener(cancel_btn);
        findViewById(R.id.registration_fail_re_btn).setOnClickListener(re_btn);
    }


    @Override
    protected  void onStart(){
        super.onStart();
    }

    Button.OnClickListener re_btn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(), Registration.class).putExtra("device_kind",device_kind).putExtra("device_nickname",device_nickname));

            finish();
        }
    };

    Button.OnClickListener cancel_btn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    public void registration_cancel(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));

        finish();
    }



    //홈키 처리
    @Override public void onPause() {
        if (isApplicationSentToBackground(this)){
            finish();
            try{
                ((MainActivity)MainActivity.mainContext).finish();
            }catch(Exception e){

            }
        }
        super.onPause();

    }

    //홈키 처리
    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}