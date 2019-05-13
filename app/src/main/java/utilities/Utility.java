package utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import AppParams.PreferenceKV;
import dbhelper.TableContract;
import model.User;

/*Setup of multiuse utility methods*/
public class Utility {

    /*get Shared Preference of Application*/
    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PreferenceKV.SHARED_PREFERENCE, context.MODE_PRIVATE);
        return prefs;
    }

    public static ContentValues userModelToContentValue (User user){
        ContentValues contentValue = new ContentValues();

        try{
            //contentValue.put(TableContract.UserEntry.USER_PHONE_NUMBER,user.getNumber());
            contentValue.put(TableContract.UserEntry.USER_FULL_NAME,user.getFullName());
            contentValue.put(TableContract.UserEntry.USER_ADDRESS,user.getAddress());
            contentValue.put(TableContract.UserEntry.USER_EXPERIENCE,user.getExperience());
            contentValue.put(TableContract.UserEntry.USER_DOB,user.getDob());

        }catch (Exception e){
            e.printStackTrace();
        }

        return contentValue;
    }

    public static ContentValues generateUserPhoneNoCV(String phoneNo){
        ContentValues contentValue = new ContentValues();

        try{
            contentValue.put(TableContract.UserEntry.USER_PHONE_NUMBER,phoneNo);
            contentValue.put(TableContract.UserEntry.USER_FULL_NAME,"");
            contentValue.put(TableContract.UserEntry.USER_ADDRESS,"");
            contentValue.put(TableContract.UserEntry.USER_EXPERIENCE,"");
            contentValue.put(TableContract.UserEntry.USER_DOB,"");

        }catch (Exception e){
            e.printStackTrace();
        }

        return contentValue;
    }

    public static User convertFBStringToUser(String s, String phoneNo){
        User u = new User();
        try {
            Log.d("Nishit", s);

            String copyString = s;


            u.setNumber(phoneNo);

            copyString = copyString.substring(s.indexOf("fullName"));
            copyString = copyString.substring(0, copyString.indexOf(","));

            s = s.replace(copyString + ",", "");
            s = s.replace(copyString, "");
            u.setFullName(copyString.replace("fullName=", ""));

            copyString = s.substring(s.indexOf("experience"));
            copyString = copyString.substring(11, 12);
            copyString = copyString.replace(",", "");

            u.setExperience(Integer.parseInt(copyString));

            s = s.replace("experience=" + copyString + ",", "");
            s = s.replace("experience=" + copyString, "");


            copyString = s.substring(s.indexOf("dob"));
            copyString = copyString.substring(4, 14);

            u.setDob(copyString);

            s = s.replace("dob=" + copyString + ",", "");
            s = s.replace("dob=" + copyString, "");

            s = s.replace("number=" + phoneNo + ",", "");
            s = s.replace("number=" + phoneNo, "");
            s = s.replace(phoneNo, "");
            s = s.replace("{", "");
            s = s.replace("}", "");
            s = s.replace("=", "");

            s = s.replace("address", "");
            s = s.trim();

            if (s.charAt(s.length() - 1) == ',') {
                s = s.substring(0, s.length() - 1);
            }

            u.setAddress(s);
        }catch (Exception e){
            e.printStackTrace();
        }
        return u;
    }
}
