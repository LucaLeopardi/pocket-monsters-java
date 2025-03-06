package com.example.mostridatasca;

import androidx.annotation.NonNull;

import com.example.mostridatasca.RetrofitData.IServerApi;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunicationController {
	// Singleton implementation
	private static Retrofit retrofitInstance = null;
	private static IServerApi apiInstance = null;
	
	public static IServerApi getRetrofitClient() {
		if (retrofitInstance == null) {
			retrofitInstance = new Retrofit.Builder()
					.baseUrl("https://develop.ewlab.di.unimi.it/mc/mostri/")
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		if (apiInstance == null) {
			apiInstance = retrofitInstance.create(IServerApi.class);
		}
		return apiInstance;
	}
	
	public static <T> Callback<T> onSuccess(Consumer<Response<T>> onSuccess) {
		return new Callback<T>() {
			@Override
			public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
				if (response.isSuccessful()) onSuccess.accept(response);
				else throw new RuntimeException("RETROFIT::Callback::onResponse:: ERROR " + response.code() + " " + response.message());
			}
			@Override
			public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
				throw new RuntimeException("RETROFIT::Callback::onFailure:: ERROR " + t.getMessage());
			}
		};
	}
}