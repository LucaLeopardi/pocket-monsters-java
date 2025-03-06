package com.example.mostridatasca.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RecyclerViews.ObjectsAdapter;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.ViewModels.ProximityViewModel;
import com.example.mostridatasca.databinding.FragmentObjectsNearbyBinding;

import java.util.List;

public class ObjectsNearbyFragment extends Fragment {
	private FragmentObjectsNearbyBinding binding;
	private PlayerViewModel playerViewModel;
	private ProximityViewModel proximityViewModel;
	
	public ObjectsNearbyFragment() {}		// Required empty public constructor
	public static ObjectsNearbyFragment newInstance() { return new ObjectsNearbyFragment();	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.OBJECTS_NEARBY);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		proximityViewModel = new ViewModelProvider(requireActivity()).get(ProximityViewModel.class);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentObjectsNearbyBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.goBackFromObjectsNearbyButton.setOnClickListener(this::handleGoBack);
		// Objects nearby list
		binding.recyclerViewObjects.setLayoutManager(new LinearLayoutManager(requireActivity()));
		ObjectsAdapter adapter = new ObjectsAdapter(
				requireActivity().getApplication(),
				getLayoutInflater(),
				this::handleGoToObjectDetails,
				playerViewModel.getLocation(),
				playerViewModel.getPlayerAmuletLevel(),
				playerViewModel.getSid().getValue());
		binding.recyclerViewObjects.setAdapter(adapter);
		if (binding.recyclerViewObjects.getItemAnimator() instanceof SimpleItemAnimator) ((SimpleItemAnimator) binding.recyclerViewObjects.getItemAnimator()).setSupportsChangeAnimations(false);	// Removes jitter on update
		proximityViewModel.getNearbyObjects().observe(getViewLifecycleOwner(), adapter::updateObjects);
	}
	
	
	private void handleGoToObjectDetails(int id, double lat, double lon) {
		Bundle args = new Bundle();
		args.putInt("id", id);
		args.putDouble("lat", lat);
		args.putDouble("lon", lon);
		((MainActivity) requireActivity()).navigateToFragment(MainActivity.FRAGMENT.OBJECT_DETAILS, args);
	}
	
	private void handleGoBack(View view) {
		getParentFragmentManager().popBackStack();
	}
}