package uet.vnu.check_in.data.source.remote.api.service;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.CheckinResponse;
import uet.vnu.check_in.data.source.remote.api.response.GetCourseByCourseResponse;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;

public interface CheckInApi {

    @POST("login")
    @FormUrlEncoded
    Observable<LoginResponse> loginByEmailAndPassword(@Field("email") String email,
                                                      @Field("password") String password);

    @POST("student")
    @FormUrlEncoded
    Observable<RegisterResponse> registerByEmailAndPassword(@Field("email") String email,
                                                            @Field("password") String password);

    @POST("student/{student_id}")
    @FormUrlEncoded
    Observable<LoginResponse> updateInformationStudent(@Field("student_name") String name,
                                                          @Field("birthday") String birthday,
                                                          @Field("vectors") String vectors,
                                                          @Path("student_id") int student,
                                                       @Field("os") String os);
    @GET("/enroll/{student_id}")
    Observable<List<Course>> enrolledCourse(@Path("student_id") int student_id);

    @GET("/course/code/{code}")
    Observable<GetCourseByCourseResponse> getCourseByCode(@Path("code") String code);

    @POST("/enroll")
    @FormUrlEncoded
    Observable<BaseResponse> enrollCourse(@Field("student_id") int studentId,
                                          @Field("course_id") int courseId);

    @POST("/unroll")
    @FormUrlEncoded
    Observable<BaseResponse> unrollCourse(@Field("student_id") int studentId,
                                          @Field("course_id") int courseId);

    @POST("/message/{course_id}")
    @FormUrlEncoded
    Observable<BaseResponse> sendMessage(@Path("course_id") int courseId,
                                          @Field("message") String message,
                                         @Field("fromId") String fromId,
                                         @Field("isTeacher") String isTeacher,
                                            @Field("name") String name);
    @POST("/student/forgotpassword")
    @FormUrlEncoded
    Observable<BaseResponse> forgotByEmail(@Field("email") String email);

    @POST("/check_in")
    @FormUrlEncoded
    Observable<CheckinResponse> checkin(@Field("student_id") int studentId,
                                        @Field("lat")  double lat,
                                        @Field("long") double longtitude);

    @POST("/check_in/{check_in_id}")
    @FormUrlEncoded
    Observable<BaseResponse> uploadPhotoURL(@Path("check_in_id") int checkinID,
                                        @Field("upload_link")  String uploadLink);

}
