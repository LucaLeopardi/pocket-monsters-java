package com.example.mostridatasca;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mostridatasca.RoomData.IObjectDAO;
import com.example.mostridatasca.RoomData.IUserDAO;
import com.example.mostridatasca.RoomData.User;
import com.example.mostridatasca.RoomData.Object;

@Database(entities = {User.class, Object.class}, version = 1)
public abstract class StorageManager extends RoomDatabase {
	private static volatile StorageManager instance;
	
	public static StorageManager getDatabase(final Context context) {
		if (instance == null) {
			synchronized (StorageManager.class) {
				if (instance == null) {
					instance = Room.databaseBuilder(context.getApplicationContext(),
													StorageManager.class,
												"MostriDaTasca.db")
										.build();
				}
			}
		}
		return instance;
	}

	public abstract IUserDAO getUserDAO();
	public abstract IObjectDAO getObjectDAO();
}