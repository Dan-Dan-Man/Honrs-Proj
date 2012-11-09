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

package com.lines;

// TODO: Filter available page numbers based on character selection
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.R;
import com.lines.database.PlayDbAdapter;

/**
 * The Options Screen where the user can choose the part they want to select and
 * the part of the play to rehearse. They can also make diffirent configurations
 * to suit their needs.
 * 
 * @author Dan
 * 
 */

// TODO: Set characterSpinner enability based on whether ownLines checkbox is
// checked or not.

public class OptionsActivity extends Activity {

	private Button mContinue;
	private Spinner mChar;
	private Spinner mAct;
	private Spinner mPage;
	private Spinner mMode;
	private CheckBox mCue;
	private CheckBox mBreak;
	private CheckBox mOwn;
	private CheckBox mStage;
	private ImageButton mCueHelp;
	private ImageButton mBreakHelp;
	private ImageButton mOwnLineHelp;
	private ImageButton mStageHelp;
	private ImageButton mModeHelp;
	private ArrayAdapter<String> mAdapterChar;
	private ArrayAdapter<String> mAdapterAct;
	private ArrayAdapter<String> mAdapterPage;
	private String pageNo;
	private String actNo;
	private String character;
	private boolean cue;
	private boolean breakUp;
	private boolean ownLines;
	private boolean stage;
	private ArrayList<String> characters = new ArrayList<String>();
	private ArrayList<String> acts = new ArrayList<String>();
	private ArrayList<String> pages;
	private PlayDbAdapter mDbAdapter;
	private Cursor mCursor;
	private static final String TAG = "OptionsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options_layout);

		mChar = (Spinner) findViewById(R.id.spinnerCharacter);
		mAct = (Spinner) findViewById(R.id.spinnerAct);
		mPage = (Spinner) findViewById(R.id.spinnerPage);
		mMode = (Spinner) findViewById(R.id.spinnerMode);
		mContinue = (Button) findViewById(R.id.buttonContinue);
		mCue = (CheckBox) findViewById(R.id.checkCue);
		mBreak = (CheckBox) findViewById(R.id.checkBreak);
		mOwn = (CheckBox) findViewById(R.id.checkOwnLines);
		mStage = (CheckBox) findViewById(R.id.checkStage);

		mModeHelp = (ImageButton) findViewById(R.id.imageButtonMode);
		mCueHelp = (ImageButton) findViewById(R.id.imageButtonCue);
		mBreakHelp = (ImageButton) findViewById(R.id.imageButtonBreak);
		mOwnLineHelp = (ImageButton) findViewById(R.id.imageButtonOwnLine);
		mStageHelp = (ImageButton) findViewById(R.id.imageButtonStage);

		mDbAdapter = new PlayDbAdapter(this);

		// Initialise Spinners
		populateCharacters();
		populateActs();

		mAct.setOnItemSelectedListener(new ActOnItemSelectedListener());
		mMode.setOnItemSelectedListener(new ModeOnItemSelectedListener());

		// Store user's configurations and move to main screen
		mContinue.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Set user's checkbox configurations
				cue = mCue.isChecked();
				ownLines = mOwn.isChecked();
				breakUp = mBreak.isChecked();
				stage = mStage.isChecked();

				pageNo = mPage.getSelectedItem().toString();
				actNo = mAct.getSelectedItem().toString();
				character = mChar.getSelectedItem().toString();

				// Once we obtain the user's selection, we need to isolate the
				// page number
				String words[] = pageNo.split("\\s+");
				pageNo = words[0];
				Log.d(TAG, "Page selected: " + pageNo);

