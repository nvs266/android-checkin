package uet.vnu.check_in.data.source;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.Path;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;

public interface AuthenticationDataSource {

    interface RemoteDataSource {
        Observable<LoginResponse> loginByEmailAndPassword(String email, String password);
        Observable<RegisterResponse> registerByEmailAndPassword(String email, String password);
        Observable<LoginResponse> updateInformationStudent( String name, String birthday, String vectors, int student_id);
    }

    interface LocalDataSource {
        void saveStudent(Student student);

        void deleteStudent();

        Student getLoggedStudent();

        String getLoginToken();
    }
}
