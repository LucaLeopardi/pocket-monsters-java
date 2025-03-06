package com.example.mostridatasca.RecyclerViews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostridatasca.R;
import com.example.mostridatasca.RoomData.Object;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.ListItemObjectBinding;

public class ObjectViewHolder extends RecyclerView.ViewHolder {
	
	private final ListItemObjectBinding binding;
	private PlayerViewModel playerViewModel;
	private Object object;
	private double lat;
	private double lon;
	
	public ObjectViewHolder(@NonNull View itemView, IObjectsRecyclerViewListener listener) {
		super(itemView);
		binding = ListItemObjectBinding.bind(itemView);
		itemView.setOnClickListener( v -> listener.onClick(object.id, lat, lon));
	}
	
	public void bind (Object object, int distance, boolean inRange, double lat, double lon) {
		this.object = object;
		this.lat = lat;
		this.lon = lon;
		// Distance check
		if (!inRange) binding.getRoot().setAlpha(0.5f);
		else binding.getRoot().setAlpha(1f);
		
		binding.nameObjectListItem.setText(object.name);
		binding.statsObjectListItem.setText(object.getStats());
		binding.distanceObjectListItem.setText(distance + "m");
		Bitmap image;
		if (object.image != null) {
			byte[] imageBytes = Base64.decode(object.image, Base64.DEFAULT);
			image = BitmapFactory.decodeByteArray((imageBytes), 0, imageBytes.length);
		} else {
			int resource;
			switch (object.type) {
				case "weapon": resource = R.drawable.weapon_icon; break;
				case "armor": resource = R.drawable.armor_icon; break;
				case "amulet": resource = R.drawable.amulet_icon; break;
				case "monster": resource = R.drawable.monster_icon; break;
				case "candy": default: resource = R.drawable.candy_icon; break;
			}
			image = BitmapFactory.decodeResource(itemView.getResources(), resource);
		}
		binding.imageObjectListItem.setImageBitmap(image);
	}
}