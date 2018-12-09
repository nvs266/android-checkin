package uet.vnu.check_in.data.source.remote.api.service;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import uet.vnu.check_in.BuildConfig;
import uet.vnu.check_in.CheckInApplication;
import uet.vnu.check_in.data.source.local.sharedpref.SharedPrefsImpl;
import uet.vnu.check_in.data.source.remote.api.middleware.InterceptorImp;
import uet.vnu.check_in.util.Constant;

public class CheckInService {

    private static CheckInApi sCheckInApi;
    private static int REQUEST_TIME_OUT = 10;

    private CheckInService() {
    }

    public static CheckInApi getInstance(CheckInApplication application) {
        if (sCheckInApi == null) {
            sCheckInApi = new Retrofit.Builder().baseUrl(Constant.ApiKey.END_POINT_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getUnsafeOkHttpClient(application))
                    .build()
                    .create(CheckInApi.class);
        }

        return sCheckInApi;
    }

    private static OkHttpClient getUnsafeOkHttpClient(CheckInApplication application) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cache(provideCache(application));
            builder.addInterceptor(provideInterceptor(application));
            builder.readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS);
            builder.connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS);

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(loggingInterceptor);
            }

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Interceptor provideInterceptor(CheckInApplication application) {
        return new InterceptorImp(new SharedPrefsImpl(application));
    }

    private static Cache provideCache(CheckInApplication application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        return new Cache(application.getCacheDir(), cacheSize);
    }
}
