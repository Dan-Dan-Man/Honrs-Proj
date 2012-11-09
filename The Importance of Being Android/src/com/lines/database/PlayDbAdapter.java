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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PlayDbAdapter {

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_CHARACTER = "character";
	public static final String KEY_LINE = "line";
	public static final String KEY_ACT = "act";
	public static final String KEY_PAGE = "page";
	public static final String KEY_NOTE = "note";
	public static final String KEY_STRIKED = "striked";
	public static final String KEY_HIGHLIGHT = "highlight";
	public static final String KEY_VIEWS = "views";
	public static final String KEY_PROMPTS = "prompts";
	public static final String KEY_COMPLETIONS = "completions";
	private static final String DB_TABLE = "Play";
	private Context mContext;
	private SQLiteDatabase mDb;
	private PlayDatabaseHelper mDbHelper;

	public PlayDbAdapter(Context context) {
		this.mContext = context;
	}

	public PlayDbAdapter open() throws SQLException {
		mDbHelper = new PlayDatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * * Create a new line entry into the table. If the entry is successfully
	 * created return the new * rowId for that note, otherwise return a -1 to
	 * indicate failure. This will only be used to initialise table.
	 */

	public long createPlay(int number, String character, String line, int act,
			int page, String note, String striked, String highlight, int views,
			int prompts, int completions) {
		ContentValues values = createContentValues(number, character, line,
				act, page, note, striked, highlight, views, prompts,
				completions);

		return mDb.insert(DB_TABLE, null, values);
	}

	/** * Update the Food */

	public boolean updatePlay(long rowId, int number, String character,
			String line, int act, int page, String note, String striked,
			String highlight, int views, int prompts, int completions) {
		ContentValues values = createContentValues(number, character, line,
				act, page, note, striked, highlight, views, prompts,
				completions);

		return mDb.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// Probably don't need this, but keep it incase.

	// /** * Deletes Food */
	//
	// public boolean deleteFood(long rowId) {
	// return mDb.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	// }

	// TODO: ALL OF THESE WILL BE USED TO FILTER LINES FOR USER. COME BACK TO
	// THIS LATER

	/**
	 * * Return a Cursor over the list of all Food in the database * * @return
	 * Cursor over all Food
	 */

	public Cursor fetchAllLines() {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_STRIKED, KEY_HIGHLIGHT, KEY_VIEWS, KEY_PROMPTS,
				KEY_COMPLETIONS }, null, null, null, null, null);
	}

	public Cursor fetchPage(String page) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_STRIKED, KEY_HIGHLIGHT, KEY_VIEWS, KEY_PROMPTS,
				KEY_COMPLETIONS }, KEY_PAGE + "= ?", new String[] { page },
				null, null, null);
	}
	
	public Cursor fetchAllPages(String act) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_STRIKED, KEY_HIGHLIGHT, KEY_VIEWS, KEY_PROMPTS,
				KEY_COMPLETIONS }, KEY_ACT + "= ?", new String[] { act },
				null, null, null);
	}

	public Cursor fetchCharacter(String character, String page) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_STRIKED, KEY_HIGHLIGHT, KEY_VIEWS, KEY_PROMPTS,
				KEY_COMPLETIONS }, KEY_CHARACTER + "= ?" + " and " + KEY_PAGE
				+ "= ?", new String[] { character, page }, null, null, null);
	}

	//
	// public Cursor fetchAllSandwich() {
	// return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_TITLE,
	// KEY_ADDRESS, KEY_POSTCODE, KEY_PHONE, KEY_WEB, KEY_BOOK,
	// KEY_CUISINE, KEY_DISTANCE, KEY_RATING }, KEY_CUISINE + "= ?",
	// new String[] { "Sandwich" }, null, null, null);
	// }
	//
	// public Cursor fetchAllFive() {
	// return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_TITLE,
	// KEY_ADDRESS, KEY_POSTCODE, KEY_PHONE, KEY_WEB, KEY_BOOK,
	// KEY_CUISINE, KEY_DISTANCE, KEY_RATING }, KEY_DISTANCE + "= ?",
	// new String[] { "5 minutes" }, null, null, null);
	// }
	//
	// public Cursor fetchAllTen() {
	// return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_TITLE,
	// KEY_ADDRESS, KEY_POSTCODE, KEY_PHONE, KEY_WEB, KEY_BOOK,
	// KEY_CUISINE, KEY_DISTANCE, KEY_RATING }, KEY_DISTANCE + "= ?",
	// new String[] { "10 minutes" }, null, null, null);
	// }
	//
	// public Cursor fetchAllFifteen() {
	// return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_TITLE,
	// KEY_ADDRESS, KEY_POSTCODE, KEY_PHONE, KEY_WEB, KEY_BOOK,
	// KEY_CUISINE, KEY_DISTANCE, KEY_RATING }, KEY_DISTANCE + "= ?",
	// new String[] { "15 minutes" }, null, null, null);
	// }
	//
	// /** * Return a Cursor positioned at the defined Food */
	//
	// public Cursor fetchFood(long rowId) throws SQLException {
	// Cursor mCursor = mDb.query(true, DB_TABLE, new String[] { KEY_ROWID,
	// KEY_TITLE, KEY_ADDRESS, KEY_POSTCODE, KEY_PHONE, KEY_WEB,
	// KEY_BOOK, KEY_CUISINE, KEY_DISTANCE, KEY_RATING }, KEY_ROWID
	// + "=" + rowId, null, null, null, null, null);
	// if (mCursor != null) {
	// mCursor.moveToFirst();
	// }
	// return mCursor;
	// }

	private ContentValues createContentValues(int number, String character,
			String line, int act, int page, String note, String striked,
			String highlight, int views, int prompts, int completions) {
		ContentValues values = new ContentValues();
		values.put(KEY_NUMBER, number);
		values.put(KEY_CHARACTER, character);
		values.put(KEY_LINE, line);
		values.put(KEY_ACT, act);
		values.put(KEY_PAGE, page);
		values.put(KEY_NOTE, note);
		values.put(KEY_STRIKED, striked);
		values.put(KEY_HIGHLIGHT, highlight);
		values.put(KEY_VIEWS, views);
		values.put(KEY_PROMPTS, prompts);
		values.put(KEY_COMPLETIONS, completions);
		return values;
	}
}