				Intent i = new Intent(OptionsActivity.this, MainActivity.class);
				// Pass through User selection
				i.putExtra("EXTRA_ACT", actNo);
				i.putExtra("EXTRA_PAGE", pageNo);
				i.putExtra("EXTRA_CHAR", character);
				i.putExtra("EXTRA_CUE", cue);
				i.putExtra("EXTRA_OWN", ownLines);
				i.putExtra("EXTRA_BREAKUP", breakUp);
				i.putExtra("EXTRA_STAGE", stage);
				OptionsActivity.this.startActivity(i);
			}
		});

		// Listeners to display relevent help messages to the user.
		mModeHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("mode");
			}
		});

		mCueHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("cue");
			}
		});

		mBreakHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("break");
			}
		});

		mOwnLineHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("own line");
			}
		});

		mStageHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("stage");
			}
		});
	}

	/**
	 * Here we get the list of characters in the database
	 * 
	 */
	private void populateCharacters() {
		mDbAdapter.open();
		mCursor = mDbAdapter.fetchAllLines();

		// First get the data from "character" column and filter out unwanted
		// characters (e.g. STAGE)
		// TODO: "and" could cause later problems. Look to search for
		// certain word than character sequence
		if (mCursor.moveToFirst()) {
			do {
				String character = mCursor.getString(mCursor
						.getColumnIndex("character"));
				if (!(character.equals("STAGE.") || character.contains("and"))) {
					characters.add(character);
				}
			} while (mCursor.moveToNext());
		}

		// Sort characters based on number of lines they have
		Set<String> unique = new HashSet<String>(characters);
		for (String key : unique) {
			Log.d(TAG, (key + ": " + Collections.frequency(characters, key)));
		}

		characters.clear();
		characters.addAll(unique);

		// TODO: Order character list based on importance (number of lines?)

		// Set contents of Character Spinner
		mAdapterChar = new ArrayAdapter<String>(OptionsActivity.this,
				R.layout.spinner_text_layout, characters);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		mCursor.close();
		mDbAdapter.close();
	}

	/**
	 * Here we get the number of acts in the database
	 * 
	 */
	private void populateActs() {
		mDbAdapter.open();
		mCursor = mDbAdapter.fetchAllLines();

		// First get the data from "act" column
		if (mCursor.moveToFirst()) {
			do {
				acts.add("Act "
						+ mCursor.getString(mCursor.getColumnIndex("act")));
			} while (mCursor.moveToNext());
		}

		// Then we remove duplicates to get exact number of acts
		HashSet<String> h = new HashSet<String>();
		h.addAll(acts);
		acts.clear();
		acts.addAll(h);

		// Set contents of Act Spinner
		mAdapterAct = new ArrayAdapter<String>(OptionsActivity.this,
				R.layout.spinner_text_layout, acts);
		mAdapterAct
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAct.setAdapter(mAdapterAct);

		mCursor.close();
		mDbAdapter.close();
	}

	/**
	 * Here we get the number of pages in the database
	 * 
	 */
	// TODO: Convert act numbers to roman numerals maybe
	private void populatePages(String act) {
		pages = new ArrayList<String>();
		mDbAdapter.open();
		mCursor = mDbAdapter.fetchAllPages(act);

		// First get the data from "page" column
		if (mCursor.moveToFirst()) {
			do {
				pages.add(mCursor.getString(mCursor.getColumnIndex("page")));
			} while (mCursor.moveToNext());
		}

		// Then we remove duplicates to get exact number of pages
		HashSet<String> h = new HashSet<String>();
		h.addAll(pages);
		pages.clear();
		pages.addAll(h);

		Object obj = Collections.min(pages);

		// Finally sort the page numbers
		int currentPage = Integer.valueOf((String) obj);
		ArrayList<String> temp = new ArrayList<String>();
		for (int i = 0; i < pages.size(); i++) {
			if (Integer.parseInt(pages.get(i)) == currentPage) {
				temp.add(pages.get(i));
				i = -1;
				currentPage++;
			}
		}

		for (int i = 0; i < temp.size(); i++) {
			Log.d(TAG, temp.get(i));
		}

		// Set contents of Page Spinner
		mAdapterPage = new ArrayAdapter<String>(OptionsActivity.this,
				R.layout.spinner_text_layout, temp);
		mAdapterPage
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPage.setAdapter(mAdapterPage);

		mCursor.close();
		mDbAdapter.close();
	}

	/**
	 * This method creates and shows a popup to the user, displaying a relevent
	 * help message.
	 * 
	 * @param msg
	 * 
	 */
	private void showPopup(String msg) {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.help_popup_layout,
				null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
		TextView text = (TextView) popupView.findViewById(R.id.text);

		// Here we decide what help message to display to the user.
		if (msg.equals("cue")) {
			text.setText(R.string.cue_help);
		} else if (msg.equals("break")) {
			text.setText(R.string.break_help);
		} else if (msg.equals("own line")) {
			text.setText(R.string.own_line_help);
		} else if (msg.equals("stage")) {
			text.setText(R.string.stage_help);
		} else if (msg.equals("mode")) {
			text.setText(R.string.mode_help);
		}

		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		popupWindow.showAsDropDown(mCueHelp, 50, -150);
	}

	/**
	 * This class updates the page spinner depending on the selection made in
	 * the act spinner.
	 * 
	 * @author Dan
	 * 
	 */
	public class ModeOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {

			if (mMode.getSelectedItem().equals("Normal")) {
				mBreak.setEnabled(false);
				mBreak.setChecked(false);
				mOwn.setEnabled(true);
			} else {
				mBreak.setEnabled(true);
				mOwn.setEnabled(false);
				mOwn.setChecked(false);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}

	public class ActOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			// Extract the act number that we want to search for
			String act = mAct.getSelectedItem().toString();
			String words[] = act.split("\\s+");
			act = words[1];

			populatePages(act);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}

}
