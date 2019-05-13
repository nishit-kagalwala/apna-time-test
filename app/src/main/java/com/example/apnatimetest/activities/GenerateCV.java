package com.example.apnatimetest.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apnatimetest.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import AppParams.AppVariables;
import AppParams.PreferenceKV;
import dbhelper.TableContract;
import model.User;
import utilities.Utility;

public class GenerateCV extends AppCompatActivity {

    /*The main functions of this activity are :
    * 1) Generating a dummy CV Format
    * 2) Saving the CV as a JPEG
    * 3) Uploading all CV text data to Firebase
    * */


    private ImageView ivPhoto;
    private TextView tvName, tvAddress, tvDOB, tvExp, tvNumber;
    private Bitmap bitmap;
    private View rootView;
    private Handler handler;
    private Runnable r;
    private SharedPreferences sharedPreferences;
    private DatabaseReference mDatabase;
    User user;

    private String path = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_generate_cv);
        getSupportActionBar().hide();

        sharedPreferences = Utility.getSharedPreferences(GenerateCV.this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivPhoto = findViewById(R.id.iv_gen_cv_photo);
        tvName = findViewById(R.id.tv_gen_cv_name);
        tvAddress = findViewById(R.id.tv_gen_cv_address);
        tvDOB = findViewById(R.id.tv_gen_cv_dob);
        tvExp = findViewById(R.id.tv_gen_cv_exp);
        tvNumber = findViewById(R.id.tv_gen_cv_number);

        user = new User();

        setImage();
        setTextData();
        addDataToFirebase();


        /*In Runnable as View in rendered later and screenshot is captured before giving NPE*/
        if(!sharedPreferences.getBoolean(PreferenceKV.KEY_IS_CV_GENERATED,PreferenceKV.DEF_IS_CV_GENERATED)) {
            rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            handler = new Handler();
            r = new Runnable() {
                public void run() {
                    if (rootView.getWidth() > 0 && rootView.getHeight() > 0) {
                        bitmap = getScreenShot(rootView);
                    }
                }
            };
            handler.postDelayed(r, 1000);
        }
    }

    /*Setup profile Image*/
    private void setImage(){

        String fileName = "/" + AppVariables.imageDirectoryName + AppVariables.cropImageName;
        String filePath = path + fileName;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        ivPhoto.setImageBitmap(bitmap);


    }

    /*Fetch user data from sqlite and render it on UI*/
    private void setTextData(){

        Cursor cursor = getContentResolver().query(TableContract.UserEntry.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            user.setFullName(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_FULL_NAME)));
            user.setNumber(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_PHONE_NUMBER)));
            user.setAddress(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_ADDRESS)));
            user.setExperience(cursor.getInt(cursor.getColumnIndex(TableContract.UserEntry.USER_EXPERIENCE)));
            user.setDob(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_DOB)));
        }

        tvName.setText(tvName.getText().toString() + " : " + user.getFullName());
        tvAddress.setText(tvAddress.getText().toString() + " : " + user.getAddress());
        tvDOB.setText(tvDOB.getText().toString() + " : " + user.getDob());
        tvExp.setText(tvExp.getText().toString() + " : " + user.getExperience());
        tvNumber.setText(tvNumber.getText().toString() + " : " + user.getNumber());
    }

    /*Save the CV in gallery as an image*/
    private void saveImage(Bitmap bitmap){
        File mfile = new File(path, AppVariables.cvImagePathFileName);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
        byte[] bitmapdata = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

        try{
            OutputStream os = new FileOutputStream(mfile);
            byte[]data = new byte[bis.available()];
            bis.read(data);
            os.write(data);
            bis.close();
            os.close();

            sharedPreferences.edit().putBoolean(PreferenceKV.KEY_IS_CV_GENERATED,true).commit();
            addDataToFirebase();

            Toast.makeText(GenerateCV.this, getResources().getString(R.string.cv_generated), Toast.LENGTH_SHORT).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*Take screenshot as a CV Image*/
    private  Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        handler.removeCallbacks(r);
        saveImage(bitmap);
        return bitmap;
    }

    /*Add user data to firebase*/
    private void addDataToFirebase(){
        mDatabase.child("user").child(user.getNumber()).setValue(user);
    }
}
