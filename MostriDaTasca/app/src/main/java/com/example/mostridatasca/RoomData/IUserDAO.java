package com.example.mostridatasca.RoomData;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface IUserDAO {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void addUser(User user);
	
	@Query("SELECT * FROM User WHERE uid = :uid")
	User getUser(int uid);
}