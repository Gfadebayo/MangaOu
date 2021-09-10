package com.exzell.mangaplayground.io.internet;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface MangaParkApi {

    @GET("/latest/")
    Call<ResponseBody> getLatest();

    @GET("/search")
    Observable<ResponseBody> advancedSearch(@QueryMap(encoded = true) Map<String, String> queries);

    @GET("/genre/")
    Call<ResponseBody> genres();

    @GET("/")
    Call<ResponseBody> home();

    @GET("{next}/")
    Observable<ResponseBody> next(@Path(value = "next", encoded = true) String next);

    @GET
    Observable<ResponseBody> get(@Url String url);
}
