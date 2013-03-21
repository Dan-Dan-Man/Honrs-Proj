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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.lines.R;
import com.lines.classes.LinesApp;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Home Screen where the user will start off. From here they can begin
 * rehearsing, view statistics, recordings and performance notes or view and
 * edit settings.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class HomeActivity extends Activity {

	private PlayDbAdapter mDbAdapter;
	private ImageButton mUpArrow;
	private ImageButton mDownArrow;
	private TextView mTop;
	private TextView mMiddle;
	private TextView mBottom;
	private LinesApp app;
	private Runnable populateDB;
	private ProgressDialog m_ProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);

		mTop = (TextView) findViewById(R.id.textTop);
		mMiddle = (TextView) findViewById(R.id.textMiddle);
		mBottom = (TextView) findViewById(R.id.textBottom);

		mUpArrow = (ImageButton) findViewById(R.id.imageButtonArrowUp);
		mDownArrow = (ImageButton) findViewById(R.id.imageButtonArrowDown);

		// Create an object of the application class so we can check if a
		// database for the play exists
		app = (LinesApp) this.getApplication();

		// If a database doesn't exist, then ask the user to select a script to
		// load
		if (!app.dbExists()) {
			selectScript();
		}

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
			Intent i = new Intent(this, RecordingsActivity.class);
			startActivityForResult(i, 0);
		} else if (mMiddle.getText().toString().equals("Settings")) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, 0);
		}
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
			mBottom.setText("Settings");
		} else if (mMiddle.getText().toString().equals("Recordings")) {
			mTop.setText("Recordings");
			mMiddle.setText("Settings");
			mBottom.setText(" ");
		}
	}

	/**
	 * Method for moving text down one
	 */
	private void switchTextDown() {
		if (mMiddle.getText().toString().equals("Settings")) {
			mTop.setText("Performance Notes");
			mMiddle.setText("Recordings");
			mBottom.setText("Settings");
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
	 * Provide the user with a prompt to select a script to load into database
	 * if one doesn't currently exist
	 * 
	 */
	private void selectScript() {
		LayoutInflater li = LayoutInflater.from(this);
		View scriptsView = li.inflate(R.layout.load_script_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(scriptsView);

		final Spinner selection = (Spinner) scriptsView
				.findViewById(R.id.spinnerScripts);

		ArrayList<String> filesList = new ArrayList<String>();

		// Search scripts directory on SD card to get list of scripts loaded.
		File directory = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/scripts");

		File[] scripts = directory.listFiles();

		// Populate arraylist with files on SD card
		for (int i = 0; i < scripts.length; i++) {
			if (scripts[i].getName().endsWith(".txt")) {
				String filename = scripts[i].getName();
				filename = filename.replace(".txt", "");
				filesList.add(filename);
			}
		}

		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, filesList);

		selection.setAdapter(aa);

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Load Script",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								final String script = selection
										.getSelectedItem().toString();
								if (script != null) {
									dialog.dismiss();
									createThread(script);
									createSettings(script);
								}
							}
						})
				.setNegativeButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	/**
	 * This method creates a new thread to display a notification of the script
	 * being loaded.
	 * 
	 * @param script
	 *            - the selected script to load
	 */
	private void createThread(final String script) {
		populateDB = new Runnable() {
			public void run() {
				try {
					readFile(script);
					m_ProgressDialog.dismiss();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(null, populateDB, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(HomeActivity.this,
				"Please wait...", "Loading Script...", true);
	}

	/**
	 * Create a settings file with the user selected script and other default
	 * settings.
	 * 
	 * @param script
	 *            - the selected script name we are saving to file
	 */
	private void createSettings(String script) {
		File settings = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/.settings.sav");
		if (settings.exists()) {
			settings.delete();
		}

		FileWriter fileWriter;
		BufferedWriter writer;

		// Write the three settings to file
		try {
			fileWriter = new FileWriter(settings);
			writer = new BufferedWriter(fileWriter);

			writer.write(script);
			writer.newLine();
			writer.write("1");
			writer.newLine();
			writer.write("No");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Here we read the script the user has selected, extracting the revelent
	 * info, and store into database
	 * 
	 * @param script
	 *            - the selected script we are reading
	 * @throws IOException
	 */
	private void readFile(String script) throws IOException {
		String text = "";

		InputStream is;
		BufferedInputStream bis;
		DataInputStream dis;
		String line = null;
		int lineNo = -1;
		int actNo = 0;
		int pageNo = 1;

		File file = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/scripts/" + script + ".txt");

		// Get adapter
		mDbAdapter = app.getPlayAdapter();

		// Try to open the file, and alert the user if file doesn't exist
		try {
			is = new FileInputStream(file);
			bis = new BufferedInputStream(is);
			dis = new DataInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
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
			if (words[words.length - 1].equals("ACT")) {
				actNo++;
			}

			// Keep count of what page we're on (23 lines/page)
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
			// want in our database (Key words).
			if (!isAllUpperCase(words[0])) {
				firstWord = firstWord.substring(0, firstWord.length() - 1);
				mDbAdapter.createPlay(lineNo, firstWord, text, actNo, pageNo,
						"N", "N", 0, 0, 0);
				// If we're not adding to the database, then we need to reduce
				// the line count.
			} else {
				lineNo--;
			}

			// Clear "text" before we read next line
			text = "";
		}

		// Cleanup
		is.close();
		bis.close();
		dis.close();

	}

	/**
	 * Method for checking if the first word of the current line is a character
	 * name.
	 * 
	 * @param word
	 *            - check to see if this String is a character in the play
	 * @return - true of false based on the result
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
	 *            - check to see if this String is entirely uppercase
	 * @return allCaps - true or false based on the result of the method
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
}
