package uet.vnu.check_in.data.source.remote.api.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import uet.vnu.check_in.data.model.Student;

public class RegisterResponse extends BaseResponse{

    @Expose
    @SerializedName("student_id")
    private int studentId;

    protected RegisterResponse(Parcel in) {
        super(in);
    }

    public RegisterResponse(int status) {
        super(status);
    }


    public int getStudentId() {
        return studentId;
    }

    public void setStudent(int studentId) {
        this.studentId = studentId;
    }
}
