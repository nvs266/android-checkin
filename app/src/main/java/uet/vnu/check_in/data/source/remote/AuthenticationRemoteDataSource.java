package uet.vnu.check_in.data.source.remote;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import uet.vnu.check_in.data.source.AuthenticationDataSource;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
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
}
