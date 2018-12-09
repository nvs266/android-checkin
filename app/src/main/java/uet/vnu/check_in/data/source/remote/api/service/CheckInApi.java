package uet.vnu.check_in.data.source.remote.api.service;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;

public interface CheckInApi {

    @POST("login")
    @FormUrlEncoded
    Observable<LoginResponse> loginByEmailAndPassword(@Field("email") String email,
                                                      @Field("password") String password);

}
