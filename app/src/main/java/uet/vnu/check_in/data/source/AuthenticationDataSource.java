package uet.vnu.check_in.data.source;

import java.util.List;

import io.reactivex.Observable;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.remote.api.response.BaseResponse;
import uet.vnu.check_in.data.source.remote.api.response.CheckinResponse;
import uet.vnu.check_in.data.source.remote.api.response.GetCourseByCourseResponse;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;

public interface AuthenticationDataSource {

    interface RemoteDataSource {
        Observable<LoginResponse> loginByEmailAndPassword(String email, String password);

        Observable<RegisterResponse> registerByEmailAndPassword(String email, String password);

        Observable<LoginResponse> updateInformationStudent(String name, String birthday,
                                                           String vectors, int student_id);

        Observable<List<Course>> enrolledCourse(int student_id);

        Observable<GetCourseByCourseResponse> getCourseByCode(String code);

        Observable<BaseResponse> enrollCourse(int studentId, int courseId);

        Observable<BaseResponse> unrollCourse(int studentId, int courseId);

        Observable<BaseResponse> sendMessage(int courseId, String message, String fromId,
                                             String isTeacher, String name);

        Observable<BaseResponse> forgotPasswordbyEmail(String email);

        Observable<CheckinResponse> checkin(int studentId, double longtitude, double lattitude);

        Observable<BaseResponse> pushPhoto(String urlImage, int photoId);
    }

    interface LocalDataSource {
        void saveStudent(Student student);

        void deleteStudent();

        Student getLoggedStudent();

        String getLoginToken();

        void saveImage1(String img);

        void saveImage2(String img);

        void saveImage3(String img);

        String getImage1();

        String getImage2();

        String getImage3();

    }
}
