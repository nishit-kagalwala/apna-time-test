package com.example.apnatimetest.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.apnatimetest.BuildConfig;
import com.example.apnatimetest.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import AppParams.AppVariables;
import AppParams.PreferenceKV;
import dbhelper.TableContract;
import model.User;
import utilities.Utility;


/*The Main Function of this Activities are :
 * 1) Take user photo from camera and crop it to get user face
 * 2) Upload the photo to firebase
 * 3) In case of returning user, directly download image from firebase and not ask the user to upload
 * 4) Get all the user data for first time user, in case of repeat user, data input fields are prefilled using data from db or firebase
 * 5) Share CV using image or a link
 * */



public class ResumeActivity extends AppCompatActivity {

    private Button btnPhoto, btnGenerateCV, btnShareImageCV, btnShareLinkCV;
    private ImageView ivPhoto;
    private EditText etName, etAddress, etExperience, etDOB;
    private DatePickerDialog datePicker;
    private SimpleDateFormat dateFormatter;
    private TextView tvUnder18;
    private ProgressDialog progress;


    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private Uri firebaseFilePath;

    private User user;

    private SharedPreferences sharedPreferences;
    File mfile, mfile2;
    Bitmap bitmap;
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    Uri mUri;
    Intent camIntent, cropIntent;

    final int CAMERA_PERMISSION_CODE = 1;
    final int STORAGE_WRITE_PERMISSION_CODE = 2;
    final int STORAGE_READ_PERMISSION_CODE = 3;



