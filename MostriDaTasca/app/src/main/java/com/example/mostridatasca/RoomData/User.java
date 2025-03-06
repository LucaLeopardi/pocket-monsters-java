package com.example.mostridatasca.RoomData;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.example.mostridatasca.RetrofitData.ServerResponse;
import retrofit2.Response;

@Entity
public class User {
	@PrimaryKey
	public int uid;
	public  String name;
	public double lat;
	public double lon;
	public int life;
	public int experience;
	public Integer weapon;	// Integer to allow null values
	public Integer armor;	// Integer to allow null values
	public Integer amulet;	// Integer to allow null values
	public String picture;
	public int profileversion;
	public boolean positionshare;
	
	// Empty constructor for Room
	public User() {}
	
	// Constructor from server UserDetails Response to make a User entity directly
	public User(ServerResponse.UserDetails response) {
		this.uid = response.getUid();
		this.name = response.getName();
		this.lat = response.getLat();
		this.lon = response.getLon();
		this.life = response.getLife();
		this.experience = response.getExperience();
		this.weapon = response.getWeapon();
		this.armor = response.getArmor();
		this.amulet = response.getAmulet();
		this.picture = response.getPicture();
		this.profileversion = response.getProfileVersion();
		this.positionshare = response.getPositionShare();
	}
	
	// Copy constructor
	public User(User user) {
		this.uid = user.uid;
		this.name = user.name;
		this.lat = user.lat;
		this.lon = user.lon;
		this.life = user.life;
		this.experience = user.experience;
		this.weapon = user.weapon;
		this.armor = user.armor;
		this.amulet = user.amulet;
		this.picture = user.picture;
		this.profileversion = user.profileversion;
		this.positionshare = user.positionshare;
	}
}