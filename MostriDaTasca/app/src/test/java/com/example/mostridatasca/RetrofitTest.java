package com.example.mostridatasca;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.mostridatasca.RetrofitData.IServerApi;
import com.example.mostridatasca.RetrofitData.ServerResponse;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitTest {

	private final IServerApi retrofit = CommunicationController.getRetrofitClient();
	private final  String sid = "bjbp5XewsyyxlME2zkG8";	// Hardcoded sid for testing

	@Test
	public void signUpTest() throws IOException {
	Response<ServerResponse.SignUp> res =retrofit.signUp().execute();
	System.out.println("- SIGN UP");
	if(!res.isSuccessful()) {
		System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
		return;
	}
	System.out.println("-- sid: " + res.body().getSid());
	System.out.println("-- Uid: " + res.body().getUid());
	System.out.println();
	}

	@Test
	public void getRankingTest() throws IOException {
		Response<List<ServerResponse.UserRanking>> res = retrofit.getRanking(sid).execute();
		System.out.println("- GET RANKING");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- size: " + res.body().size());
		for (int i = 0; i < res.body().size(); i++) {
			System.out.println("---------------------------------------------");
			System.out.println("-- uid: " + res.body().get(i).getUid());
			System.out.println("-- profileversion: " + res.body().get(i).getProfileVersion());
		}
	}
	
	@Test
	public void getUsersNearbyTest() throws IOException {
		Response<List<ServerResponse.UserNearby>> res = retrofit.getUsersNearby(sid, 0.0, 0.0).execute();
		System.out.println("- GET USERS NEARBY");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- size: " + res.body().size());
		for (int i = 0; i < res.body().size(); i++) {
			System.out.println("---------------------------------------------");
			System.out.println("-- uid: " + res.body().get(i).getUid());
			System.out.println("-- lat: " + res.body().get(i).getLat());
			System.out.println("-- lon: " + res.body().get(i).getLon());
			System.out.println("-- profileversion: " + res.body().get(i).getProfileVersion());
		}
	}
	
	@Test
	public void getUserDetailsTest() throws IOException {
		Response<ServerResponse.UserDetails> res = retrofit.getUserDetails(16177, sid).execute();
		System.out.println("- GET USER DETAILS");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- uid: " + res.body().getUid());
		System.out.println("-- name: " + res.body().getName());
		System.out.println("-- lat: " + res.body().getLat());
		System.out.println("-- lon: " + res.body().getLon());
		System.out.println("-- life: " + res.body().getLife());
		System.out.println("-- experience: " + res.body().getExperience());
		System.out.println("-- weapon: " + res.body().getWeapon());
		System.out.println("-- armor: " + res.body().getArmor());
		System.out.println("-- amulet: " + res.body().getAmulet());
		if (res.body().getPicture() == null) System.out.println("-- picture: null");
		else System.out.println("-- picture: " + res.body().getPicture().substring(0, 10) + "...");
		System.out.println("-- profileversion: " + res.body().getProfileVersion());
		System.out.println("-- positionshare: " + res.body().getPositionShare());
	}
	
	@Test
	public void updateUserTest() throws IOException {
		Response<Void> res = retrofit.updateUser(sid, 16177, "LucaTest", null, true).execute();
		System.out.println("- UPDATE USER");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		System.out.println("-- success");
	}
	
	@Test
	public void getObjectsNearbyTest() throws IOException {
		Response<List<ServerResponse.ObjectNearby>> res = retrofit.getObjectsNearby(sid, 0.0, 0.0).execute();
		System.out.println("- GET OBJECTS NEARBY");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- size: " + res.body().size());
		for (int i = 0; i < res.body().size(); i++) {
			System.out.println("---------------------------------------------");
			System.out.println("-- id: " + res.body().get(i).getId());
			System.out.println("-- lat: " + res.body().get(i).getLat());
			System.out.println("-- lon: " + res.body().get(i).getLon());
			System.out.println("-- type: " + res.body().get(i).getType());
		}
	}
	
	@Test
	public void getObjectDetailsTest() throws IOException {
		Response<ServerResponse.ObjectDetails> res = retrofit.getObjectDetails( 12, sid).execute();
		System.out.println("- GET OBJECT DETAILS");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- id: " + res.body().getId());
		System.out.println("-- type: " + res.body().getType());
		System.out.println("-- name: " + res.body().getName());
		System.out.println("-- level: " + res.body().getLevel());
		if (res.body().getImage() == null) System.out.println("-- image: null");
		else System.out.println("-- image: " + res.body().getImage().substring(0, 10) + "...");
	}
	
	@Test
	public void activateObjectTest() throws IOException {
		Response<ServerResponse.ObjectActivation> res = retrofit.activateObject(sid, 12).execute();
		System.out.println("- ACTIVATE OBJECT");
		if (!res.isSuccessful()) {
			assert res.errorBody() != null;
			System.out.println("Error: " + res.code() + " - " + res.errorBody().string());
			return;
		}
		assert res.body() != null;
		System.out.println("-- died: " + res.body().getDied());
		System.out.println("-- life: " + res.body().getLife());
		System.out.println("-- experience: " + res.body().getExperience());
		System.out.println("-- weapon: " + res.body().getWeapon());
		System.out.println("-- armor: " + res.body().getArmor());
		System.out.println("-- amulet: " + res.body().getAmulet());
	}
	
	@Rule
	public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
	
	@Test
	public void updateNearbyEntitiesTest() throws IOException {
		MutableLiveData<List<ServerResponse.UserNearby>> nearbyUsers = new MutableLiveData<>();
		MutableLiveData<List<ServerResponse.ObjectNearby>> nearbyObjects = new MutableLiveData<>();
		double lat = 45.478;
		double lon = 9.227;
		
		List<ServerResponse.UserNearby> users = retrofit.getUsersNearby(sid, lat, lon).execute().body();
		nearbyUsers.setValue(users);
		
		List<ServerResponse.ObjectNearby> objects = retrofit.getObjectsNearby(sid, lat, lon).execute().body();
		nearbyObjects.setValue(objects);
		
		System.out.println("- UPDATE NEARBY ENTITIES");
		System.out.println("-- nearbyUsers size: " + nearbyUsers.getValue().size());
		for(ServerResponse.UserNearby u : nearbyUsers.getValue()) {
			System.out.println("---------------------------------------------");
			System.out.println("-- uid: " + u.getUid());
			System.out.println("-- lat: " + u.getLat());
			System.out.println("-- lon: " + u.getLon());
			System.out.println("-- profileversion: " + u.getProfileVersion());
		}
		System.out.println("---------------------------------------------");
		System.out.println("---------------------------------------------");
		System.out.println("-- nearbyObjects size: " + nearbyObjects.getValue().size());
		for(ServerResponse.ObjectNearby o : nearbyObjects.getValue()) {
			System.out.println("---------------------------------------------");
			System.out.println("-- id: " + o.getId());
			System.out.println("-- lat: " + o.getLat());
			System.out.println("-- lon: " + o.getLon());
			System.out.println("-- type: " + o.getType());
		}
	}
}