package com.example.mostridatasca.ViewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mostridatasca.RoomData.User;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class PlayerViewModel extends AndroidViewModel {
	private final SharedPreferences prefs;
	private final MutableLiveData<String> sid;
	private final MutableLiveData<Integer> uid;
	private final MutableLiveData<Integer> profileVersion;
	private final MutableLiveData<Integer> weaponLevel;
	private final MutableLiveData<Integer> armorLevel;
	private final MutableLiveData<Integer> amuletLevel;
	private final MutableLiveData<Location> location;
	private final LocationCallback locationCallback;
	
	public PlayerViewModel(@NonNull Application application) {
		super(application);
		prefs = application.getSharedPreferences("playerData", Context.MODE_PRIVATE);
		sid = new MutableLiveData<>(prefs.getString("sid", null));
		uid = new MutableLiveData<>(prefs.getInt("uid", -1));
		profileVersion = new MutableLiveData<>(prefs.getInt("profileVersion", 0));
		Log.d("PlayerViewModel", "SID: " + sid.getValue() + " UID: " + uid.getValue() + " ProfileVersion: " + profileVersion.getValue());
		weaponLevel = new MutableLiveData<>(prefs.getInt("weaponLevel", 0));
		armorLevel = new MutableLiveData<>(prefs.getInt("armorLevel", 0));
		amuletLevel = new MutableLiveData<>(prefs.getInt("amuletLevel", 0));
		location = new MutableLiveData<>(null);
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				super.onLocationResult(locationResult);
				if (locationResult.getLastLocation() == null) {
					Log.d("PlayerViewModel", "Location is null");
					return;
				}
				Log.d("PlayerViewModel", "Location updated: " + locationResult.getLastLocation().toString());
				location.setValue(locationResult.getLastLocation());
			}
		};
	}
	
	public LocationCallback getLocationCallback() {
		return locationCallback;
	}
	
	public void setSid(String newSid) {
		sid.setValue(newSid);
		prefs.edit().putString("sid", newSid).apply(); }
	public void setPlayerUid(int newUid) {
		uid.setValue(newUid);
		prefs.edit().putInt("uid", newUid).apply(); }
	public void setPlayerProfileVersion(int newVer) {
		profileVersion.setValue(newVer);
		prefs.edit().putInt("profileVersion", newVer).apply(); }
	public void setPlayerWeaponLevel(int newPlayerWeaponLevel) {
		weaponLevel.postValue(newPlayerWeaponLevel);	// postValue as it's called from background threads and isn't very time-sensitive
		prefs.edit().putInt("weaponLevel", newPlayerWeaponLevel).apply(); }
	public void setPlayerArmorLevel(int newPlayerArmorLevel) {
		armorLevel.postValue(newPlayerArmorLevel);		// postValue as it's called from background threads and isn't very time-sensitive
		prefs.edit().putInt("armorLevel", newPlayerArmorLevel).apply(); }
	public void setPlayerAmuletLevel(int newPlayerAmuletLevel) {
		amuletLevel.postValue(newPlayerAmuletLevel);	// postValue as it's called from background threads and isn't very time-sensitive
		prefs.edit().putInt("amuletLevel", newPlayerAmuletLevel).apply(); }
	public void setLocation(Location newLocation) {
		location.setValue(newLocation); }
	
	public LiveData<String> getSid() { return sid; }
	public LiveData<Integer> getPlayerUid() { return uid; }
	public LiveData<Integer> getPlayerProfileVersion() { return profileVersion; }
	public LiveData<Integer> getPlayerWeaponLevel() { return weaponLevel; }
	public LiveData<Integer> getPlayerArmorLevel() { return armorLevel; }
	public LiveData<Integer> getPlayerAmuletLevel() { return amuletLevel; }
	public LiveData<Location> getLocation() { return location; }

}