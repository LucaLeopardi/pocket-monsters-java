package com.example.mostridatasca.RecyclerViews;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.RoomData.IObjectDAO;
import com.example.mostridatasca.RoomData.Object;
import com.example.mostridatasca.StorageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectsAdapter extends RecyclerView.Adapter<ObjectViewHolder> {
	
	private final Application application;
	private final LayoutInflater inflater;
	private final IObjectsRecyclerViewListener listener;
	private final LiveData<Location> playerLocation;
	private final LiveData<Integer> playerRangeBonus;
	private final String sid;
	private final List<ServerResponse.ObjectNearby> objects;
	private final HashMap<ServerResponse.ObjectNearby, Integer> objectsDistance;
	
	public ObjectsAdapter(Application application, LayoutInflater inflater, IObjectsRecyclerViewListener listener, LiveData<Location> playerLocation, LiveData<Integer> playerRangeBonus, String sid) {
		this.application = application;
		this.inflater = inflater;
		this.listener = listener;
		this.playerLocation = playerLocation;
		this.playerRangeBonus = playerRangeBonus;
		this.sid = sid;
		this.objects = new ArrayList<>();
		this.objectsDistance = new HashMap<>();
	}
	
	public void updateObjects(List<ServerResponse.ObjectNearby> newObjects) {
		// Get distance data for new objects list
		if (playerLocation.getValue() == null) {
			Log.e("ERROR::ObjectsAdapter", "updateObjects::playerLocation is null. Cannot calculate distance.");
			return;
		}
		objectsDistance.clear();
		for (ServerResponse.ObjectNearby newObject : newObjects) {
			Location objectLocation = new Location("");
			objectLocation.setLatitude(newObject.getLat());
			objectLocation.setLongitude(newObject.getLon());
			objectsDistance.put(newObject, Math.round(playerLocation.getValue().distanceTo(objectLocation)));
		}
		
		// Sort objects by using HashMap distance data
		newObjects.sort((obj1, obj2) -> Integer.compare(objectsDistance.get(obj1), objectsDistance.get(obj2)));
		
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
			@Override
			public int getOldListSize() {
				return objects.size();
			}
			@Override
			public int getNewListSize() {
				return newObjects.size();
			}
			@Override
			public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
				return objects.get(oldItemPosition).getId() == (newObjects.get(newItemPosition).getId());
			}
			@Override
			public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
				return objects.get(oldItemPosition).equals(newObjects.get(newItemPosition));
			}
		});
		
		objects.clear();
		objects.addAll(newObjects);
		// Notify adapter of changes
		diffResult.dispatchUpdatesTo(this);
	}
	
	@NonNull
	@Override
	public ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View item = inflater.inflate(R.layout.list_item_object, parent, false);
		return new ObjectViewHolder(item, listener);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ObjectViewHolder holder, int position) {
		
		ServerResponse.ObjectNearby objectNearby = objects.get(position);
		if (!objectsDistance.containsKey(objectNearby) || playerRangeBonus.getValue() == null) {
			Log.e("ERROR::ObjectsAdapter", "onBindViewHolder::playerLocation or playerRangeBonus is null. Cannot calculate distance.");
			return;
		}
		//noinspection DataFlowIssue
		boolean inRange = objectsDistance.get(objectNearby) <= (100 + playerRangeBonus.getValue());
		
		MutableLiveData<Object> data = new MutableLiveData<>();
		data.observeForever(new Observer<Object>() {
			@Override
			public void onChanged(Object objectDetails) {
				holder.bind(objectDetails, objectsDistance.get(objectNearby), inRange, objectNearby.getLat(), objectNearby.getLon());
				data.removeObserver(this);
			}
		});
		
		new Thread(() -> {
			IObjectDAO database = StorageManager.getDatabase(application).getObjectDAO();
			Object dbObject = database.getObject(objectNearby.getId());
			
			if (dbObject != null) {
				data.postValue(dbObject);
			} else {
				CommunicationController.getRetrofitClient().getObjectDetails(objectNearby.getId(), sid)
						.enqueue(CommunicationController.onSuccess(
								(response) -> {
									if (response.body() == null) throw new RuntimeException("ERROR::ObjectsAdapter::onBindViewHolder::onResponse::response.body() is null");
									Object object = new Object(response.body());
									new Thread(() -> database.addObject(object)).start();
									data.postValue(object);
								}
						));
			}
		}).start();
	}
	
	@Override
	public int getItemCount() { return objects.size(); }
}