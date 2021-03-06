package uet.vnu.check_in.data.source.remote.api.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import uet.vnu.check_in.data.model.Student;

public class LoginResponse extends BaseResponse {

    @Expose
    @SerializedName("student")
    private Student mStudent;

    protected LoginResponse(Parcel in) {
        super(in);
    }

    public LoginResponse(int status) {
        super(status);
    }

    public Student getStudent() {
        return mStudent;
    }

    public void setStudent(Student student) {
        mStudent = student;
    }
}
