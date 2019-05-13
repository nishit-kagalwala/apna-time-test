package AppParams;

/*Has the keys used for shared preferences aling with the default values*/
public interface PreferenceKV {

    /*Name of SharedPreference*/
    String SHARED_PREFERENCE = "UserSharedPreference";

    String KEY_IS_PROFILE_PIC_GENERATED = "isProfilePicGenerated";
    boolean DEF_IS_PROFILE_PIC_GENERATED = false;

    String KEY_IS_CV_GENERATED = "isCVGenerated";
    boolean DEF_IS_CV_GENERATED = false;

    String USER_NUM = "mobileNumber";
    String DEF_NUM = "0";



}
