package uet.vnu.check_in;

import android.app.Application;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import uet.vnu.check_in.data.model.Course;
import uet.vnu.check_in.data.source.local.AuthenticationLocalDataSource;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsApi;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsImpl;
import uet.vnu.check_in.data.source.remote.AuthenticationRemoteDataSource;
import uet.vnu.check_in.data.source.remote.api.service.CheckInApi;
import uet.vnu.check_in.data.source.remote.api.service.CheckInService;

public class CheckInApplication extends Application {

    private static CheckInApplication sCheckInApplication;

    private SharedPrefsApi mSharedPrefsApi;

    @Override
    public void onCreate() {
        super.onCreate();
        sCheckInApplication = this;
    }

    public static CheckInApplication getInstance() {
        return sCheckInApplication;
    }

    public CheckInApi getCheckInApi() {
        return CheckInService.getInstance(this);
    }

    public SharedPrefsApi getSharedPrefsApi() {
        if (mSharedPrefsApi == null) {
            mSharedPrefsApi = new SharedPrefsImpl(this);
        }

        return mSharedPrefsApi;
    }
    public void subscribeMessageToTopic(final Boolean subscribe){
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .enrolledCourse(AuthenticationLocalDataSource.getInstance(CheckInApplication.getInstance().getSharedPrefsApi()).getLoggedStudent().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).subscribe(new Consumer<List<Course>>() {
            @Override
            public void accept(List<Course> courses) throws Exception {
                for (Course course : courses) {
                    if (subscribe) {
                        FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(course.getId()));
                    }else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(String.valueOf(course.getId()));
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }
}
