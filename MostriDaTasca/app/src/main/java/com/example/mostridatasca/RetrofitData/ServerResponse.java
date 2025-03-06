package com.example.mostridatasca.RetrofitData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerResponse {
	// Inner classes are static because non-static inner classes apparently need a reference to the outer class
	// Commented-out fields are Response fields that are not used in the app
	
	public static class SignUp {
		private String sid;
		private int uid;
		
		public String getSid() {return sid;}
		public int getUid() {return uid;}
	}
	
	public static class UserRanking {
		private int uid;
		private int life;
		private int experience;
		private int profileversion;
		// private boolean positionshare;
		private double lat;
		private double lon;
		
		public int getUid() {return uid;}
		public int getLife() {return life;}
		public int getExperience() {return experience;}
		public int getProfileVersion() {return profileversion;}
		// public boolean getPositionShare() {return positionshare;}
		public double getLat() {return lat;}
		public double getLon() {return lon;}
	}
	
	public static class UserNearby {
		private int uid;
		private double lat;
		private double lon;
		private int profileversion;
		private int life;
		private int experience;
		// private String time;
		
		public int getUid() {return uid;}
		public double getLat() {return lat;}
		public double getLon() {return lon;}
		public int getProfileVersion() {return profileversion;}
		public int getLife() {return life;}
		public int getExperience() {return experience;}
		// public String getTime() {return time;}
		
		// For use in MapFragment
		public static Map<Integer, UserNearby> toMap(List<UserNearby> users) {
			Map<Integer, UserNearby> map = new HashMap<>();
			for (UserNearby user : users) {	map.put(user.getUid(), user); }
			return map;
		}
	}
	
	public static class UserDetails {
		private int uid;
		private  String name;
		private double lat;
		private double lon;
		// private String time;
		private int life;
		private int experience;
		private int weapon;
		private int armor;
		private int amulet;
		private String picture;
		private int profileversion;
		private boolean positionshare;

		public int getUid() {return uid;}
		public String getName() {return name;}
		public double getLat() {return lat;}
		public double getLon() {return lon;}
		// public String getTime() {return time;}
		public int getLife() {return life;}
		public int getExperience() {return experience;}
		public int getWeapon() {return weapon;}
		public int getArmor() {return armor;}
		public int getAmulet() {return amulet;}
		public String getPicture() {return picture;}
		public int getProfileVersion() {return profileversion;}
		public boolean getPositionShare() {return positionshare;}
	}
	
	public static class ObjectNearby {
		private int id;
		private double lat;
		private double lon;
		private String type;
		
		// For use in MapFragment, to associate a marker with an object
		public static Map<Integer, ObjectNearby> toMap(List<ObjectNearby> objectsNearby) {
			Map<Integer, ObjectNearby> map = new HashMap<>();
			for (ObjectNearby object : objectsNearby) {	map.put(object.getId(), object); }
			return map;
		}
		
		public int getId() {return id;}
		public double getLat() {return lat;}
		public double getLon() {return lon;}
		public String getType() {return type;}
	}
	
	public static class ObjectDetails {
		private int id;
		private String type;
		private int level;
		// private double lat;
		// private double lon;
		private String image;
		private String name;
		
		public int getId() {return id;}
		public String getType() {return type;}
		public int getLevel() {return level;}
		// public double getLat() {return lat;}
		// public double getLon() {return lon;}
		public String getImage() {return image;}
		public String getName() {return name;}
	}
	
	public static class ObjectActivation {
		private boolean died;
		private int life;
		private int experience;
		private int weapon;
		private int armor;
		private int amulet;
		
		public boolean getDied() {return died;}
		public int getLife() {return life;}
		public int getExperience() {return experience;}
		public int getWeapon() {return weapon;}
		public int getArmor() {return armor;}
		public int getAmulet() {return amulet;}
	}
}