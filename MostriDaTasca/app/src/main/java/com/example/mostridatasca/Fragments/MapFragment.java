package com.example.mostridatasca.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.ObjectMarkerTag;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.UserMarkerTag;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.ViewModels.ProximityViewModel;
import com.example.mostridatasca.ViewModels.ProximityViewModelFactory;
import com.example.mostridatasca.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {
	private PlayerViewModel playerViewModel;
	private ProximityViewModel proximityViewModel;
	private FragmentMapBinding binding;
	private GoogleMap map;
	private final int defaultZoom = 17;
	private LiveData<Location> playerLocation;
	private Marker playerMarker;
	private Circle playerCircle;
	private Map<Integer, Marker> usersMarkers;
	private Map<Integer, Marker> objectsMarkers;
	
	private final OnMapReadyCallback callback = new OnMapReadyCallback() {
		@Override
		public void onMapReady(@NonNull GoogleMap googleMap) {
			map = googleMap;
			map.getUiSettings().setMapToolbarEnabled(false);
			map.setBuildingsEnabled(false);
			// Custom map style
			Context context = MapFragment.this.getContext();
			if (context == null) Log.w("MapFragment", "Context is null. Can't load style.");
			else {
				try {
					boolean success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style));
					if (!success) Log.e("MapFragment", "Style parsing failed.");
				} catch (Exception e) { Log.e("MapFragment", "Can't load style. Error: ", e); }
			}
			// Marker tap handling
			map.setOnMarkerClickListener(MapFragment.this::handleMarkerClick);
			// First initialization
			if (playerLocation.getValue() == null) {
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.46, 9.22), defaultZoom));
			} else {
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(playerLocation.getValue().getLatitude(), playerLocation.getValue().getLongitude()),
						defaultZoom));
			}
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.MAP);
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		playerViewModel.getPlayerAmuletLevel().observe(this, level -> {
			if (playerCircle != null) playerCircle.setRadius(100 + level);
		});
		
		proximityViewModel = new ViewModelProvider(requireActivity(), new ProximityViewModelFactory(playerViewModel)).get(ProximityViewModel.class);
		usersMarkers = new HashMap<>();
		objectsMarkers = new HashMap<>();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		binding = FragmentMapBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SupportMapFragment mapFragment =
				(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(callback);
		}
		// LiveData setup
		playerLocation = playerViewModel.getLocation();
		playerLocation.observe(getViewLifecycleOwner(), this::updatePlayerLocation);
		proximityViewModel.getNearbyUsers().observe(getViewLifecycleOwner(), this::updateUsersMarkers);
		proximityViewModel.getNearbyObjects().observe(getViewLifecycleOwner(), this::updateObjectsMarkers);
		// Buttons click listeners
		binding.goFromMapToObjectsNearbyButton.setOnClickListener(this::handleNavigationPress);
		binding.goFromMapToRankingButton.setOnClickListener(this::handleNavigationPress);
		binding.goFromMapToSettingsButton.setOnClickListener(this::handleNavigationPress);
		binding.centerMapOnPlayerButton.setOnClickListener(this::centerMapOnPlayer);
	}
	
	private void updatePlayerLocation(Location location) {
		if (map == null) return;
		if (playerMarker == null) {
			playerMarker = map.addMarker(new MarkerOptions()
					.position(new LatLng(location.getLatitude(), location.getLongitude()))
					.anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory.fromBitmap(scaleIcon(R.drawable.player_icon))));
			centerMapOnPlayer(null);
		}
		if (playerCircle == null) {
			playerCircle = map.addCircle(new com.google.android.gms.maps.model.CircleOptions()
					.center(new LatLng(location.getLatitude(), location.getLongitude()))
					.radius(100 + playerViewModel.getPlayerAmuletLevel().getValue())
					.strokeWidth(0)
					.fillColor(0x50ffe070));
		}
		playerMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
		playerCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
	}
	
	private void updateUsersMarkers(List<ServerResponse.UserNearby> usersNearby) {
		if (map == null || usersMarkers == null || usersNearby == null) return;
		Log.d("MapFragment", "Updating users markers. List size: " + usersNearby.size());
		Map<Integer, ServerResponse.UserNearby> usersMap = ServerResponse.UserNearby.toMap(usersNearby);
		Iterator<Map.Entry<Integer, Marker>> iterator = usersMarkers.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<Integer, Marker> entry = iterator.next();
			ServerResponse.UserNearby user = usersMap.get(entry.getKey());
			if(user == null) {
				// Remove Marker of user no longer in list
				entry.getValue().remove();
				iterator.remove();
			} else {
				// Update position of Markers of users that are still in the list
				entry.getValue().setPosition(new LatLng(user.getLat(), user.getLon()));
				usersMap.remove(entry.getKey());
			}
		}
		// Add Markers of new users nearby (aka all the ones left in the Map)
		for (ServerResponse.UserNearby user : usersMap.values()) {
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(user.getLat(), user.getLon()))
					.anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory.fromBitmap(scaleIcon(R.drawable.user_icon))));
			if (marker == null) {
				Log.w("MapFragment", "Error adding marker for user " + user.getUid());
				continue;
			}
			marker.setTag(new UserMarkerTag(user.getUid(), user.getProfileVersion(), user.getLife(), user.getExperience()));
			usersMarkers.put(user.getUid(), marker);
		}
	}
	
	private void updateObjectsMarkers(List<ServerResponse.ObjectNearby> objectsNearby) {
		if (map == null || objectsMarkers == null || objectsNearby == null) return;
		Log.d("MapFragment", "Updating objects markers. List size: " + objectsNearby.size());
		Map<Integer, ServerResponse.ObjectNearby> objectsMap = ServerResponse.ObjectNearby.toMap(objectsNearby);
		Iterator<Map.Entry<Integer, Marker>> iterator = objectsMarkers.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<Integer, Marker> entry = iterator.next();
			ServerResponse.ObjectNearby object = objectsMap.get(entry.getKey());
			if(object == null) {
				// Remove Marker of object no longer in list
				entry.getValue().remove();
				iterator.remove();
			} else {
				// Update position of Markers of objects that are still in the list
				entry.getValue().setPosition(new LatLng(object.getLat(), object.getLon()));
				objectsMap.remove(entry.getKey());
			}
		}
		// Add Markers of new objects nearby (aka all the ones left in the Map)
		for (ServerResponse.ObjectNearby object : objectsMap.values()) {
			int image;
			switch (object.getType()) {
				case "weapon": image = R.drawable.weapon_icon; break;
				case "armor": image = R.drawable.armor_icon; break;
				case "amulet": image = R.drawable.amulet_icon; break;
				case "candy": image = R.drawable.candy_icon; break;
				default: image = R.drawable.monster_icon; break;
			}
			
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(object.getLat(), object.getLon()))
					.anchor(0.5f, 0.5f)
					.icon(BitmapDescriptorFactory.fromBitmap(scaleIcon(image))));
			if (marker == null) {
				Log.w("MapFragment", "Error adding marker for object " + object.getId());
				continue;
			}
			marker.setTag(new ObjectMarkerTag(object.getId()));
			objectsMarkers.put(object.getId(), marker);
		}
	}
	
	private void centerMapOnPlayer(View view) {
		if (map == null || playerLocation.getValue() == null) return;
		LatLng newLocation = new LatLng(
				playerLocation.getValue().getLatitude(),
				playerLocation.getValue().getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, defaultZoom));
	}
	
	private boolean handleMarkerClick(Marker marker) {
		Object tag = marker.getTag();
		Bundle bundle = new Bundle();
		MainActivity.FRAGMENT destination = MainActivity.FRAGMENT.MAP;	// Default value to make Android Studio happy
		if (tag instanceof UserMarkerTag) {
			Log.d("MapFragment", "Clicked on user " + ((UserMarkerTag) tag).uid);
			bundle.putInt("uid", ((UserMarkerTag) tag).uid);
			bundle.putInt("profileversion", ((UserMarkerTag) tag).profileversion);
			bundle.putInt("hp", ((UserMarkerTag) tag).hp);
			bundle.putInt("xp", ((UserMarkerTag) tag).xp);
			bundle.putDouble("lat", marker.getPosition().latitude);
			bundle.putDouble("lon", marker.getPosition().longitude);
			destination = MainActivity.FRAGMENT.USER_DETAILS;
			}
		if (tag instanceof ObjectMarkerTag) {
			Log.d("MapFragment", "Clicked on object " + ((ObjectMarkerTag) tag).id);
			bundle.putInt("id", ((ObjectMarkerTag) tag).id);
			bundle.putDouble("lat", marker.getPosition().latitude);
			bundle.putDouble("lon", marker.getPosition().longitude);
			destination = MainActivity.FRAGMENT.OBJECT_DETAILS;
		}
		((MainActivity) requireActivity()).navigateToFragment(destination, bundle);
		return true;
	}
	
	private void handleNavigationPress(View view) {
		MainActivity.FRAGMENT destination = null;
		int id = view.getId();
		// Switch statement is not possible because cases must be constant expressions. I hate this.
		if (id == binding.goFromMapToObjectsNearbyButton.getId()) {
			destination = MainActivity.FRAGMENT.OBJECTS_NEARBY;
		} else if (id == binding.goFromMapToRankingButton.getId()) {
			destination = MainActivity.FRAGMENT.RANKING;
		} else if (id == binding.goFromMapToSettingsButton.getId()) {
			destination = MainActivity.FRAGMENT.PLAYER_PROFILE;
		}
		if (destination == null) return;
		((MainActivity) requireActivity()).navigateToFragment(destination);
	}
	
	private Bitmap scaleIcon(int icon) {
		return Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), icon),
				100,
				100,
				false
		);
	}
}