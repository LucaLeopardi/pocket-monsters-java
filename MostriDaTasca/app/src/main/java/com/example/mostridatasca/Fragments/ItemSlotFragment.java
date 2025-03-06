package com.example.mostridatasca.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.R;
import com.example.mostridatasca.RoomData.Object;
import com.example.mostridatasca.ViewModels.ObjectDetailsViewModel;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.FragmentItemSlotBinding;

public class ItemSlotFragment extends Fragment {
	private FragmentItemSlotBinding binding;
	private ObjectDetailsViewModel objectDetailsViewModel;
	private Bitmap icon;
	
	public ItemSlotFragment() {}		// Required empty public constructor

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		objectDetailsViewModel = new ViewModelProvider(this).get(ObjectDetailsViewModel.class);
	}
	
	public void loadObject(Integer id, String type, String sid) {
		int resource;
		switch (type) {
			case "weapon": resource = R.drawable.weapon_icon; break;
			case "armor": resource = R.drawable.armor_icon; break;
			case "amulet": default: resource = R.drawable.amulet_icon; break;
		}
		icon = BitmapFactory.decodeResource(getResources(), resource);
		
		if (id == null || id == 0) loadDefaultUI(type);
		else new Thread(() -> objectDetailsViewModel.loadObject(id, sid)).start();
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentItemSlotBinding.inflate(getLayoutInflater());
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		objectDetailsViewModel.getObject().observe(getViewLifecycleOwner(), this::updateUI);
	}
	
	private void updateUI(Object object) {
		if (object == null) return;
		binding.titleEquipSlot.setText(object.type);
		binding.nameEquipSlot.setText(object.name);
		binding.levelEquipSlot.setText("LVL " + object.level);
		// Description
		String effect = null;
		switch (object.type) {
			case "weapon": effect = "+" + object.level + "% DMG RES"; break;
			case "armor": effect = "+" + object.level + " Max HP"; break;
			case "amulet": effect = "+" + object.level + "% Map range"; break;
		}
		binding.effectEquipSlot.setText(effect);
		// Image
		Bitmap picture;
		if (object.image != null) {
			byte[] pictureBytes = Base64.decode(object.image, Base64.DEFAULT);
			picture = BitmapFactory.decodeByteArray((pictureBytes), 0, pictureBytes.length);
		} else {
			picture = icon;
		}
		binding.imageEquipSlot.setImageBitmap(picture);
	}
	
	private void loadDefaultUI(String type) {
		binding.titleEquipSlot.setText(type);
		binding.nameEquipSlot.setText("Empty");
		binding.levelEquipSlot.setVisibility(View.GONE);
		binding.effectEquipSlot.setVisibility(View.GONE);
		binding.imageEquipSlot.setImageBitmap(icon);
		binding.imageEquipSlot.setAlpha(0.5f);
	}
}