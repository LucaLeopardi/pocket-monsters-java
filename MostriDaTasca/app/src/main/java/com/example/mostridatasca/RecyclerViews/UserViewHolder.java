package com.example.mostridatasca.RecyclerViews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostridatasca.RetrofitData.ServerResponse;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.databinding.ListItemUserBinding;

public class UserViewHolder extends RecyclerView.ViewHolder {
	
	private final ListItemUserBinding binding;
	private User user;
	private ServerResponse.UserRanking userRankingData;
	
	public UserViewHolder(@NonNull View itemView, IUserRecyclerViewListener listener) {
		super(itemView);
		binding = ListItemUserBinding.bind(itemView);
		itemView.setOnClickListener( v -> listener.onClick(user.uid, user.profileversion, userRankingData.getLife(), userRankingData.getExperience(), userRankingData.getLat(), userRankingData.getLon()));
	}
	
	public void bind (User user, ServerResponse.UserRanking userRankingData) {
		this.user = user;
		this.userRankingData = userRankingData;
		binding.nameUserListItem.setText(user.name);
		binding.statsUserListItem.setText("HP: " + userRankingData.getLife() + "  |  XP: " + userRankingData.getExperience());
		Bitmap picture;
		if (user.picture != null) {
			byte[] pictureBytes = Base64.decode(user.picture, Base64.DEFAULT);
			picture = BitmapFactory.decodeByteArray((pictureBytes), 0, pictureBytes.length);
			binding.pictureUserListItem.setImageBitmap(picture);
		}
	}
}