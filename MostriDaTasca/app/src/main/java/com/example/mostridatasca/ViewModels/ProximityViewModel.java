package com.example.mostridatasca.ViewModels;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.RetrofitData.IServerApi;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import java.util.List;

public class ProximityViewModel extends ViewModel {
	private final MutableLiveData<List<ServerResponse.UserNearby>> nearbyUsers;
	private final MutableLiveData<List<ServerResponse.ObjectNearby>> nearbyObjects;
	private final IServerApi retrofit;
	private final PlayerViewModel playerViewModel;
	
	public LiveData<List<ServerResponse.UserNearby>> getNearbyUsers() {	return nearbyUsers;	}
	public LiveData<List<ServerResponse.ObjectNearby>> getNearbyObjects() {	return nearbyObjects;	}
	
	// !!! Must be initialized by a ProximityViewModelFactory to take a PlayerViewModel as parameter
	public ProximityViewModel(PlayerViewModel playerViewModel) {
		this.nearbyUsers = new MutableLiveData<>();
		this.nearbyObjects = new MutableLiveData<>();
		this.retrofit = CommunicationController.getRetrofitClient();
		this.playerViewModel = playerViewModel;
		playerViewModel.getLocation().observeForever(this::updateNearbyEntities);
	}
	
	@Override
	protected void onCleared() {
		super.onCleared();
		this.playerViewModel.getLocation().removeObserver(this::updateNearbyEntities);
	}
	
	private void updateNearbyEntities(Location location) {
		if (location == null) return;
		String sid = playerViewModel.getSid().getValue();
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		
		retrofit.getUsersNearby(sid, lat, lon).enqueue(CommunicationController.onSuccess(
				(response) -> nearbyUsers.postValue(response.body())
		));
		
		retrofit.getObjectsNearby(sid, lat, lon).enqueue(CommunicationController.onSuccess(
				(response) -> nearbyObjects.postValue(response.body())
		));
	}
}