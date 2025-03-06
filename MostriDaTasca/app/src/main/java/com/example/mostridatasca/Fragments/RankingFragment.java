package com.example.mostridatasca.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.MainActivity;
import com.example.mostridatasca.RecyclerViews.RankingAdapter;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.ViewModels.RankingViewModel;
import com.example.mostridatasca.databinding.FragmentRankingBinding;

import java.util.List;

public class RankingFragment extends Fragment {
	private FragmentRankingBinding binding;
	private PlayerViewModel playerViewModel;
	private RankingViewModel rankingViewModel;
	
	public RankingFragment() {}		// Required empty public constructor
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) requireActivity()).setCurrentFragment(MainActivity.FRAGMENT.RANKING);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
		rankingViewModel = new RankingViewModel();
		rankingViewModel.fetchRanking(playerViewModel.getSid().getValue());
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRankingBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.goBackFromRankingButton.setOnClickListener(this::handleGoBack);
		// Ranking list
		binding.recyclerViewRanking.setLayoutManager(new LinearLayoutManager(requireActivity()));
		rankingViewModel.getRanking().observe(getViewLifecycleOwner(), this::updateUI);
	}
	
	
	private void updateUI(List<ServerResponse.UserRanking> usersRanking) {
		binding.recyclerViewRanking.setAdapter(new RankingAdapter(
				requireActivity().getApplication(),
				getLayoutInflater(),
				usersRanking,
				this::handleGoToUserDetails,
				playerViewModel.getSid().getValue()
		));
	}
	
	private void handleGoToUserDetails(int uid, int profileversion, int hp, int xp, double lat, double lon) {
		Bundle args = new Bundle();
		args.putInt("uid", uid);
		args.putInt("profileversion", profileversion);
		args.putInt("hp", hp);
		args.putInt("xp", xp);
		args.putDouble("lat", lat);
		args.putDouble("lon", lon);
		((MainActivity) requireActivity()).navigateToFragment(MainActivity.FRAGMENT.USER_DETAILS, args);
	}
	
	private void handleGoBack(View view) {
		getParentFragmentManager().popBackStack();
	}
}