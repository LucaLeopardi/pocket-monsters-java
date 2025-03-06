package com.example.mostridatasca.RoomData;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.mostridatasca.RetrofitData.ServerResponse;

import retrofit2.Response;

@Entity
public class Object {
	@PrimaryKey
	public int id;
	public String type;
	public int level;
	public String image;
	public String name;
	
	// Empty constructor for Room
	public Object () {}

	// Constructor from server ObjectDetails Response to make a Object entity directly
	public Object (ServerResponse.ObjectDetails response) {
		this.id = response.getId();
		this.type = response.getType();
		this.level = response.getLevel();
		this.image = response.getImage();
		this.name = response.getName();
	}
	
	public String getStats() {
		return "Level " + level + " " + type;
	}
	
	public String getInteractionType() {
		switch (type) {
			case "weapon":
			case "armor":
			case "amulet": return "Equip";
			case "candy": return "Eat";
			case "monster": return "Fight";
		}
		return null;
	}
}