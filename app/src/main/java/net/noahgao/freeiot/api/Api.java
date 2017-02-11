package net.noahgao.freeiot.api;

import android.util.ArrayMap;

import net.noahgao.freeiot.model.UserModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Noah Gao on 17-2-8.
 * By Android Studio
 */

public interface Api {

    @FormUrlEncoded
    @POST("/user/auth")
    Call<UserModel> auth(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("/user")
    Call<UserModel> reg(@Field("email") String email, @Field("password") String password);
}
