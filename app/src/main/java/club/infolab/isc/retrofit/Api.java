package club.infolab.isc.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Api {
    @POST("add_test")
    Call<Object> addTest(@Body RequestBody test);

    @GET("get_tests")
    Call<Object> getTests();

    @GET("delete_tests/all")
    Call<Object> deleteTests();
}
