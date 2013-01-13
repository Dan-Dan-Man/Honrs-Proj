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

package com.lines.database.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NoteDbAdapter {

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_TITLE = "title";
	public static final String KEY_NOTE = "note";
	private static final String DB_TABLE = "Note";
	private Context mContext;
	private SQLiteDatabase mDb;
	private NoteDatabaseHelper mDbHelper;

	public NoteDbAdapter(Context context) {
		this.mContext = context;
	}

	public NoteDbAdapter open() throws SQLException {
		mDbHelper = new NoteDatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public void deleteTable() {
		mDbHelper = new NoteDatabaseHelper(mContext);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete("Note", null, null);
		db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + "Note" + "'");
	}

	/**
	 * * Create a new line entry into the table. If the entry is successfully
	 * created return the new * rowId for that note, otherwise return a -1 to
	 * indicate failure. This will only be used to initialise table.
	 */

	public long createNote(int number, String title, String note) {
		ContentValues values = createContentValues(number, title, note);

		return mDb.insert(DB_TABLE, null, values);
	}

	/** * Update the Food */

	public boolean updateNote(long rowId, int number, String title, String note) {
		ContentValues values = createContentValues(number, title, note);

		return mDb.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Deletes Note
	 * 
	 */
	public boolean deleteNote(long rowId) {
		return mDb.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllNotes() {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_TITLE, KEY_NOTE }, null, null, null, null, null);
	}

	public Cursor fetchNotes(String number) {
		return mDb.query(DB_TABLE, new String[] { KEY_ROWID, KEY_NUMBER,
				KEY_TITLE, KEY_NOTE }, KEY_NUMBER + "= ?",
				new String[] { number }, null, null, null);
	}

	public Cursor fetchNote(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DB_TABLE, new String[] { KEY_ROWID,
				KEY_NUMBER, KEY_TITLE, KEY_NOTE }, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(int number, String title,
			String note) {
		ContentValues values = new ContentValues();
		values.put(KEY_NUMBER, number);
		values.put(KEY_TITLE, title);
		values.put(KEY_NOTE, note);
		return values;
	}
}
