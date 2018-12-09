package uet.vnu.check_in.data.source;

import io.reactivex.Observable;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;

public interface AuthenticationDataSource {

    interface RemoteDataSource {
        Observable<LoginResponse> loginByEmailAndPassword(String email, String password);
    }

    interface LocalDataSource {
        void saveStudent(Student student);

        void deleteStudent();

        Student getLoggedStudent();

        String getLoginToken();
    }
}
