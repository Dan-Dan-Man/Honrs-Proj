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

package com.lines.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This class initialises the Database table used for storing the play.
 * 
 * @author Dan
 * 
 */

public class PlayTable {
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table Play "
			+ "(_id integer primary key autoincrement, "
			+ "number integer not null, " + "character text not null, "
			+ "line text not null, " + "act integer not null, "
			+ "page integer not null, " + "note text not null, "
			+ "striked text not null, " + "highlight text not null, "
			+ "views integer not null, " + "prompts integer not null, "
			+ "completions integer not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(PlayTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS Play");
		onCreate(database);
	}
}
