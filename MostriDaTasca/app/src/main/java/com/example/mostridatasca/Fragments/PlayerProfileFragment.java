package com.example.mostridatasca.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.FragmentPlayerProfileBinding;

public class PlayerProfileFragment extends Fragment {
	private FragmentPlayerProfileBinding binding;
	private PlayerViewModel playerViewModel;
	private MutableLiveData<ServerResponse.UserDetails> player;
	
	public PlayerProfileFragment() {}		// Required empty public constructor
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.PLAYER_PROFILE, getArguments());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playerViewModel = new PlayerViewModel(requireActivity().getApplication());
		Log.d("PlayerProfileFragment", "onCreate: {WeaponLevel: " + playerViewModel.getPlayerWeaponLevel().getValue() + ", ArmorLevel: " + playerViewModel.getPlayerArmorLevel().getValue() + ", AmuletLevel: " + playerViewModel.getPlayerAmuletLevel().getValue() + "}");
		Integer uid = playerViewModel.getPlayerUid().getValue();
		String sid = playerViewModel.getSid().getValue();
		if (uid == null || sid == null) {
			Log.e("PlayerProfileFragment", "ERROR::PlayerViewModel: PlayerUid is null");
			return;
		}
		
		player = new MutableLiveData<>();
		CommunicationController.getRetrofitClient().getUserDetails(uid, sid).enqueue(CommunicationController.onSuccess(
				(response) -> player.setValue(response.body())
		));
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentPlayerProfileBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.goBackFromPlayerProfile.setOnClickListener(this::handleGoBack);
		getChildFragmentManager().beginTransaction()
				.add(binding.settingsContainerPlayerProfile.getId(), SettingsFragment.class, null)
				.commit();
		player.observe(getViewLifecycleOwner(), this::updateUserUI);
	}
	
	
	private void updateUserUI(ServerResponse.UserDetails data) {
		binding.statsPlayerProfile.setText("HP: " + data.getLife() + "  |  XP: " + data.getExperience());
		// Equipment slots
		ItemSlotFragment weaponSlot = new ItemSlotFragment();
		ItemSlotFragment armorSlot = new ItemSlotFragment();
		ItemSlotFragment amuletSlot = new ItemSlotFragment();
		getChildFragmentManager().beginTransaction()
				.add(binding.weaponContainerPlayerProfile.getId(), weaponSlot, null)
				.add(binding.armorContainerPlayerProfile.getId(), armorSlot, null)
				.add(binding.amuletContainerPlayerProfile.getId(), amuletSlot, null)
				.commitNow();
		Log.d("PlayerProfileFragment", "Equipment IDs: {Weapon: " + data.getWeapon() + ", Armor: " + data.getArmor() + ", Amulet: " + data.getAmulet() + "}");
		weaponSlot.loadObject(data.getWeapon(), "weapon", playerViewModel.getSid().getValue());
		armorSlot.loadObject(data.getArmor(), "armor", playerViewModel.getSid().getValue());
		amuletSlot.loadObject(data.getAmulet(), "amulet", playerViewModel.getSid().getValue());
	}
	
	private void handleGoBack(View view) { getParentFragmentManager().popBackStack(); }
}