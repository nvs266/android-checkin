package uet.vnu.check_in.data.source.remote;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.AuthenticationDataSource;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.CheckinResponse;
import uet.vnu.check_in.data.source.remote.api.response.GetCourseByCourseResponse;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;
import uet.vnu.check_in.data.source.remote.api.service.CheckInApi;

public class AuthenticationRemoteDataSource implements AuthenticationDataSource.RemoteDataSource {

    private static AuthenticationRemoteDataSource sAuthenticationRemoteDataSource;
    private CheckInApi mCheckInApi;

    public static AuthenticationRemoteDataSource getInstance(@NonNull CheckInApi checkInApi) {
        if (sAuthenticationRemoteDataSource == null) {
            sAuthenticationRemoteDataSource = new AuthenticationRemoteDataSource(checkInApi);
        }

        return sAuthenticationRemoteDataSource;
    }

    private AuthenticationRemoteDataSource(CheckInApi checkInApi) {
        mCheckInApi = checkInApi;
    }


    @Override
    public Observable<LoginResponse> loginByEmailAndPassword(String email, String password) {
        return mCheckInApi.loginByEmailAndPassword(email, password);
    }
    @Override
    public Observable<RegisterResponse> registerByEmailAndPassword(String email, String password) {
        return mCheckInApi.registerByEmailAndPassword(email, password);
    }

    @Override
    public Observable<LoginResponse> updateInformationStudent(String name, String birthday, String vectors, int student_id) {
        return mCheckInApi.updateInformationStudent(name, birthday, vectors, student_id, "android");
    }

    @Override
    public Observable<List<Course>> enrolledCourse(int student_id) {
        return mCheckInApi.enrolledCourse(student_id);
    }

    @Override
    public Observable<GetCourseByCourseResponse> getCourseByCode(String code) {
        return mCheckInApi.getCourseByCode(code);
    }

    @Override
    public Observable<BaseResponse> enrollCourse(int studentId, int courseId) {
        return mCheckInApi.enrollCourse(studentId, courseId);
    }

    @Override
    public Observable<BaseResponse> unrollCourse(int studentId, int courseId) {
        return mCheckInApi.unrollCourse(studentId, courseId);
    }

    @Override
    public Observable<BaseResponse> sendMessage(int courseId, String message, String fromId, String isTeacher, String name) {
        return mCheckInApi.sendMessage(courseId, message, fromId, isTeacher, name);
    }

    @Override
    public Observable<BaseResponse> forgotPasswordbyEmail(String email) {
        return mCheckInApi.forgotByEmail(email);
    }

    @Override
    public Observable<CheckinResponse> checkin(int studentId, double longtitude, double lattitude) {
        return mCheckInApi.checkin(studentId, lattitude, longtitude);
    }

    @Override
    public Observable<BaseResponse> pushPhoto(String urlImage, int photoId) {
        return mCheckInApi.uploadPhotoURL(photoId, urlImage);
    }

}
