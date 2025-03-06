package com.example.mostridatasca.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.FragmentRegistrationBinding;

public class RegistrationFragment extends Fragment {
	private PlayerViewModel playerViewModel;
	private FragmentRegistrationBinding binding;
	
	public RegistrationFragment() {}	// Required empty public constructor
	public static RegistrationFragment newInstance() { return new RegistrationFragment(); }

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.REGISTRATION);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		
		binding = FragmentRegistrationBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.signUpButtonRegistration.setOnClickListener(this::handleSignUpClick);
	}
	
	private void handleSignUpClick(View view) {
		binding.signUpButtonRegistration.setEnabled(false);
		binding.signUpButtonRegistration.setAlpha(0.5f);
		CommunicationController.getRetrofitClient().signUp().enqueue(CommunicationController.onSuccess(
				(response) -> {
					if (response.body() == null) throw new RuntimeException("RegistrationFragment: response.body() is null.");
					playerViewModel.setSid(response.body().getSid());
					playerViewModel.setPlayerUid(response.body().getUid());
					openSettings();
				}));
	}
	
	private void openSettings() {
		binding.signUpButtonRegistration.setVisibility(View.GONE);
		getChildFragmentManager().beginTransaction()
				.add(binding.settingsContainerRegistration.getId(), SettingsFragment.class, null)
				.commit();
	}
}