package uet.vnu.check_in.data.source.local;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import uet.vnu.check_in.data.model.Student;
import uet.vnu.check_in.data.source.AuthenticationDataSource;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsApi;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsKey;

public class AuthenticationLocalDataSource implements AuthenticationDataSource.LocalDataSource {

    private static AuthenticationLocalDataSource sAuthenticationLocalDataSource;
    private SharedPrefsApi mSharedPrefsApi;

    public static AuthenticationLocalDataSource getInstance(@NonNull SharedPrefsApi sharedPrefsApi) {
        if (sAuthenticationLocalDataSource == null) {
            sAuthenticationLocalDataSource = new AuthenticationLocalDataSource(sharedPrefsApi);
        }
        return sAuthenticationLocalDataSource;
    }

    private AuthenticationLocalDataSource(SharedPrefsApi sharedPrefsApi) {
        mSharedPrefsApi = sharedPrefsApi;
    }

    @Override
    public void saveStudent(Student student) {
        String data = new Gson().toJson(student);
        mSharedPrefsApi.put(SharedPrefsKey.PREFERENCE_STUDENT_KEY, data);
    }

    @Override
    public void deleteStudent() {
        mSharedPrefsApi.delete(SharedPrefsKey.PREFERENCE_STUDENT_KEY);
        mSharedPrefsApi.delete(SharedPrefsKey.AVATAR_KEY1);
        mSharedPrefsApi.delete(SharedPrefsKey.AVATAR_KEY2);
        mSharedPrefsApi.delete(SharedPrefsKey.AVATAR_KEY3);
    }

    @Override
    public Student getLoggedStudent() {
        String data = mSharedPrefsApi.get(SharedPrefsKey.PREFERENCE_STUDENT_KEY, String.class);
        return new Gson().fromJson(data, Student.class);
    }

    @Override
    public String getLoginToken() {
        return null;
    }

    @Override
    public void saveImage1(String img) {
        mSharedPrefsApi.put(SharedPrefsKey.AVATAR_KEY1, img);
    }

    @Override
    public void saveImage2(String img) {
        mSharedPrefsApi.put(SharedPrefsKey.AVATAR_KEY2, img);
    }

    @Override
    public void saveImage3(String img) {
        mSharedPrefsApi.put(SharedPrefsKey.AVATAR_KEY3, img);
    }

    @Override
    public String getImage1() {
        return mSharedPrefsApi.get(SharedPrefsKey.AVATAR_KEY1, String.class);
    }

    @Override
    public String getImage2() {
        return mSharedPrefsApi.get(SharedPrefsKey.AVATAR_KEY2, String.class);
    }

    @Override
    public String getImage3() {
        return  mSharedPrefsApi.get(SharedPrefsKey.AVATAR_KEY3, String.class);
    }
}
