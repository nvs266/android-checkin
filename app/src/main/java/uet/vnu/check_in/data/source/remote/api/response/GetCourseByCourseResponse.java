package uet.vnu.check_in.data.source.remote.api.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetCourseByCourseResponse extends BaseResponse{

    @Expose
    @SerializedName("course_id")
    private int courseId;

    @Expose
    @SerializedName("course_name")
    private String courseName;

    protected GetCourseByCourseResponse(Parcel in) {
        super(in);
    }

    public GetCourseByCourseResponse(int status) {
        super(status);
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
