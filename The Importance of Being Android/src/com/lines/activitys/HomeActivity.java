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

package com.lines.activitys;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.R;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Home Screen where the user will start off.
 * 
 * @author Dan
 * 
 */

// TODO: Some minor formating bugs in database. Need to examine these and edit
// textfile.

public class HomeActivity extends Activity {

	private PlayDbAdapter mDbAdapter;
	private ImageButton mUpArrow;
	private ImageButton mDownArrow;
	private TextView mTop;
	private TextView mMiddle;
	private TextView mBottom;
	private static final String TAG = "HomeActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);

		// TODO: This check does not work right and database is populated
		// everytime. Check ForumApp example and see if same thing happens
		// there.
		// Create and populate Database if one doesn't exist
//		if (!checkDataBase()) {
//			try {
//				Log.d(TAG, "Creating new Database");
//				readFile();
//			} catch (IOException e) {
//				// TODO: Add proper error handling
//				Log.e(TAG, "Error");
//			}
//		}

		Log.d(TAG, "Opening Database");

		mTop = (TextView) findViewById(R.id.textTop);
		mMiddle = (TextView) findViewById(R.id.textMiddle);
		mBottom = (TextView) findViewById(R.id.textBottom);

		mUpArrow = (ImageButton) findViewById(R.id.imageButtonArrowUp);
		mDownArrow = (ImageButton) findViewById(R.id.imageButtonArrowDown);

