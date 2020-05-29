package club.infolab.isc.retrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitController {
    private Api api;
    private RetrofitCallback callback;
    
    public RetrofitController(RetrofitCallback callback) {
        this.callback = callback;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://serverforisk.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(Api.class);
    }

    public void addTest(String type, String date, String result, final int index) {
        RequestBody test = new RequestBody(type, date, result);
        Call call = api.addTest(test);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                callback.onResult(index, true);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onResult(index, false);
            }
        });
    }
}
