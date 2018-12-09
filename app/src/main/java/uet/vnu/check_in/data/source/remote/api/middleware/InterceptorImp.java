package uet.vnu.check_in.data.source.remote.api.middleware;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsImpl;

public class InterceptorImp implements Interceptor {

    private SharedPrefsImpl mSharedPrefs;

    public InterceptorImp(SharedPrefsImpl sharedPrefs) {
        mSharedPrefs = sharedPrefs;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = initializeHeader(chain);
        Request request = builder.build();
        return chain.proceed(request);
    }

    private Request.Builder initializeHeader(Chain chain) {
        Request originRequest = chain.request();
        return originRequest.newBuilder()
                .method(originRequest.method(), originRequest.body());
    }
}
