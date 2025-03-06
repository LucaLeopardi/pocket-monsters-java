package com.example.mostridatasca.ViewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.RetrofitData.IServerApi;
import com.example.mostridatasca.RoomData.IUserDAO;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.StorageManager;

public class UserDetailsViewModel extends AndroidViewModel {
	private final IUserDAO database;
	private final MutableLiveData<User> user;
	
	public UserDetailsViewModel(@NonNull Application application) {
		super(application);
		database = StorageManager.getDatabase(application).getUserDAO();
		user = new MutableLiveData<>();
	}
	
	public LiveData<User> getUser() { return user; }
	
	public void loadUser(int uid, int profileversion, int hp, int xp, double lat, double lon, String sid) {
		User dbUser = database.getUser(uid);
		if (dbUser == null || dbUser.profileversion < profileversion){
			if (dbUser != null) Log.d("UserDetailsViewModel", "User found in database, but outdated. Needed: " + profileversion + ", found: " + dbUser.profileversion + ".");
			else Log.d("UserDetailsViewModel", "User not found in database.");
			Log.d("UserDetailsViewModel", "Up-to-date User not found in database. Retrieving from server...");
			CommunicationController.getRetrofitClient().getUserDetails(uid, sid).enqueue(CommunicationController.onSuccess(
					(response) -> {
						if (response.body() == null) throw new RuntimeException("UserDetailsViewModel: response.body() is null.");
						User serverUser = new User(response.body());
						user.setValue(serverUser);
						new Thread(() -> database.addUser(serverUser)).start();
					}));
		} else {
			Log.d("UserDetailsViewModel", "User found in database.");
			// Data that may not be updated in database
			dbUser.life = hp;
			dbUser.experience = xp;
			dbUser.lat = lat;
			dbUser.lon = lon;
			user.postValue(dbUser);
		}
	}
	
	/**!!! To be used only for player character !!!*/
	public void updateUser(String sid, User user) {
		IServerApi retrofit = CommunicationController.getRetrofitClient();
		retrofit.updateUser( sid, user.uid, user.name, user.picture, user.positionshare)
				.enqueue(CommunicationController.onSuccess((result) -> {}));
	}
}