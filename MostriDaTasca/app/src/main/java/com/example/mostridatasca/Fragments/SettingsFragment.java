package com.example.mostridatasca.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.ViewModels.UserDetailsViewModel;
import com.example.mostridatasca.databinding.FragmentSettingsBinding;

import java.io.InputStream;


public class SettingsFragment extends Fragment {
	private FragmentSettingsBinding binding;
	private PlayerViewModel playerViewModel;
	private MutableLiveData<ServerResponse.UserDetails> player;
	private User newPlayer;
	private ActivityResultLauncher<String> picturePickerLauncher;
	
	public SettingsFragment() {}		// Required empty public constructor
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		picturePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),this::loadNewImage);
		
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		Integer uid = playerViewModel.getPlayerUid().getValue();
		String sid = playerViewModel.getSid().getValue();
		if (uid == null  || sid == null) {
			Log.e("SettingsFragment", "onCreate: uid, profileversion or sid is null");
			return;
		}
		
		player = new MutableLiveData<>();
		CommunicationController.getRetrofitClient().getUserDetails(uid, sid).enqueue(CommunicationController.onSuccess(
				(response) -> player.setValue(response.body())
		));
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSettingsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		player.observe(getViewLifecycleOwner(), this::loadData);
		binding.confirmButtonPlayerProfile.setEnabled(false);
		binding.confirmButtonPlayerProfile.setAlpha(0.5f);
	}
	
	
	private void loadData(ServerResponse.UserDetails data) {
		if (data == null) return;
		newPlayer = new User(data);
		newPlayer.name = null;		// To simplify checkConfirmButtonEnabled()
		newPlayer.picture = null;	// To simplify checkConfirmButtonEnabled()
		
		binding.namePlayerProfile.setHint("✏️" + data.getName());
		binding.positionShareSwitchPlayerProfile.setChecked(data.getPositionShare());
		// Picture
		Bitmap picture;
		Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.user_icon);;
		if (data.getPicture() != null) {
			byte[] pictureBytes = Base64.decode(data.getPicture(), Base64.DEFAULT);
			picture = BitmapFactory.decodeByteArray((pictureBytes), 0, pictureBytes.length);
		} else {
			picture = icon;
		}
		binding.picturePlayerProfile.setImageBitmap(picture);
		
		// New player data update
		binding.namePlayerProfile.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				newPlayer.name = s.toString();
				checkConfirmButtonEnabled();
			}
		});
		// Picture
		binding.changeImageButtonSettings.setOnClickListener(v -> picturePickerLauncher.launch("image/*"));
		// PositionShare
		binding.positionShareSwitchPlayerProfile.setOnCheckedChangeListener((v, isChecked) -> {
			newPlayer.positionshare = isChecked;
			checkConfirmButtonEnabled();
		});
		// Confirm button
		binding.confirmButtonPlayerProfile.setOnClickListener(this::updatePlayerData);
	}
	
	private void checkConfirmButtonEnabled() {
		if (player.getValue() == null) return;
		if (newPlayer.name == null && newPlayer.picture == null && newPlayer.positionshare == player.getValue().getPositionShare()) {
			binding.confirmButtonPlayerProfile.setEnabled(false);
			binding.confirmButtonPlayerProfile.setAlpha(0.5f);
		} else {
			binding.confirmButtonPlayerProfile.setEnabled(true);
			binding.confirmButtonPlayerProfile.setAlpha(1f);
		}
	}
	
	private void loadNewImage(Uri uri) {
		if (uri == null) return;
		try {
			// Set new Base64 picture
			InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
			if (inputStream == null) return;
			if (inputStream.available() > 100*1024) {
				Log.e("SettingsFragment", "loadNewImage: Image too big: over 100KB.");
				return;
			}
			byte[] bytes = new byte[inputStream.available()];
			if (inputStream.read(bytes) == -1) return;
			newPlayer.picture = Base64.encodeToString(bytes, Base64.DEFAULT);
			inputStream.close();
			// Show new picture in Settings
			Bitmap picture = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
			binding.picturePlayerProfile.setImageBitmap(picture);
			checkConfirmButtonEnabled();
		} catch (Exception e) {
			Log.e("SettingsFragment", "loadNewImage: " + e.getMessage());
		}
	}
	
	private void updatePlayerData(View v) {
		CommunicationController.getRetrofitClient().updateUser( playerViewModel.getSid().getValue(), newPlayer.uid, newPlayer.name, newPlayer.picture, newPlayer.positionshare)
				.enqueue(CommunicationController.onSuccess((result) -> {}));
		
		playerViewModel.setPlayerProfileVersion(newPlayer.profileversion + 1);
		Log.d("SettingsFragment", "Updated player data: " + newPlayer.name + " " + newPlayer.positionshare);
		((MainActivity) requireActivity()).navigateToFragment(MainActivity.FRAGMENT.MAP);
	}
}