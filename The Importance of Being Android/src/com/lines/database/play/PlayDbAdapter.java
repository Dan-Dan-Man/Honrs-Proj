/*
 * Copyright (C) 2013 The Android Open Source Project 
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

package com.lines.database.play;

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
	public static final String KEY_AUDIO = "audio";
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
	 * Delete all the entries in the database, and reset the auto-incrementer
	 * 
	 */
	public void deleteTable() {
		mDbHelper = new PlayDatabaseHelper(mContext);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete("Play", null, null);
		db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + "Play" + "'");
	}

	/**
	 * * Create a new line entry into the table. If the entry is successfully
	 * created return the new * rowId for that note, otherwise return a -1 to
	 * indicate failure. This will only be used to initialise table.
	 */

	public long createPlay(int number, String character, String line, int act,
			int page, String note, String audio, int views, int prompts,
			int completions) {
		ContentValues values = createContentValues(number, character, line,
				act, page, note, audio, views, prompts, completions);

		return mDb.insert(DB_TABLE, null, values);
	}

	/**
	 * Update the Notes column if their exists a performance note
	 * 
	 */
	public boolean updateNotes(long rowId, String note) {
		ContentValues args = new ContentValues();
		args.put(KEY_NOTE, note);
		return mDb.update(DB_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Update the Audio column if their exists an audio file
	 * 
	 */
	public boolean updateAudio(long rowId, String audio) {
		ContentValues args = new ContentValues();
		args.put(KEY_AUDIO, audio);
		return mDb.update(DB_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Update the Views column
	 * 
	 */
	public boolean updateViews(long rowId, int views) {
		ContentValues args = new ContentValues();
		args.put(KEY_VIEWS, views);
		return mDb.update(DB_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Update the Prompts column
	 * 
	 */
	public boolean updatePrompts(long rowId, int prompts) {
		ContentValues args = new ContentValues();
		args.put(KEY_PROMPTS, prompts);
		return mDb.update(DB_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Update the Completions column
	 * 
	 */
	public boolean updateCompletions(long rowId, int completions) {
		ContentValues args = new ContentValues();
		args.put(KEY_COMPLETIONS, completions);
		return mDb.update(DB_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return all lines in the database
	 * 
	 */
	public Cursor fetchAllLines() {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS }, null,
				null, null, null, null);
	}

	/**
	 * Return one line in the database
	 * 
	 */
	public Cursor fetchLine(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DB_TABLE, new String[] { KEY_ROWID,
				KEY_NUMBER, KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE,
				KEY_NOTE, KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return all the lines that appear on the given page
	 * 
	 */
	public Cursor fetchPage(String page) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS }, KEY_PAGE
				+ "= ?", new String[] { page }, null, null, null);
	}

	/**
	 * Return all the pages that appear in the given act
	 * 
	 */
	public Cursor fetchAllPages(String act) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS }, KEY_ACT
				+ "= ?", new String[] { act }, null, null, null);
	}

	/**
	 * Return all the pages that appear in the given act for the given character
	 * 
	 */
	public Cursor fetchFilteredPages(String act, String character) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS }, KEY_ACT
				+ "= ?" + " and " + KEY_CHARACTER + "=?", new String[] { act,
				character }, null, null, null);
	}

	/**
	 * Return all the lines that appear in the given page for the fiven
	 * character
	 * 
	 */
	public Cursor fetchCharacter(String character, String page) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS },
				KEY_CHARACTER + "= ?" + " and " + KEY_PAGE + "= ?",
				new String[] { character, page }, null, null, null);
	}

	/**
	 * Return all the acts that the given character appears in
	 * 
	 */
	public Cursor fetchActs(String character) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS },
				KEY_CHARACTER + "= ?", new String[] { character }, null, null,
				null);
	}

	/**
	 * Return all the pages that the given character appears in
	 * 
	 */
	public Cursor fetchAllFilteredPages(String character) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_CHARACTER, KEY_LINE, KEY_ACT, KEY_PAGE, KEY_NOTE,
				KEY_AUDIO, KEY_VIEWS, KEY_PROMPTS, KEY_COMPLETIONS },
				KEY_CHARACTER + "= ?", new String[] { character }, null, null,
				null);
	}

	private ContentValues createContentValues(int number, String character,
			String line, int act, int page, String note, String audio,
			int views, int prompts, int completions) {
		ContentValues values = new ContentValues();
		values.put(KEY_NUMBER, number);
		values.put(KEY_CHARACTER, character);
		values.put(KEY_LINE, line);
		values.put(KEY_ACT, act);
		values.put(KEY_PAGE, page);
		values.put(KEY_NOTE, note);
		values.put(KEY_AUDIO, audio);
		values.put(KEY_VIEWS, views);
		values.put(KEY_PROMPTS, prompts);
		values.put(KEY_COMPLETIONS, completions);
		return values;
	}
}
