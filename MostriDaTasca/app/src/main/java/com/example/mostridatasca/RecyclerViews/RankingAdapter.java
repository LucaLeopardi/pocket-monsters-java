package com.example.mostridatasca.RecyclerViews;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostridatasca.CommunicationController;
import com.example.mostridatasca.R;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.RoomData.IUserDAO;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.StorageManager;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<UserViewHolder> {
	
	private final Application application;
	private final LayoutInflater inflater;
	private final List<ServerResponse.UserRanking> users;
	private final IUserRecyclerViewListener listener;
	private final String sid;
	
	public RankingAdapter(Application application, LayoutInflater inflater, List<ServerResponse.UserRanking> users, IUserRecyclerViewListener listener, String sid) {
		this.application = application;
		this.inflater = inflater;
		this.users = users;
		this.listener = listener;
		this.sid = sid;
	}
	
	@NonNull
	@Override
	public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = inflater.inflate(R.layout.list_item_user, parent, false);
		return new UserViewHolder(itemView, listener);
	}
	
	@Override
	public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
		ServerResponse.UserRanking userRankingData = users.get(position);
		MutableLiveData<User> data = new MutableLiveData<>();
		data.observeForever(new Observer<User>() {
			@Override
			public void onChanged(User user) {
				holder.bind(user, userRankingData);
				data.removeObserver(this);
			}
		});
		
		// DB and Retrofit operations: it's aaaaall asynchronous
		new Thread(() -> {
			IUserDAO database = StorageManager.getDatabase(application).getUserDAO();
			User dbUser = database.getUser(users.get(position).getUid());
			
			if (dbUser != null && dbUser.profileversion >= users.get(position).getProfileVersion()) {
				data.postValue(dbUser);
			} else {
				CommunicationController.getRetrofitClient().getUserDetails(users.get(position).getUid(), sid)
						.enqueue(CommunicationController.onSuccess(
								(response) -> {
									if (response.body() == null)
										throw new RuntimeException("ERROR::RankingAdapter::onBindViewHolder::onResponse::response.body() is null");
									User user = new User(response.body());
									new Thread(() -> database.addUser(user)).start();
									data.postValue(user);
								}
						));
			}
		}).start();
	}
	
	@Override
	public int getItemCount() { return users.size(); }
}