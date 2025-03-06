package com.example.mostridatasca.ViewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.RoomData.IObjectDAO;
import com.example.mostridatasca.RoomData.Object;
import com.example.mostridatasca.StorageManager;

public class ObjectDetailsViewModel extends AndroidViewModel {
	private final IObjectDAO database;
	private final MutableLiveData<Object> object;
	
	public ObjectDetailsViewModel(@NonNull Application application) {
		super(application);
		database = StorageManager.getDatabase(application).getObjectDAO();
		object = new MutableLiveData<>();
	}
	
	public LiveData<Object> getObject() { return object; }
	
	/**!!! ASYNC: To be called only from a background thread !!!*/
	public void loadObject(int id, String sid) {
		Object dbObject = database.getObject(id);
		if (dbObject == null) {
			Log.d("ObjectDetailsViewModel", "Object not found in database. Retrieving from server...");
			CommunicationController.getRetrofitClient().getObjectDetails(id, sid).enqueue(CommunicationController.onSuccess(
					(response) -> {
						if (response.body() == null) throw new RuntimeException("ObjectDetailsViewModel: response.body() is null.");
						Object serverObject = new Object(response.body());
						object.setValue(serverObject);
						new Thread(() -> database.addObject(serverObject)).start();
					}));
		} else {
			Log.d("ObjectDetailsViewModel", "Object found in database.");
			object.postValue(dbObject);
		}
	}
}