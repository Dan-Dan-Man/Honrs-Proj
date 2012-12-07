package com.lines.classes;

import android.app.Application;

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

	@Override
	public void onCreate() {
		nDb = new NoteDbAdapter(getApplicationContext());
		pDb = new PlayDbAdapter(getApplicationContext());
		nDb.open();
		pDb.open();
	}

	/**
	 * Return adapter to Note database.
	 * 
	 * @return
	 */
	public NoteDbAdapter getNoteAdapter() {
		return nDb;
	}

	/**
	 * Return adapter to Play database.
	 * 
	 * @return
	 */
	public PlayDbAdapter getPlayAdapter() {
		return pDb;
	}

}
