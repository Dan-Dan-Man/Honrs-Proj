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

package com.lines.classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.Environment;

import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * This is the class for the whole application itself. Primarily used for our
 * database adapters. Now we only work with one adapter per database.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class LinesApp extends Application {

	private static NoteDbAdapter nDb;
	private static PlayDbAdapter pDb;
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
		nDb.open();
		pDb.open();

		// Create folder for scripts
		File scripts = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/scripts");

		if (!scripts.exists()) {
			scripts.mkdirs();
		}

		File defaultScript = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/scripts/earnest.txt");

		if (!defaultScript.exists()) {
			transferScriptToSD();
		}

		// Create folder to save audio files
		File audio = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/audio");

		if (!audio.exists()) {
			audio.mkdirs();
		}
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
	 * Retrieve the sample script "earnest" from the assets folder, and copy to
	 * SD card in "scripts" folder
	 * 
	 */
	private void transferScriptToSD() {
		AssetManager am = getAssets();
		InputStream in;
		OutputStream out;

		try {
			in = am.open("scripts/earnest.txt");
			out = new FileOutputStream(
					Environment.getExternalStorageDirectory()
							+ "/learnyourlines/scripts/earnest.txt");
			copyFile(in, out);
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the file in the assets folder, and output to the script folder on SD
	 * card.
	 * 
	 * @param in
	 *            - the script in the assest folder
	 * @param out
	 *            - the new file we are writing to on the SD card
	 * @throws IOException
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
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
		File database = getApplicationContext().getDatabasePath("Play Data");

		if (!database.exists()) {
		    return false;
		} else {
			return true;
		}

	}

}
