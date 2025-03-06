package com.example.mostridatasca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.example.mostridatasca.Fragments.ErrorFragment;
import com.example.mostridatasca.Fragments.MapFragment;
import com.example.mostridatasca.Fragments.ObjectDetailsFragment;
import com.example.mostridatasca.Fragments.ObjectsNearbyFragment;
import com.example.mostridatasca.Fragments.RankingFragment;
import com.example.mostridatasca.Fragments.RegistrationFragment;
import com.example.mostridatasca.Fragments.PlayerProfileFragment;
import com.example.mostridatasca.Fragments.UserDetailsFragment;
import com.example.mostridatasca.ViewModels.PlayerViewModel;
import com.example.mostridatasca.databinding.ActivityMainBinding;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class MainActivity extends AppCompatActivity {
	private PlayerViewModel playerViewModel;
	private FragmentManager fragmentManager;
	private ActivityMainBinding binding;
	private View rootView;
	private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Tried everything to force Light theme, but it doesn't work on my phone. It works on the emulator, though.
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		getApplication().setTheme(R.style.Theme_MostriDaTasca);
		
		super.onCreate(savedInstanceState);
		
		fragmentManager = getSupportFragmentManager();
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		rootView = binding.getRoot();
		binding.locationDisabledContent.setVisibility(View.GONE);
		setContentView(rootView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		checkInternetConnection();
		if (currentFragment == -1) checkLocationPermissions();	// This will call checkLocationServices() and then resumeAppLaunch()
	}
	
	@Override
	public void onBackPressed() {
		// Ignore warning: the whole point is to prevent default Back button behavior in MainActivity (closing the app)
		if (fragmentManager.getBackStackEntryCount() > 0) fragmentManager.popBackStack();
	}
	
	private void checkInternetConnection() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkCapabilities capabilities = (connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()));
		boolean connected = capabilities != null && (
							capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
							capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
							capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
		if (!connected) {
			Bundle args = new Bundle();
			args.putString("message", "It seems you don't have an internet connection. Please check your connection and try launching the app again.");
			binding.mainActivityContent.setVisibility(View.GONE);
			navigateToFragment(FRAGMENT.ERROR, args);
		}
	}
	
	private void checkLocationPermissions() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.d("MainActivity", "Requesting location permissions...");
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS_REQUEST_CODE);
		} else {
			Log.d("MainActivity", "Location permissions already granted");
			checkLocationServices();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d("MainActivity", "Location permissions granted");
				checkLocationServices();
			} else {
				Log.d("MainActivity", "Location permissions denied");
				Bundle args = new Bundle();
				args.putString("message", "This app needs location permissions to work. Please grant the permissions and launch the app again.");
				binding.mainActivityContent.setVisibility(View.GONE);
				navigateToFragment(FRAGMENT.ERROR, args);
			}
		}
	}
	
	private void checkLocationServices() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )|| !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.d("MainActivity", "Location services disabled");
			binding.locationDisabledContent.setVisibility(View.VISIBLE);
			binding.openDeviceSettingsButton.setOnClickListener((v) -> {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			});
		} else {
			Log.d("MainActivity", "Location services enabled. Proceeding...");
			continueAppLaunch();
		}
	}
	
	private void continueAppLaunch() {
		playerViewModel = new ViewModelProvider(
				this,
				// All this to pass the application Context to the ViewModel's constructor
				new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(PlayerViewModel.class);

		startLocationUpdates();
		
		// Navigate to app or registration
		Log.d("MainActivity", "Stored SID: " + playerViewModel.getSid().getValue());
		if (playerViewModel.getSid().getValue() == null) {
			navigateToFragment(FRAGMENT.REGISTRATION);
		} else {
			navigateToFragment(FRAGMENT.MAP);
		}
	}
	
	private void startLocationUpdates() {
		// Redundant permission check to make Android Studio happy
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
		
		// Location updated are slow for some reason, if hopefully the device has a last location to initialize the map with
		LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener((firstLocation) -> {
			if (firstLocation == null) return;
			Log.d("MainActivity/PlayerViewModel", "First, hopefully faster, location update: " + firstLocation);
			playerViewModel.setLocation(firstLocation);
		});
		// Actual location updates subscription
		LocationRequest locationRequest = new LocationRequest.Builder(3000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
		LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
				locationRequest,
				playerViewModel.getLocationCallback(),
				Looper.getMainLooper());
	}
	
	
	// NAVIGATION //
	// Contrived way to save current fragment to restore on configuration change
	private int currentFragment = -1;
	private Bundle currentFragmentArgs;
	
	public enum FRAGMENT {
		REGISTRATION,
		MAP,
		OBJECTS_NEARBY,
		OBJECT_DETAILS,
		RANKING,
		USER_DETAILS,
		PLAYER_PROFILE,
		ERROR
	}
	
	public void setCurrentFragment(FRAGMENT fragment) { setCurrentFragment(fragment, null); }
	
	public void setCurrentFragment(FRAGMENT fragment, Bundle args) {
		Log.d("MainActivity", "Current fragment: " + fragment.toString());
		currentFragment = fragment.ordinal();
		currentFragmentArgs = args;
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentFragment", currentFragment);
		outState.putBundle("args", currentFragmentArgs);
	}
	
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("MainActivity", "Restoring fragment: " + savedInstanceState.getInt("currentFragment"));
		currentFragment = savedInstanceState.getInt("currentFragment");
		currentFragmentArgs = savedInstanceState.getBundle("args");
		navigateToFragment(FRAGMENT.values()[currentFragment], currentFragmentArgs);
	}
	
	public void navigateToFragment(FRAGMENT fragment) { navigateToFragment(fragment, null); }
	
	public void navigateToFragment(FRAGMENT fragment, @Nullable Bundle args) {
		Log.d("MainActivity", "Navigating to " + fragment.toString());
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		switch (fragment) {
			case REGISTRATION:
				transaction
					.replace(rootView.getId(), new RegistrationFragment());
				break;
			case MAP:
				transaction
					.replace(rootView.getId(), new MapFragment());
				break;
			case OBJECTS_NEARBY:
				transaction
					.addToBackStack(null)
					.replace(rootView.getId(), new ObjectsNearbyFragment());
				break;
			case OBJECT_DETAILS:
				if (args == null) {
					Log.w("MainActivity", "Restored Fragment args is null. Navigation to Map...");
					navigateToFragment(FRAGMENT.MAP);
				}
				transaction
					.addToBackStack(null)
					.replace(rootView.getId(), ObjectDetailsFragment.class, args);
				break;
			case RANKING:
				transaction
					.addToBackStack(null)
					.replace(rootView.getId(), new RankingFragment());
				break;
			case USER_DETAILS:
				if (args == null) {
					Log.w("MainActivity", "Restored Fragment args is null. Navigation to Map...");
					navigateToFragment(FRAGMENT.MAP);
				}
				transaction
					.addToBackStack(null)
					.replace(rootView.getId(), UserDetailsFragment.class, args);
				break;
			case PLAYER_PROFILE:
				transaction
					.addToBackStack(null)
					.replace(rootView.getId(), new PlayerProfileFragment());
				break;
			case ERROR:
				transaction.replace(rootView.getId(), ErrorFragment.class, args);
		}
		transaction.commit();
	}
}