    @Override
    protected void onResume() {
        dismissLoadingDialog();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);


        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());

        sharedPreferences = Utility.getSharedPreferences(getApplicationContext());

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        btnPhoto = findViewById(R.id.btn_cv_photo);
        ivPhoto = findViewById(R.id.iv_cv_photo);
        btnGenerateCV = findViewById(R.id.btn_cv_generate);
        btnShareImageCV = findViewById(R.id.btn_cv_image_share);
        btnShareLinkCV = findViewById(R.id.btn_cv_link_share);

        etName = findViewById(R.id.et_cv_name);
        etAddress = findViewById(R.id.et_cv_address);
        etExperience = findViewById(R.id.et_cv_experience);
        etDOB = findViewById(R.id.et_cv_date);

        tvUnder18 = findViewById(R.id.tv_cv_under_18);

        setDateTimeField();

        user = new User();

        initializeData();


        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraOpen();
            }
        });

        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });

        etDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePicker.show();
            }
        });


        btnGenerateCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    insertDataIntoDB();
                    if (AppVariables.isEighteenPlus)
                        startActivity(new Intent(ResumeActivity.this, GenerateCV.class));
                    else
                        Toast.makeText(ResumeActivity.this, getResources().getString(R.string.below_18_toast), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnShareImageCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean(PreferenceKV.KEY_IS_CV_GENERATED, PreferenceKV.DEF_IS_CV_GENERATED))
                    shareCV();
                else
                    Toast.makeText(ResumeActivity.this, getResources().getString(R.string.cv_not_generated), Toast.LENGTH_LONG).show();
            }
        });

        btnShareLinkCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean(PreferenceKV.KEY_IS_CV_GENERATED, PreferenceKV.DEF_IS_CV_GENERATED))
                    shareTextLink();
                else
                    Toast.makeText(ResumeActivity.this, getResources().getString(R.string.cv_not_generated), Toast.LENGTH_LONG).show();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 0 && resultCode == RESULT_OK)
            cropImage();

        else if (requestCode == 1){
            if (data != null){

                Uri uri = data.getData();

                if(uri != null){
                    bitmap = decodeUriAsBitmap(uri);
                    ivPhoto.setImageBitmap(bitmap);
                    ivPhoto.setVisibility(View.VISIBLE);
                    btnPhoto.setVisibility(View.GONE);
                    saveImage();
                }else{
                    Toast.makeText(ResumeActivity.this, getResources().getString(R.string.camera_error), Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    //open camera to capture
    private void cameraOpen(){
        camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mfile = new File(Environment.getExternalStorageDirectory(),AppVariables.imageDirectoryName + AppVariables.cropImageName);

        mUri = Uri.fromFile(mfile);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT,mUri);
        camIntent.putExtra("return-data",true);
        startActivityForResult(camIntent,0);
    }

    //open cropper
    private void cropImage(){
        try{
            cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(mUri,"image/*");

            cropIntent.putExtra("crop","true");
            cropIntent.putExtra("outputX",180);
            cropIntent.putExtra("outputY",180);
            cropIntent.putExtra("aspectX",3);
            cropIntent.putExtra("aspectY",3);
            cropIntent.putExtra("scaleUpIfNeeded",true);
            //cropIntent.putExtra("return-data",true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);


            startActivityForResult(cropIntent,1);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.crop_error), Toast.LENGTH_LONG).show();
        }

    }

    //save cropped image to local directory
    private void saveImage(){
        //mfile2 = new File(path,"cv-cropped-image.jpg");
        //path.mkdir();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,50,bos);
        byte[] bitmapdata = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

        try{
            OutputStream os = new FileOutputStream(mfile);
            byte[]data = new byte[bis.available()];
            bis.read(data);
            os.write(data);
            bis.close();
            os.close();

            sharedPreferences.edit().putBoolean(PreferenceKV.KEY_IS_PROFILE_PIC_GENERATED,true).commit();
            uploadProfilePicToFirebase();

            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.cv_profile_pic_saved), Toast.LENGTH_SHORT).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    //initialise datepcikser
    private void setDateTimeField(){
        Calendar newCalendar = Calendar.getInstance();

        datePicker = new DatePickerDialog(ResumeActivity.this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                Calendar currentAge = new GregorianCalendar(year,monthOfYear,dayOfMonth);
                Calendar minAge = new GregorianCalendar();
                minAge.add(Calendar.YEAR,-18);

                if(minAge.before(currentAge)) {
                    tvUnder18.setVisibility(View.VISIBLE);
                    AppVariables.isEighteenPlus = false;
                }
                else {
                    tvUnder18.setVisibility(View.GONE);
                    AppVariables.isEighteenPlus = true;
                }

                etDOB.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    //share image of cv
    private void shareCV(){

        String filePath = Environment.getExternalStorageDirectory().getPath();

        File cvFile = new File(filePath,"ApnaTime/cv-image.png");

        Uri uri = FileProvider.getUriForFile(ResumeActivity.this, "com.example.apnatimetest.fileprovider",cvFile);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_cv_body));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_cv_photo)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.share_error), Toast.LENGTH_SHORT).show();
        }
    }

    //share link
    private void shareTextLink(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
        i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_body) + AppVariables.appShareURL);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.share_link)));
    }

    //Check if form data is correct
    private boolean validateData(){
        boolean dataValidated = false;

        if(etName.getText().toString().trim().isEmpty())
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.name_error), Toast.LENGTH_LONG).show();
        else if(etAddress.getText().toString().trim().isEmpty())
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.address_error), Toast.LENGTH_LONG).show();
        else if(etExperience.getText().toString().trim().isEmpty())
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.experience_error), Toast.LENGTH_LONG).show();
        else if(etDOB.getText().toString().trim().isEmpty())
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.dob_error), Toast.LENGTH_LONG).show();
        else
            dataValidated = true;



        return dataValidated;
    }

    private void insertDataIntoDB(){

        user = new User();
        user.setFullName(etName.getText().toString().trim());
        user.setAddress(etAddress.getText().toString().trim());
        user.setExperience(Integer.parseInt(etExperience.getText().toString().trim()));
        user.setDob(etDOB.getText().toString().trim());

        ContentValues cv = Utility.userModelToContentValue(user);

        ResumeActivity.this.getContentResolver().update(TableContract.UserEntry.CONTENT_URI,cv,null,null);

    }

    //Initialise image and text if already available
    private void initializeData(){
        showLoadingDialog(0);

        try {
            if (!loadUserDataFromDB())
                getUserDataFromFirebase();

            if (!loadImageFromDB())
                getImageFromFirebase();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            dismissLoadingDialog();
        }
    }

    /*Load image from gallery*/
    private boolean loadImageFromDB(){
        Boolean found = false;

        String path = Environment.getExternalStorageDirectory().getPath();
        String fileName = "/" + AppVariables.imageDirectoryName + AppVariables.cropImageName;
        String filePath = path + fileName;

        File f = new File(filePath);

        if(f.exists()){
            btnPhoto.setVisibility(View.GONE);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            ivPhoto.setImageBitmap(bitmap);
            ivPhoto.setVisibility(View.VISIBLE);
            found = true;
        }

        return found;
    }

    /*Get user data from firebase*/
    private void getUserDataFromFirebase(){
        final String phoneNo = sharedPreferences.getString(PreferenceKV.USER_NUM, PreferenceKV.DEF_NUM);

        mDatabase = FirebaseDatabase.getInstance().getReference("user");
        DatabaseReference userRef = mDatabase.child(phoneNo);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    User u = dataSnapshot.getValue(User.class);
                    if(u.getFullName() !=null && u.getFullName().length() >0){
                        ContentValues cv = Utility.userModelToContentValue(u);
                        getContentResolver().update(TableContract.UserEntry.CONTENT_URI,cv,null,null);
                        loadUserDataFromDB();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /*Load Data from db and show on UI on the input fields*/
    private Boolean loadUserDataFromDB(){
        boolean loaded = false;

        Cursor cursor = getContentResolver().query(TableContract.UserEntry.CONTENT_URI, null, null, null, null);
        user = new User();

        if (cursor.moveToFirst()) {
            user.setFullName(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_FULL_NAME)));
            user.setNumber(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_PHONE_NUMBER)));
            user.setAddress(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_ADDRESS)));
            user.setExperience(cursor.getInt(cursor.getColumnIndex(TableContract.UserEntry.USER_EXPERIENCE)));
            user.setDob(cursor.getString(cursor.getColumnIndex(TableContract.UserEntry.USER_DOB)));
        }

        if(user.getFullName() != null && user.getFullName().length() > 0){
            etName.setText(user.getFullName());
            etAddress.setText(user.getAddress());
            etExperience.setText(user.getExperience()+"");
            etDOB.setText(user.getDob());
            loaded = true;
        }
        return loaded;
    }

    public void showLoadingDialog(int i) {
        if (progress == null) {
            progress = new ProgressDialog(this);

            if(i == 0) {
                progress.setTitle(getString(R.string.progress_data_fetching_title));
                progress.setMessage(getString(R.string.progress_data_fetching_body));
            }else if(i==1){
                progress.setTitle(getString(R.string.progress_data_uploading_title));
                progress.setMessage(getString(R.string.progress_data_uploading_body));
            }
        }
        progress.show();
    }

    public void dismissLoadingDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    /*Upload Pic to firebase*/
    public void uploadProfilePicToFirebase(){

        showLoadingDialog(1);

        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String path = Environment.getExternalStorageDirectory().getPath();
        String fileName = "/" + AppVariables.imageDirectoryName + AppVariables.cropImageName;
        String filePath = path + fileName;



        File f = new File(filePath);

        firebaseFilePath = Uri.fromFile(f);
        StorageReference ref = storageRef.child("images/"+sharedPreferences.getString(PreferenceKV.USER_NUM,PreferenceKV.DEF_NUM));
        ref.putFile(firebaseFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dismissLoadingDialog();
                Toast.makeText(ResumeActivity.this, getResources().getString(R.string.profile_pic_uploaded), Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissLoadingDialog();
                        e.printStackTrace();
                        Toast.makeText(ResumeActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        ;

    }

    /*Get image from firebase*/
    public void getImageFromFirebase(){
        try {
            storage = FirebaseStorage.getInstance();

            StorageReference mImageRef = storage.getReference();
            StorageReference pathRef = mImageRef.child("images/" + sharedPreferences.getString(PreferenceKV.USER_NUM,PreferenceKV.DEF_NUM));

            Glide.with(ResumeActivity.this)
                    .load(pathRef)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            btnPhoto.setVisibility(View.VISIBLE);
                            ivPhoto.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ivPhoto.setVisibility(View.VISIBLE);
                            btnPhoto.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivPhoto);

            Glide.with(ResumeActivity.this)
                    .asBitmap()
                    .load(pathRef)
                    .into(new SimpleTarget<Bitmap>(100,100) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            saveFirebaseImageLocally(resource);
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*Save image fetched from firebase to external storage*/
    private void saveFirebaseImageLocally(Bitmap bm){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mfile = new File(Environment.getExternalStorageDirectory(),AppVariables.imageDirectoryName + AppVariables.cropImageName);

        bm.compress(Bitmap.CompressFormat.JPEG,50,bos);
        byte[] bitmapdata = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

        try{
            OutputStream os = new FileOutputStream(mfile);
            byte[]data = new byte[bis.available()];
            bis.read(data);
            os.write(data);
            bis.close();
            os.close();

            sharedPreferences.edit().putBoolean(PreferenceKV.KEY_IS_PROFILE_PIC_GENERATED,true).commit();
            Toast.makeText(ResumeActivity.this, getResources().getString(R.string.cv_profile_pic_saved), Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}


