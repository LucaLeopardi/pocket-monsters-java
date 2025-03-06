package com.example.mostridatasca.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.RoomData.Object;
import com.example.mostridatasca.StorageManager;
import com.example.mostridatasca.ViewModels.ObjectDetailsViewModel;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.DialogObjectActivationBinding;
import com.example.mostridatasca.databinding.FragmentObjectDetailsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Objects;

public class ObjectDetailsFragment extends Fragment {
	private FragmentObjectDetailsBinding binding;
	private PlayerViewModel playerViewModel;
	private ObjectDetailsViewModel objectDetailsViewModel;
	private LiveData<Object> object;
	private double objectLat, objectLon;

	public ObjectDetailsFragment() {}		// Required empty public constructor
	public static ObjectDetailsFragment newInstance() { return new ObjectDetailsFragment();	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.OBJECT_DETAILS, getArguments());
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		objectDetailsViewModel = new ViewModelProvider(this).get(ObjectDetailsViewModel.class);
		object = objectDetailsViewModel.getObject();
		
		int id;
		if (getArguments() != null) {
			id = getArguments().getInt("id");
			objectLat = getArguments().getDouble("lat");
			objectLon = getArguments().getDouble("lon");
		}
		else throw new RuntimeException("ObjectDetailsFragment: getArguments() is null. Cannot retrieve id.");
		new Thread(() -> objectDetailsViewModel.loadObject(id, playerViewModel.getSid().getValue())).start();
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentObjectDetailsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.goBackFromObjectDetailsButton.setOnClickListener(this::handleGoBack);
		object.observe(getViewLifecycleOwner(), this::updateUI);
	}
	
	private void updateUI(Object object) {
		if (object == null || playerViewModel.getLocation().getValue() == null || playerViewModel.getPlayerAmuletLevel().getValue() == null) return;
		
		Location objectLocation = new Location("");
		objectLocation.setLatitude(objectLat);
		objectLocation.setLongitude(objectLon);
		int distance = (int) objectLocation.distanceTo(playerViewModel.getLocation().getValue());
		int playerRange = 100 + playerViewModel.getPlayerAmuletLevel().getValue();
		
		binding.nameObjectDetails.setText(object.name);
		binding.statsObjectDetails.setText(object.getStats());
		binding.effectsObjectDetails.setText(Objects.equals(object.type, "monster") ? "Danger" : "Effects");
		binding.descriptionObjectDetails.setText(getDescription(object));
		binding.distanceTextObjectDetails.setText("Distance: " + distance + "m");
		
		if (object.type.equals("monster") || object.type.equals("candy")) binding.compareTextObjectDetails.setVisibility(View.GONE);
		else binding.compareTextObjectDetails.setText(getCurrentEffect(object.type));

		// Interaction button
		if (distance <= playerRange) binding.activateButtonObjectDetails.setText(object.getInteractionType());
		else {
			binding.activateButtonObjectDetails.setText("Too far");
			binding.activateButtonObjectDetails.setEnabled(false);
			binding.activateButtonObjectDetails.setAlpha(0.5f);
		}
		binding.activateButtonObjectDetails.setOnClickListener(this::handleActivateObject);
		
		// Image
		int resource;
		switch (object.type) {
			case "weapon": resource = R.drawable.weapon_icon; break;
			case "armor": resource = R.drawable.armor_icon; break;
			case "amulet": resource = R.drawable.amulet_icon; break;
			case "monster": resource = R.drawable.monster_icon; break;
			case "candy": default: resource = R.drawable.candy_icon; break;
		}
		Bitmap icon = BitmapFactory.decodeResource(getResources(), resource);
		Bitmap image;
		if (object.image != null) {
			byte[] imageBytes = Base64.decode(object.image, Base64.DEFAULT);
			image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		} else {
			image = icon;
		}
		binding.imageObjectDetails.setImageBitmap(image);
		// Minimap
		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapObjectDetails);
		if (mapFragment != null) mapFragment.getMapAsync(map -> initializeMap(map, BitmapDescriptorFactory.fromBitmap(scaleIcon(icon))));
	}
	
	public String getDescription(Object object) {
		switch (object.type) {
			case "weapon": return "Grants " + object.level + "% Damage Resistance.";
			case "armor": return "Increases Max HP by " + object.level + " points.";
			case "amulet": return "Increases map reach by " + object.level + "%.";
			case "candy": return "Restores " + object.level + "-" + object.level*2 + " HP.";
			case "monster": {
				int minDamage = object.level - (int) ( (object.level / 100f) * playerViewModel.getPlayerWeaponLevel().getValue());
				int maxDamage = object.level*2 - (int) ( (object.level / 50f) * playerViewModel.getPlayerWeaponLevel().getValue());
				return "Deals " + minDamage + "-" + maxDamage + " damage.";
			}
		}
		return null;
	}
	
	private String getCurrentEffect(String type) {
		switch (type) {
			case "weapon": return "Current: " + playerViewModel.getPlayerWeaponLevel().getValue() + "%";
			case "armor": return "Current: +" + playerViewModel.getPlayerArmorLevel().getValue() + " HP";
			case "amulet": return "Current: " + playerViewModel.getPlayerAmuletLevel().getValue() + "%";
		}
		return null;
	}
	
	private void initializeMap(GoogleMap map, BitmapDescriptor icon) {
		// Custom style
		Context context = ObjectDetailsFragment.this.getContext();
		if (context == null) Log.e("ObjectDetailsFragment", "Context is null. Can't load style.");
		else {
			try {
				boolean success = map.setMapStyle(
						com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
								context, R.raw.custom_map_style));
				if (!success) Log.e("ObjectDetailsFragment", "Style parsing failed.");
			} catch (Exception e) {
				Log.e("ObjectDetailsFragment", "Can't find style. Error: ", e);
			}
		}
		map.getUiSettings().setAllGesturesEnabled(false);
		map.setOnMarkerClickListener(marker -> true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(objectLat, objectLon), 16));
		map.addMarker(new MarkerOptions()
				.position(new LatLng(objectLat, objectLon))
				.anchor(0.5f, 0.5f)
				.icon(icon));
	}
	
	private void handleActivateObject(View view) {
		if (object.getValue() == null) return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setCancelable(false);
		DialogObjectActivationBinding dialogBinding = DialogObjectActivationBinding.inflate(getLayoutInflater());
		builder.setView(dialogBinding.getRoot());
		Dialog dialog = builder.create();
		dialog.show();
		
		CommunicationController.getRetrofitClient()
				.activateObject(playerViewModel.getSid().getValue(), object.getValue().id).enqueue(CommunicationController.onSuccess(
					(result) -> showActivationResultPopup(dialog, dialogBinding, result.body())
		));
	}
	
	private void showActivationResultPopup(Dialog dialog, DialogObjectActivationBinding dialogBinding, ServerResponse.ObjectActivation result) {
		Object obj = object.getValue();
		if (obj == null || result == null) return;
		
		// Update PlayerViewModel for easy access to equipment levels
		if (result.getDied()) {
			playerViewModel.setPlayerWeaponLevel(0);
			playerViewModel.setPlayerArmorLevel(0);
			playerViewModel.setPlayerAmuletLevel(0);
		} else {
			if (result.getWeapon() != 0) new Thread(() -> playerViewModel.setPlayerWeaponLevel(getObjectLevel(result.getWeapon()))).start();
			if (result.getArmor() != 0) new Thread(() -> playerViewModel.setPlayerArmorLevel(getObjectLevel(result.getArmor()))).start();
			if (result.getAmulet() != 0) new Thread(() -> playerViewModel.setPlayerAmuletLevel(getObjectLevel(result.getAmulet()))).start();
		}
		// Populate pop-up message
		String popUpMessage = null;
		switch (obj.type) {
			case "weapon": popUpMessage = obj.name + " equipped!\n+" + obj.level + " Damage Resistance"; break;
			case "armor": popUpMessage = obj.name + " equipped!\n+" + obj.level + " Max HP"; break;
			case "amulet": popUpMessage = obj.name + " equipped!\n+" + obj.level + "% Map interaction range"; break;
			case "candy": popUpMessage = obj.name + " eaten!\nYou now have " + result.getLife() + " HP"; break;
			case "monster": {
				if (!result.getDied()) popUpMessage = "You won!\nYou gained " + obj.level + " experience\n\nCurrent XP: " + result.getExperience() + "\nCurrent HP: " + result.getLife();
				else popUpMessage = "You died!\nAll items and experience lost";
				break;
			}
		}
		dialogBinding.messagePopup.setText(popUpMessage);
		dialogBinding.okButtonPopup.setOnClickListener((v) -> {
			dialog.dismiss();
			((MainActivity) requireActivity()).navigateToFragment(MainActivity.FRAGMENT.MAP);
		});
	}
	
	/**!!! Only to be called on background thread !!!*/
	private int getObjectLevel(int id) {
		// Try to get data from database
		Object dbData = StorageManager.getDatabase(getContext()).getObjectDAO().getObject(id);
		if (dbData != null) return dbData.level;
		// Otherwise, from server
		ServerResponse.ObjectDetails serverData;
		try {
			serverData = CommunicationController.getRetrofitClient().getObjectDetails(id, playerViewModel.getSid().getValue()).execute().body();
		} catch (IOException e) {
			return -1;
		}
		if (serverData != null) return serverData.getLevel();
		// If everything fails, return error value
		return -1;
	}
	
	private void handleGoBack(View view) {
		getParentFragmentManager().popBackStack();
	}
	
	private Bitmap scaleIcon(Bitmap icon) {
		return Bitmap.createScaledBitmap(icon,100,100,false);
	}
}