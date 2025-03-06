package com.example.mostridatasca.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.ViewModels.UserDetailsViewModel;
import com.example.mostridatasca.databinding.FragmentUserDetailsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class UserDetailsFragment extends Fragment {
	private FragmentUserDetailsBinding binding;
	private PlayerViewModel playerViewModel;
	private UserDetailsViewModel userDetailsViewModel;
	private LiveData<User> user;
	
	public UserDetailsFragment() {}		// Required empty public constructor
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.USER_DETAILS, getArguments());
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		userDetailsViewModel = new ViewModelProvider(this).get(UserDetailsViewModel.class);
		user = userDetailsViewModel.getUser();
		
		int uid, profileversion, hp, xp;
		double lat, lon;
		if (getArguments() != null) {
			uid = getArguments().getInt("uid");
			profileversion = getArguments().getInt("profileversion");
			hp = getArguments().getInt("hp");
			xp = getArguments().getInt("xp");
			lat = getArguments().getDouble("lat");
			lon = getArguments().getDouble("lon");
		} else {
			throw new RuntimeException("UserDetailsFragment: getArguments() is null. Cannot retrieve uid.");
		}
		// Load user details from DB or server, so it's got to be on a background thread
		new Thread(() -> userDetailsViewModel.loadUser(uid, profileversion, hp, xp, lat, lon, playerViewModel.getSid().getValue())).start();
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentUserDetailsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.goBackFromUserDetailsButton.setOnClickListener(this::handleGoBack);
		user.observe(getViewLifecycleOwner(), this::updateUI);
	}
	
	
	private void updateUI(User user) {
		if (user == null) return;
		binding.nameUserDetails.setText(user.name);
		binding.statsUserDetails.setText("HP: " + user.life + "  |  XP: " + user.experience);
		// Picture
		Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.user_icon);;
		Bitmap picture;
		if (user.picture != null) {
			byte[] pictureBytes = Base64.decode(user.picture, Base64.DEFAULT);
			picture = BitmapFactory.decodeByteArray((pictureBytes), 0, pictureBytes.length);
		} else {
			picture = icon;
		}
		binding.pictureUserDetails.setImageBitmap(picture);
		// Minimap
		if (user.positionshare) {
			SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapUserDetails);
			if (mapFragment != null)
				mapFragment.getMapAsync(map -> initializeMap(map, user, BitmapDescriptorFactory.fromBitmap(scaleIcon(icon))));
		} else {
			binding.mapUserDetails.setVisibility(View.GONE);
			binding.noPositionSharePlayerDetails.setVisibility(View.VISIBLE);
		}
	}
	
	private void initializeMap(GoogleMap map, User user, BitmapDescriptor icon) {
		// Custom style
		Context context = UserDetailsFragment.this.getContext();
		if (context == null) Log.e("MapFragment", "Context is null. Can't load style.");
		else {
			try {
				boolean success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style));
				if (!success) Log.e("MapFragment", "Style parsing failed.");
			} catch (Exception e) {
				Log.e("MapFragment", "Can't load style. Error: ", e);
			}
		}
		map.getUiSettings().setAllGesturesEnabled(false);
		map.setOnMarkerClickListener(marker -> true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(user.lat, user.lon), 16));
		map.addMarker(new MarkerOptions()
				.position(new LatLng(user.lat, user.lon))
				.anchor(0.5f, 0.5f)
				.icon(icon));
	}
	
	private void handleGoBack(View view) { getParentFragmentManager().popBackStack(); }
	
	private Bitmap scaleIcon(Bitmap icon) {	return Bitmap.createScaledBitmap(icon,100,100,false);	}
}