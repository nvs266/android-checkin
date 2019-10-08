package uet.vnu.check_in.data.source.remote.api.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckinResponse extends BaseResponse {

    @Expose
    @SerializedName("insertId")
    private int insertId;

    @Expose
    @SerializedName("course_id")
    private int courseID;

    @Expose
    @SerializedName("course_name")
    private String courseName;


    protected CheckinResponse(Parcel in) {
        super(in);
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getInsertId() {
        return insertId;
    }

    public void setInsertId(int insertId) {
        this.insertId = insertId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
