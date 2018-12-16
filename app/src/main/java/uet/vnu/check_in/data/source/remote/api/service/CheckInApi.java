package uet.vnu.check_in.data.source.remote.api.service;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;
import uet.vnu.check_in.data.source.remote.api.response.LoginResponse;
import uet.vnu.check_in.data.source.remote.api.response.RegisterResponse;

public interface CheckInApi {

    @POST("login")
    @FormUrlEncoded
    Observable<LoginResponse> loginByEmailAndPassword(@Field("email") String email,
                                                      @Field("password") String password);

    @POST("student")
    @FormUrlEncoded
    Observable<RegisterResponse> registerByEmailAndPassword(@Field("email") String email,
                                                            @Field("password") String password);

    @POST("student/{student_id}")
    @FormUrlEncoded
    Observable<LoginResponse> updateInformationStudent(@Field("student_name") String name,
                                                          @Field("birthday") String birthday,
                                                          @Field("vectors") String vectors,
                                                          @Path("student_id") int student_id);
}
