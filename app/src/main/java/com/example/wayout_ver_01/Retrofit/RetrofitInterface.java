package com.example.wayout_ver_01.Retrofit;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    // 서버에 보내고 받을 인터페이스 설정
    // 인터페이스 설정시 retrofit 이 자동으로 처리해줌
    // @POST : HTTP 통신 방식 // test.php 서버측 URL 경로
    // @FormUrlEncoded 무조건해야함 -> URL 형식으로 인코디애서 작성됨
    @FormUrlEncoded
    @POST("joinInsert.php")
    // Call 함수 -> DTO 클래스  // @Field 에는 보낼 Key&value 입력
    Call<JoinRequest> insertJoin(
            @Field("userId") String userId,
            @Field("userPw") String userPw,
            @Field("userNick") String userNick,
            @Field("userPhone") String userPhone
    );

    @FormUrlEncoded
    @POST("joinSelect.php")
    Call<JoinRequest> selectJoin(
            @Field("userId") String userId,
            @Field("userPw") String userPw
    );

    @FormUrlEncoded
    @POST("userUpdate.php")
    Call<User> userReset(
            @Field("userId") String userId,
            @Field("userPw") String userPw,
            @Field("userNick") String userNick,
            @Field("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("phoneInsert.php")
    Call<User> userPhoneCk(
            @Field("userPhone") String userPhone,
            @Field("code") String Code
    );

    @FormUrlEncoded
    @POST("userPwReset.php")
    Call<User> userPwReset(
            @Field("userPw") String userPw,
            @Field("userNewPw") String userNewPw,
            @Field("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("joinDelete.php")
    Call<User> userDelete(
            @Field("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("findResetPw.php")
    Call<User> findResetPw(
            @Field("userPw") String userPw,
            @Field("userPhone") String userPhone
    );

    @Multipart
    @POST("updateUserProfile.php")
    Call<User> userProfile(
            @Part MultipartBody.Part file,
            @Part("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("getUserProfile.php")
    Call<User> getUserProfile(
            @Field("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("deleteUserProfile.php")
    Call<User> deleteUserProfile(
            @Field("userIndex") Integer userIndex
    );

    @FormUrlEncoded
    @POST("writeFreeboard.php")
    Call<DTO_board> writeFreeBoard(
            @Field("writer") String writer,
            @Field("title") String title,
            @Field("content") String content,
            @Field("date") String date
    );

    @GET("getFreeBoard.php")
    Call<ArrayList<DTO_board>> getFreeBoard();


}