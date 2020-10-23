package com.caowj.lib_network.net;


import com.caowj.lib_network.bean.HttpBaseResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface HttpServiceApi {


    /**
     * https://api.apiopen.top/getJoke?page=1&count=2&type=video
     *
     * @return
     */
    @GET("getJoke")
    Call<Object> getJoke(@Query("page") int page, @Query("count") int count, @Query("type") String type);

//    @GET("getJoke2")
//    Call<HttpBaseResult<List<String>>> getJoke2(@Query("page") int page, @Query("count") int count, @Query("type") String type);

}
