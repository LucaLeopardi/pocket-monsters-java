package com.example.mostridatasca.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.RetrofitData.ServerResponse;

import java.util.List;

public class RankingViewModel extends ViewModel {
	
	private final MutableLiveData<List<ServerResponse.UserRanking>> ranking;
	
	public RankingViewModel() {
		ranking = new MutableLiveData<>();
	}
	
	public LiveData<List<ServerResponse.UserRanking>> getRanking() { return ranking; }
	
	public void fetchRanking(String sid) {
		CommunicationController.getRetrofitClient().getRanking(sid)
				.enqueue(CommunicationController.onSuccess(
						(response) -> {
							if (response.body() == null) throw new RuntimeException("ERROR::RankingViewModel::getRanking: response.body() is null");
							ranking.setValue(response.body());
						}
				));
	}
}