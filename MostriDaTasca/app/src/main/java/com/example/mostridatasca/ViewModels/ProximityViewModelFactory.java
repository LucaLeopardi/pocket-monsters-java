package com.example.mostridatasca.ViewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ProximityViewModelFactory implements ViewModelProvider.Factory {
	
	private final PlayerViewModel playerViewModel;
	
	public ProximityViewModelFactory(PlayerViewModel playerViewModel) {
		this.playerViewModel = playerViewModel;
	}
	
	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		if (modelClass.equals(ProximityViewModel.class)) return (T) new ProximityViewModel(playerViewModel);
		else throw new IllegalArgumentException("ERROR::ProximityViewModelFactory::create:: Factory only creates ProximityViewModels. Passed: " + modelClass.getName());
	}
}