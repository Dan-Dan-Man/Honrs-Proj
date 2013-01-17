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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.lines.R;
import com.lines.classes.LinesApp;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Options Screen where the user can choose the part they want to select and
 * the part of the play to rehearse. They can also make different configurations
 * to suit their needs.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class OptionsActivity extends Activity {

	private Button mContinue;
	private Spinner mChar;
	private Spinner mAct;
	private Spinner mPage;
	private Spinner mMode;
	private CheckBox mCue;
	private CheckBox mRandom;
	private CheckBox mOwn;
	private CheckBox mStage;
	private ImageButton mCueHelp;
	private ImageButton mRandomHelp;
	private ImageButton mOwnLineHelp;
	private ImageButton mStageHelp;
	private ImageButton mModeHelp;
	private ArrayAdapter<String> mAdapterChar;
	private ArrayAdapter<String> mAdapterAct;
	private ArrayAdapter<String> mAdapterPage;
	private String pageNo;
	private String actNo;
	private String character;
	private boolean rehearsal;
	private boolean cue;
	private boolean random;
	private boolean ownLines;
	private boolean stage;
	private ArrayList<String> characters = new ArrayList<String>();
	private ArrayList<String> acts = new ArrayList<String>();
	private ArrayList<String> pages;
	private PlayDbAdapter mDbAdapter;
	private Cursor mCursor;

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
		mRandom = (CheckBox) findViewById(R.id.checkRandom);
		mOwn = (CheckBox) findViewById(R.id.checkOwnLines);
		mStage = (CheckBox) findViewById(R.id.checkStage);

		mModeHelp = (ImageButton) findViewById(R.id.imageButtonMode);
		mCueHelp = (ImageButton) findViewById(R.id.imageButtonCue);
		mRandomHelp = (ImageButton) findViewById(R.id.imageButtonRandom);
		mOwnLineHelp = (ImageButton) findViewById(R.id.imageButtonOwnLine);
		mStageHelp = (ImageButton) findViewById(R.id.imageButtonStage);

		LinesApp app = (LinesApp) this.getApplication();
		mDbAdapter = app.getPlayAdapter();

		// Initialise Spinners
		populateCharacters();

		mChar.setEnabled(false);

		// Set listeners for the spinners
		mAct.setOnItemSelectedListener(new ActOnItemSelectedListener());
		mMode.setOnItemSelectedListener(new ModeOnItemSelectedListener());
		mChar.setOnItemSelectedListener(new CharOnItemSelectedListener());

		// Set whether we can select character based on the OwnLines Checkbox
		mOwn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mOwn.isChecked()) {
					mChar.setEnabled(true);
					populateActs(true);
				} else {
					mChar.setEnabled(false);
					populateActs(false);
				}
			}
		});

		// Store user's configurations and move to main screen
		mContinue.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Set user's checkbox configurations
				cue = mCue.isChecked();
				ownLines = mOwn.isChecked();
				random = mRandom.isChecked();
				stage = mStage.isChecked();

				pageNo = mPage.getSelectedItem().toString();
				actNo = mAct.getSelectedItem().toString();

				if (mChar.isEnabled()) {
					character = mChar.getSelectedItem().toString();
				} else {
					character = "All";
				}

				if (mMode.getSelectedItem().toString().equals("Rehearsal")) {
					rehearsal = true;
				} else {
					rehearsal = false;
				}

				// Once we obtain the user's selection, we need to isolate the
				// page number
				String words[] = pageNo.split("\\s+");
				pageNo = words[0];

				Intent i = new Intent(OptionsActivity.this, MainActivity.class);
				// Pass through User selection
				i.putExtra("EXTRA_ACT", actNo);
				i.putExtra("EXTRA_PAGE", pageNo);
				i.putExtra("EXTRA_CHAR", character);
				i.putExtra("EXTRA_MODE", rehearsal);
				i.putExtra("EXTRA_CUE", cue);
				i.putExtra("EXTRA_OWN", ownLines);
				i.putExtra("EXTRA_RANDOM", random);
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

		mRandomHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("random");
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

		mCue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mOwn.setChecked(false);
					mOwn.setEnabled(false);
				} else {
					mOwn.setEnabled(true);
				}
			}
		});

		mOwn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mCue.setChecked(false);
					mCue.setEnabled(false);
				} else {
					mCue.setEnabled(true);
				}
			}
		});
	}

	/**
	 * Here we get the list of characters in the database
	 * 
	 */
	private void populateCharacters() {
		mCursor = mDbAdapter.fetchAllLines();

		// First get the data from "character" column and filter out unwanted
		// characters (e.g. STAGE)
		if (mCursor.moveToFirst()) {
			do {
				String character = mCursor.getString(mCursor
						.getColumnIndex("character"));
				if (!(character.equals("STAGE.") || character.contains("and"))) {
					characters.add(character);
				}
			} while (mCursor.moveToNext());
		}

		HashMap<String, Integer> charOccur = new HashMap<String, Integer>();

		// Get the number of lines spoken by each character and store in HashMap
		Set<String> unique = new HashSet<String>(characters);
		for (String key : unique) {
			charOccur.put(key, Collections.frequency(characters, key));
		}

		characters.clear();

		// Sort character list based on the number of lines they have
		while (charOccur.size() > 0) {
			int max = Collections.max(charOccur.values());
			characters.add(getKeyByValue(charOccur, max));
			charOccur.remove(getKeyByValue(charOccur, max));
		}

		// Set contents of Character Spinner
		mAdapterChar = new ArrayAdapter<String>(OptionsActivity.this,
				R.layout.spinner_text_layout, characters);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		mCursor.close();
	}

	/**
	 * Get the key of the HashMap based on the value.
	 * 
	 * @param map
	 *            - the map we want to search through
	 * @param value
	 *            - the element we are looking for in the map
	 * @return - the key in the map associated with value
	 * 
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Here we get the number of acts in the database. "Filter" decides if we
	 * need to filter out Acts which the selected character doesn't appear in.
	 * 
	 * @param filter
	 */
	private void populateActs(boolean filter) {
		acts = new ArrayList<String>();
		if (filter) {
			mCursor = mDbAdapter.fetchActs(mChar.getSelectedItem().toString());
		} else {
			mCursor = mDbAdapter.fetchAllLines();
		}

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
	}

	/**
	 * Here we get the number of pages in the database
	 * 
	 */
	private void populatePages(String act) {
		pages = new ArrayList<String>();

		if (mChar.isEnabled()) {
			mCursor = mDbAdapter.fetchFilteredPages(act, mChar
					.getSelectedItem().toString());
		} else {
			mCursor = mDbAdapter.fetchAllPages(act);
		}

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

		// Finally sort the page numbers
		int currentPage = findMin(pages);
		ArrayList<String> temp = new ArrayList<String>();
		for (int i = 0; i < pages.size(); i++) {
			if (Integer.parseInt(pages.get(i)) == currentPage) {
				temp.add(pages.get(i));
				pages.remove(i);
				i = -1;
				if (pages.size() > 0) {
					currentPage = findMin(pages);
				}
			}
		}

		// Set contents of Page Spinner
		mAdapterPage = new ArrayAdapter<String>(OptionsActivity.this,
				R.layout.spinner_text_layout, temp);
		mAdapterPage
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPage.setAdapter(mAdapterPage);

		mCursor.close();
	}

	/**
	 * Find minimum value in ArrayList
	 * 
	 * @param pages
	 *            - arraylist to search
	 * @return - minimum value in list
	 * 
	 */
	private int findMin(ArrayList<String> pages) {
		int min = Integer.MAX_VALUE;
		for (String page : pages) {
			if (Integer.parseInt(page) < min) {
				min = Integer.parseInt(page);
			}
		}
		return min;
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
		if (msg.equals("cue")) {
			text.setText(R.string.cue_help);
		} else if (msg.equals("random")) {
			text.setText(R.string.random_help);
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
	 * This class updates the available configurations depending on the
	 * selection made in the mode spinner.
	 * 
	 * @author Daniel Muir, s0930256
	 * 
	 */
	public class ModeOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			if (mMode.getSelectedItem().equals("Normal")) {
				if (!mRandom.isChecked()) {
					mOwn.setEnabled(true);
				}
				mChar.setEnabled(false);
				populateActs(false);
			} else {
				mOwn.setEnabled(false);
				mOwn.setChecked(false);
				mChar.setEnabled(true);
				populateActs(true);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}
	}

	/**
	 * This class updates the page spinner depending on the selection made in
	 * the act spinner.
	 * 
	 * @author Daniel Muir, s0930256
	 * 
	 */
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

	/**
	 * This class updates the page spinner depending on the selection made in
	 * the act spinner.
	 * 
	 * @author Daniel Muir, s0930256
	 * 
	 */
	public class CharOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			populateActs(true);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}
	}

}
