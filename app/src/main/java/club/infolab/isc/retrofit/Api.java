package club.infolab.isc.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {
    @POST("tests/add")
    Call<Object> addTest(@Body RequestBody test);

}
