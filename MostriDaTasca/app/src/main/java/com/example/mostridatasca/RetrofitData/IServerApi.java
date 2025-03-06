package com.example.mostridatasca.RetrofitData;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IServerApi {

	@POST("users")
	Call<ServerResponse.SignUp> signUp();
	
	@GET("ranking")
	Call<List<ServerResponse.UserRanking>> getRanking(@Query("sid") String sid);
	
	@GET("users")
	Call<List<ServerResponse.UserNearby>> getUsersNearby(@Query("sid") String sid,
														 @Query("lat") double lat,
														 @Query("lon") double lon);
	
	@GET("users/{id}")
	Call<ServerResponse.UserDetails> getUserDetails(@Path("id") int id,
													@Query("sid") String sid);
	
	@PATCH("users/{id}")
	@FormUrlEncoded
	Call<Void> updateUser(@Field("sid") String sid,
						  @Path("id") int id,
						  @Field("name") String name,
						  @Field("picture") String picture,
						  @Field("positionshare") boolean positionshare);
	
	@GET("objects")
	Call<List<ServerResponse.ObjectNearby>> getObjectsNearby(@Query("sid") String sid,
															 @Query("lat") double lat,
															 @Query("lon") double lon);
	
	@GET("objects/{id}")
	Call<ServerResponse.ObjectDetails> getObjectDetails(@Path("id") int id,
														@Query("sid") String sid);
	
	@POST("objects/{id}/activate")
	@FormUrlEncoded
	Call<ServerResponse.ObjectActivation> activateObject(@Field("sid") String sid,
														 @Path("id") int id);
}