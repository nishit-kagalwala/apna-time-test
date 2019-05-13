package model;

import com.google.firebase.database.IgnoreExtraProperties;

/*Model of our User for db and data manupulation*/

@IgnoreExtraProperties
public class User {

    private String number;
    private String fullName;
    private String address;
    private int experience;
    private String dob;


    public User() {
    }

    public User(String number, String fullName, String address, int experience, String dob){
        this.number = number;
        this.fullName = fullName;
        this.address = address;
        this.experience = experience;
        this.dob = dob;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }


    @Override
    public String toString() {
        return "User{" +
                "number='" + number + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + address + '\'' +
                ", experience=" + experience +
                ", dob='" + dob + '\'' +
                '}';
    }
}
