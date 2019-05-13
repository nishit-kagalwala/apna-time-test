package com.example.apnatimetest.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.apnatimetest.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity  {

    private EditText etMobileNumber;
    private Button btnLogin;



    /*The functions of this activity are :
    * 1) Take and user phone number and submit to OTP activity for verification
    * 2) Getting runtime permissions for camera and storage
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //FirebaseApp.initializeApp(this);

        etMobileNumber = findViewById(R.id.et_login_number);
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Check for permissions
                String[] PERMISSIONS = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,

                };

                if(hasPermissions(LoginActivity.this,PERMISSIONS)) {
                    String countryCode = "+91";
                    String number = etMobileNumber.getText().toString().trim();

                    if (number.isEmpty() || number.length() < 10) {
                        etMobileNumber.setError(getResources().getString(R.string.error_number));
                        etMobileNumber.requestFocus();
                        return;
                    }

                    String phoneNo = countryCode + number;

                    Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                    intent.putExtra("phonenumber", phoneNo);
                    startActivity(intent);
                }else {
                    requestPermissions();
                }


            }
        });
    }


    /*Requests for permissions required*/
    private void requestPermissions(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,

        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    /*Checks if all necessary permissions are granted*/
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
