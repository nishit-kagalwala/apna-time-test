package com.example.apnatimetest.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.example.apnatimetest.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import AppParams.PreferenceKV;
import utilities.Utility;

public class MainActivity extends AppCompatActivity {
    /*Main Activity Will Be Performing Following Functions :
    * 1) Selection Of Language
    * 2) Serve as launcher Activity
    * */


    /*Get UI Elements*/
    private Button buttonEnglish;
    private Button buttonHindi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Initialize UI Elements*/
        buttonEnglish = findViewById(R.id.btn_lang_english);
        buttonHindi = findViewById(R.id.btn_lang_hindi);


        /*Set onclick listener for hindi button. It changes the locale configuration and sets app language to hindi*/
        buttonHindi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("hi");
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);


            }
        });

        /*Set onclick listener for english button. It changes the locale configuration and sets app language to english*/
        buttonEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("en");
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });


    }


    /*Set Locale function gets the locale configuration and sets the language of the app to the desired param universally*/
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //Intent refresh = new Intent(this, MainActivity.class);
        //startActivity(refresh);
        //finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseApp.initializeApp(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ResumeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }else if(Utility.getSharedPreferences(MainActivity.this).getBoolean(PreferenceKV.KEY_IS_CV_GENERATED,PreferenceKV.DEF_IS_CV_GENERATED)){
            startActivity(new Intent(MainActivity.this,ResumeActivity.class));
        }
    }

}
