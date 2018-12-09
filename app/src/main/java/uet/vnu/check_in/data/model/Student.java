package uet.vnu.check_in.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Student implements Parcelable {

    @Expose
    @SerializedName("student_id")
    private int mId;

    @Expose
    @SerializedName("student_name")
    private String mName;

    @Expose
    @SerializedName("birthday")
    private String mBirthday;

    @Expose
    @SerializedName("email")
    private String mEmail;

    @Expose
    @SerializedName("vectors")
    private String mVectors;

    @Expose
    @SerializedName("os")
    private String mOS;

    @Expose
    @SerializedName("device_token")
    private String mDeviceToken;

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mBirthday);
        dest.writeString(mEmail);
        dest.writeString(mVectors);
        dest.writeString(mOS);
        dest.writeString(mDeviceToken);
    }

    protected Student(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mBirthday = in.readString();
        mEmail = in.readString();
        mVectors = in.readString();
        mOS = in.readString();
        mDeviceToken = in.readString();
    }

    public Student(int id, String name, String birthday, String email, String vectors,
                   String OS, String deviceToken) {
        mId = id;
        mName = name;
        mBirthday = birthday;
        mEmail = email;
        mVectors = vectors;
        mOS = OS;
        mDeviceToken = deviceToken;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String birthday) {
        mBirthday = birthday;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getVectors() {
        return mVectors;
    }

    public void setVectors(String vectors) {
        mVectors = vectors;
    }

    public String getOS() {
        return mOS;
    }

    public void setOS(String OS) {
        mOS = OS;
    }

    public String getDeviceToken() {
        return mDeviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        mDeviceToken = deviceToken;
    }
}
