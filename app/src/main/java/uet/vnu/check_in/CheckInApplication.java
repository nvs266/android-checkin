package uet.vnu.check_in;

import android.app.Application;

import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsApi;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsImpl;
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
}
