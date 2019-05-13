package com.example.apnatimetest.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.apnatimetest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import AppParams.PreferenceKV;
import dbhelper.TableContract;
import model.User;
import utilities.Utility;

public class OTPActivity extends AppCompatActivity {

    private String verificationid;
    private FirebaseAuth mAuth;
    //private ProgressBar progressBar;
    private EditText etOTP;
    private Button btnVerifyOTP;
    private String phoneNumber;
    private DatabaseReference mDatabase;


    /*The main functions of this activity are :
    * 1) Verify user based on mobile number
    * 2) Once user is verified, creating a sqlite object of the user
    * 3) Once user is verified, creating a firebase child with the phone number
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Get firebase auth instance required for verification
        mAuth = FirebaseAuth.getInstance();

        //progressBar = findViewById(R.id.progressbar);
        etOTP = findViewById(R.id.et_otp);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);

        phoneNumber = getIntent().getStringExtra("phonenumber");

        //Send to Firebase to get OTP
        sendVerificationCode(phoneNumber);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etOTP.getText().toString().trim();
                if ((code.isEmpty() || code.length() < 6)){
                    etOTP.setError(getResources().getString(R.string.otp_error));
                    etOTP.requestFocus();
                    return;
                }
                //progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });
    }

    private void verifyCode(String code){
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid, code);
            signInWithCredential(credential);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //Insert Phone Number Into DB
                            OTPActivity.this.getContentResolver().insert(TableContract.UserEntry.CONTENT_URI, Utility.generateUserPhoneNoCV(phoneNumber));

                            mDatabase = FirebaseDatabase.getInstance().getReference("user");
                            DatabaseReference userRef = mDatabase.child(phoneNumber);

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getValue() != null){
                                        User u = dataSnapshot.getValue(User.class);
                                        if(u.getFullName() !=null && u.getFullName().length() >0){
                                            ContentValues cv = Utility.userModelToContentValue(u);
                                            getContentResolver().update(TableContract.UserEntry.CONTENT_URI,cv,null,null);
                                        }
                                    }else{
                                        Log.d("Nishit","No snapshot found");
                                        User user = new User();
                                        user.setNumber(phoneNumber);
                                        mDatabase = FirebaseDatabase.getInstance().getReference();
                                        mDatabase.child("user").child(phoneNumber).setValue(user);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("Nishit","No snapshot found");
                                }
                            });




                            Utility.getSharedPreferences(OTPActivity.this).edit().putString(PreferenceKV.USER_NUM,phoneNumber).commit();

                            Intent intent = new Intent(OTPActivity.this, ResumeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //progressBar.setVisibility(View.GONE);
                            startActivity(intent);

                        } else {
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(OTPActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }

    private void sendVerificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.d("Nishit","Code Sent");
            verificationid = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.d("Nishit","Verification Complete");
            String code = phoneAuthCredential.getSmsCode();

            if (code != null){
                //progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d("Nishit","Verification Failed");
            Toast.makeText(OTPActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };
}

