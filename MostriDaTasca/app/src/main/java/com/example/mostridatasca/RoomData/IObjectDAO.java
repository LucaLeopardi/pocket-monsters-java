package com.example.mostridatasca.RoomData;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface IObjectDAO {
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void addObject(Object object);
	
	@Query("SELECT * FROM Object WHERE id = :id")
	Object getObject(int id);
}