/*
 * Copyright (C) 2012 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 * 			The Importance of Being Android					  
 * 			    4th year Honours Project			  
 * 				  	 Created By					  				  
 * 			         Daniel Muir					  
 */

package com.lines.classes;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * This is the class for the whole application itself. Primarily used for our
 * database adapters. Now we only work with one adapter per database.
 * 
 * @author Dan
 * 
 */
public class LinesApp extends Application {

	private static NoteDbAdapter nDb;
	private static PlayDbAdapter pDb;
	private static final String TAG = "LinesApp";
	private boolean dbExists;

	@Override
	public void onCreate() {

		// Check if database exists
		if (!checkDataBase()) {
			dbExists = false;
		} else {
			dbExists = true;
		}

		// Create and open adapters to databases
		nDb = new NoteDbAdapter(getApplicationContext());
		pDb = new PlayDbAdapter(getApplicationContext());
		Log.d(TAG, "Opening Databases");
		nDb.open();
		pDb.open();
	}

	/**
	 * Return adapter to Note database.
	 * 
	 * @return nDb
	 */
	public NoteDbAdapter getNoteAdapter() {
		return nDb;
	}

	/**
	 * Return adapter to Play database.
	 * 
	 * @return pDb
	 */
	public PlayDbAdapter getPlayAdapter() {
		return pDb;
	}

	/**
	 * Return whether the Play database exists or not.
	 * 
	 * @return
	 */
	public boolean dbExists() {
		return dbExists;
	}

	/**
	 * This method checks if a Database exists.
	 * 
	 * @return
	 */
	private boolean checkDataBase() {
		String DB_PATH = "/data/data/com.lines/databases/Play Data";
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(DB_PATH, null,
					SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
		} catch (SQLiteException e) {
			// TODO: Print proper message
			// database doesn't exist yet.
		}

		return checkDB != null ? true : false;

	}

}