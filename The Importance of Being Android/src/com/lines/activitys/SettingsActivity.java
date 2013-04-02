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
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;
import com.lines.classes.LinesApp;
import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Settings screen. Here the user can select which script to load, how many
 * words to reveal when they require a prompt and if they wish to have audio
 * auto-play for lines with an assigned file when a new page is loaded
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class SettingsActivity extends Activity {

	private Spinner mScripts;
	private Spinner mPrompts;
	private Spinner mAuto;
	private Button mSave;
	private Button mReset;
	private ImageButton mPromptsHelp;
	private ImageButton mAutoHelp;
	private PlayDbAdapter mDbAdapter;
	private NoteDbAdapter mNDbAdapter;
	private LinesApp app;
	private Runnable populateDB;
	private ProgressDialog m_ProgressDialog = null;
	private String defaultScript;
	private String defaultPrompts;
	private String defaultAuto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		mScripts = (Spinner) findViewById(R.id.spinnerScripts);
		mPrompts = (Spinner) findViewById(R.id.spinnerPrompts);
		mAuto = (Spinner) findViewById(R.id.spinnerAuto);
		mSave = (Button) findViewById(R.id.buttonSave);
		mReset = (Button) findViewById(R.id.buttonReset);
		mPromptsHelp = (ImageButton) findViewById(R.id.imageButtonPrompts);
		mAutoHelp = (ImageButton) findViewById(R.id.imageButtonAuto);

		app = (LinesApp) this.getApplication();

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
		mScripts.setAdapter(aa);

		try {
			populateSpinners();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String script = mScripts.getSelectedItem().toString();
				if (!defaultScript.equals(script)) {
					confirmScriptSelection(script);
				} else {
					saveSettings();
					finish();
				}
			}
		});

		mReset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					populateSpinners();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		mPromptsHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPopup("prompts");
			}
		});

		mAutoHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPopup("auto");
			}
		});
	}

	/**
	 * Set the default selection of all spinners to their
	 * 
	 * @throws IOException
	 */
	private void populateSpinners() throws IOException {
		File settings = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/.settings.sav");

		InputStream is;
		BufferedInputStream bis;
		DataInputStream dis;

		try {
			is = new FileInputStream(settings);
			bis = new BufferedInputStream(is);
			dis = new DataInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		defaultScript = dis.readLine();
		for (int i = 0; i < mScripts.getCount(); i++) {
			if (mScripts.getItemAtPosition(i).toString().equals(defaultScript)) {
				mScripts.setSelection(i);
				break;
			}
		}

		defaultPrompts = dis.readLine();
		for (int i = 0; i < mPrompts.getCount(); i++) {
			if (mPrompts.getItemAtPosition(i).toString().equals(defaultPrompts)) {
				mPrompts.setSelection(i);
				break;
			}
		}

		defaultAuto = dis.readLine();
		for (int i = 0; i < mAuto.getCount(); i++) {
			if (mAuto.getItemAtPosition(i).toString().equals(defaultAuto)) {
				mAuto.setSelection(i);
				break;
			}
		}
	}

	/**
	 * Update the Settings file with the user's selections
	 * 
	 */
	private void saveSettings() {
		File settings = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/.settings.sav");
		if (settings.exists()) {
			settings.delete();
		}

		FileWriter fileWriter;
		BufferedWriter writer;

		try {
			fileWriter = new FileWriter(settings);
			writer = new BufferedWriter(fileWriter);

			writer.write(mScripts.getSelectedItem().toString());
			writer.newLine();
			writer.write(mPrompts.getSelectedItem().toString());
			writer.newLine();
			writer.write(mAuto.getSelectedItem().toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(getApplicationContext(), "Settings saved!",
				Toast.LENGTH_LONG).show();
	}

	/**
	 * Before loading a new script, first get user's confirmation.
	 * 
	 * @param script
	 *            - the selected script the user wants to load
	 */
	private void confirmScriptSelection(final String script) {

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Overwrite script?")
				.setMessage(
						"Loading a new script will delete all data "
								+ "associated with current loaded script"
								+ " (except recordings). Do you wish to continue?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								createThread(script);
								saveSettings();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).show();
	}

	/**
	 * This method creates a new thread to display a notification of the script
	 * being loaded.
	 * 
	 * @param script
	 */
	private void createThread(final String script) {
		populateDB = new Runnable() {
			public void run() {
				try {
					loadScript(script);
					m_ProgressDialog.dismiss();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(null, populateDB, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(SettingsActivity.this,
				"Please wait...", "Loading Script...", true);
	}

	/**
	 * Delete the databases that already exist, and populate a new one with the
	 * user's selected script
	 * 
	 * @param script
	 *            - script file we are populating the database with
	 * @throws IOException
	 */
	private void loadScript(String script) throws IOException {

		// Get adapters
		mDbAdapter = app.getPlayAdapter();
		mNDbAdapter = app.getNoteAdapter();

		// Reset tables for Script and Notes
		mDbAdapter.deleteTable();
		mNDbAdapter.deleteTable();

		String text = "";
		InputStream is;
		BufferedInputStream bis;
		DataInputStream dis;
		String line = null;
		int lineNo = -1;
		int actNo = 0;
		int pageNo = 1;

		// Add file extension back on
		script = script + ".txt";
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/scripts/" + script);

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
			if (firstWord.equals("FIRST") || firstWord.equals("SECOND")
					|| firstWord.equals("THIRD")) {
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
			// want in our database.
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

		finish();

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

	/**
	 * This method creates and shows a popup to the user, displaying a relevent
	 * help message.
	 * 
	 * @param msg
	 *            - decides which message we are displaying to the user
	 * 
	 */
	private void showPopup(String msg) {
		// Create popup
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.help_popup_layout,
				null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
		TextView text = (TextView) popupView.findViewById(R.id.text);

		// Here we decide what help message to display to the user.
		if (msg.equals("prompts")) {
			text.setText(R.string.prompt_help);
		} else {
			text.setText(R.string.auto_help);
		}

		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		popupWindow.showAsDropDown(mPrompts, 50, -150);
	}
}
