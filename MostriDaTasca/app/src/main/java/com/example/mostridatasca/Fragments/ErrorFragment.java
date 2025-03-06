package com.example.mostridatasca.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostridatasca.R;
import com.example.mostridatasca.databinding.FragmentErrorBinding;

public class ErrorFragment extends Fragment {
	private FragmentErrorBinding binding;
	private String message;

	public ErrorFragment() {}		// Required empty public constructor
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			message = getArguments().getString("message");
		}
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentErrorBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.errorMessage.setText(message);
	}
}