		/**
		 * Method for scrolling down text
		 */
		mUpArrow.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchTextDown();
			}
		});

		/**
		 * Method for scrolling up text
		 */
		mDownArrow.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchTextUp();
			}
		});
	}

	/**
	 * Switch screen based on user selection.
	 * 
	 * @param v
	 */
	public void menuClick(View v) {
		if (mMiddle.getText().toString().equals("Start")) {
			Intent i = new Intent(this, OptionsActivity.class);
			startActivityForResult(i, 0);
		} else if (mMiddle.getText().toString().equals("Statistics")) {
			Intent i = new Intent(this, StatsActivity.class);
			startActivityForResult(i, 0);
		} else if (mMiddle.getText().toString().equals("Performance Notes")) {
			Intent i = new Intent(this, NotesActivity.class);
			startActivityForResult(i, 0);
		} else if (mMiddle.getText().toString().equals("Recordings")) {
			// TODO: Create recordings screen
		} else if (mMiddle.getText().toString().equals("Help")) {
			Intent i = new Intent(this, HelpActivity.class);
			startActivityForResult(i, 0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * Method for moving text up one
	 */
	private void switchTextUp() {
		if (mMiddle.getText().toString().equals("Start")) {
			mTop.setText("Start");
			mMiddle.setText("Statistics");
			mBottom.setText("Performance Notes");
		} else if (mMiddle.getText().toString().equals("Statistics")) {
			mTop.setText("Statistics");
			mMiddle.setText("Performance Notes");
			mBottom.setText("Recordings");
		} else if (mMiddle.getText().toString().equals("Performance Notes")) {
			mTop.setText("Performance Notes");
			mMiddle.setText("Recordings");
			mBottom.setText("Help");
		} else if (mMiddle.getText().toString().equals("Recordings")) {
			mTop.setText("Recordings");
			mMiddle.setText("Help");
			mBottom.setText(" ");
		}
	}

	/**
	 * Method for moving text down one
	 */
	private void switchTextDown() {
		if (mMiddle.getText().toString().equals("Help")) {
			mTop.setText("Performance Notes");
			mMiddle.setText("Recordings");
			mBottom.setText("Help");
		} else if (mMiddle.getText().toString().equals("Recordings")) {
			mTop.setText("Statistics");
			mMiddle.setText("Performance Notes");
			mBottom.setText("Recordings");
		} else if (mMiddle.getText().toString().equals("Performance Notes")) {
			mTop.setText("Start");
			mMiddle.setText("Statistics");
			mBottom.setText("Performance Notes");
		} else if (mMiddle.getText().toString().equals("Statistics")) {
			mTop.setText(" ");
			mMiddle.setText("Start");
			mBottom.setText("Statistics");
		}
	}

	/**
	 * Here we read the file, extracting the character name speaking the line.
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void readFile() throws IOException {
		// TODO: Need to make sure a new act starts on a new page.

		String text = "";
		AssetManager am = getAssets();

		InputStream is;
		BufferedInputStream bis;
		DataInputStream dis;
		String line = null;
		int lineNo = -1;
		int actNo = 0;
		int pageNo = 1;

		mDbAdapter = new PlayDbAdapter(this);
		mDbAdapter.open();

		// Try to open the file, and alert the user if file doesn't exist
		try {
			is = am.open("earnest.txt");
			bis = new BufferedInputStream(is);
			dis = new DataInputStream(bis);
		} catch (IOException e) {
			// TODO: Add proper error handling. Show popup to user informing
			// them of the missing file (need to create their own/re-install)
			// and then close the app.
			Log.e(TAG, "Error");
			return;
		}

		// Read line-by-line and pick out the relevent information
		while (dis.available() != 0) {

			lineNo++;

			line = dis.readLine();

			// Split line into array of words
			String words[] = line.split("\\s+");
			String firstWord = words[0];

			// Keep a count of which Act we're on
			// TODO: This will need to be better suited for other files.
			if (firstWord.equals("FIRST") || firstWord.equals("SECOND")
					|| firstWord.equals("THIRD")) {
				actNo++;
				Log.d(TAG, Integer.toString(actNo));
			}

			// Keep count of what page we're on (23 lines/page)
			// TODO: Subject to change depending on testing
			if ((lineNo % 23) == 0 && lineNo != 0) {
				pageNo++;
			}

			// Check our firstWord is a character name
			if (isCharacter(firstWord)) {
				// If the first word doesn't contain a period (.) then we need
				// to take the second word as well as part of the character
				// name.
				if (!firstWord.contains(".")) {
					firstWord += " " + words[1];

					text = "";
					for (int j = 2; j < words.length; j++) {
						text += words[j] + " ";
					}
					// If the second word is "and" then it is two characters
					// delievering a line and we need to get the other
					// characters name.
					if (words[1].equals("and")) {
						firstWord += " " + words[2] + ".";

						text = "";
						for (int j = 3; j < words.length; j++) {
							text += words[j] + " ";
						}
						// Handle the rest of the data that hasn't yet been
						// filtered
					} else if (!words[1].contains(".")) {
						firstWord = "STAGE.";

						text = "";
						for (int j = 0; j < words.length; j++) {
							text += words[j] + " ";
						}

						// Log.d(TAG, firstWord + " :::: " + text);
					}
				}
				// If the firstWord isn't a character, then it is a stage
				// direction
			} else {
				firstWord = "STAGE.";

				text = "";
				for (int j = 0; j < words.length; j++) {
					text += words[j] + " ";
				}
			}

			// If we didn't manage to populate "text" from the previous if
			// statements, then do it here.
			if (text.equals("")) {
				for (int j = 1; j < words.length; j++) {
					text += words[j] + " ";
				}
			}

			// Once we have all the data picked out from current line in text
			// file, create a new row in the database. Filter out text we don't
			// want in our database.
			if (!isAllUpperCase(words[0])) {
				mDbAdapter.createPlay(lineNo, firstWord, text, actNo, pageNo,
						"N", "N", "N", 0, 0, 0);
				// If we're not adding to the database, then we need to reduce
				// the line count.
			} else {
				lineNo--;
			}

			Log.d(TAG, Integer.toString(pageNo));
			Log.i(TAG, firstWord + " :::: " + text);

			// Clear "text" before we read next line
			text = "";
		}

		// Cleanup
		is.close();
		bis.close();
		dis.close();
		onDestroy();

	}

	/**
	 * Method for checking if the first word of the current line is a character
	 * name.
	 * 
	 * @param word
	 * @return
	 */
	private boolean isCharacter(String word) {
		// Check if the line is a stage direction
		if (word.contains("[") || isAllUpperCase(word)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Method for checking if the current word is entirely capitals.
	 * 
	 * @param word
	 * @return
	 */
	private boolean isAllUpperCase(String word) {
		boolean allCaps = true;
		for (char c : word.toCharArray()) {
			if (Character.isLowerCase(c)) {
				allCaps = false;
				break;
			}
		}
		return allCaps;
	}

	/**
	 * Close adapter when we are finished.
	 * 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDbAdapter != null) {
			mDbAdapter.close();
		}
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
			// database doesn't exist yet.
		}

		return checkDB != null ? true : false;

	}
}
