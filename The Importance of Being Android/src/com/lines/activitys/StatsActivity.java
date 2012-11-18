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
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.R;
import com.lines.database.PlayDbAdapter;

/**
 * The Stats screen where the user can view some statistics of their
 * performances with choice of filtering results. User can also review
 * performace notes and recordings made.
 * 
 * @author Dan
 * 
 */

public class StatsActivity extends Activity {

	private Spinner mChar;
	private Spinner mAct;
	private Spinner mPage;
	private Button mClear;
	private TextView mViewsNum;
	private TextView mViewsPercent;
	private TextView mPromptsNum;
	private TextView mPromptsPercent;
	private TextView mCompleteNum;
	private TextView mCompletePercent;
	private ArrayAdapter<String> mAdapterChar;
	private ArrayAdapter<String> mAdapterAct;
	private ArrayAdapter<String> mAdapterPage;
	private ArrayList<String> characters = new ArrayList<String>();
	private ArrayList<String> acts = new ArrayList<String>();
	private ArrayList<String> pages;
	private PlayDbAdapter mDbAdapter;
	private Cursor mCursor;
	private String currentPage = "All";
	private String currentAct = "All";
	private String character = "All";
	private static final String TAG = "StatsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats_layout);

		mChar = (Spinner) findViewById(R.id.spinnerCharacter);
		mAct = (Spinner) findViewById(R.id.spinnerAct);
		mPage = (Spinner) findViewById(R.id.spinnerPage);
		mClear = (Button) findViewById(R.id.buttonStats);

		mViewsNum = (TextView) findViewById(R.id.textViewsNum);
		mViewsPercent = (TextView) findViewById(R.id.textViewsPercent);
		mPromptsNum = (TextView) findViewById(R.id.textPromptsNum);
		mPromptsPercent = (TextView) findViewById(R.id.textPromptsPercent);
		mCompleteNum = (TextView) findViewById(R.id.textCompleteNum);
		mCompletePercent = (TextView) findViewById(R.id.textCompletePercent);

		mDbAdapter = new PlayDbAdapter(this);

		// Initialise Spinners
		populateCharacters();

		// Retrieve User choice from previous Activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			currentPage = extras.getString("EXTRA_PAGE");
			currentAct = extras.getString("EXTRA_ACT");
			character = extras.getString("EXTRA_CHARACTER");
		} else {
			Log.i(TAG, "No user choice to pass through");
		}

		for (int i = 0; i < mChar.getCount(); i++) {
			if (mChar.getItemAtPosition(i).equals(character)) {
				mChar.setSelection(i);
				break;
			}
		}

		mAct.setOnItemSelectedListener(new ActOnItemSelectedListener());
		mChar.setOnItemSelectedListener(new CharOnItemSelectedListener());

		mClear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup();
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

		HashMap<String, Integer> charOccur = new HashMap<String, Integer>();

		// Get the number of lines spoken by each character and store in HashMap
		Set<String> unique = new HashSet<String>(characters);
		for (String key : unique) {
			charOccur.put(key, Collections.frequency(characters, key));
		}

		characters.clear();

		characters.add("All");

		// Sort character list based on the number of lines they have
		while (charOccur.size() > 0) {
			int max = Collections.max(charOccur.values());
			characters.add(getKeyByValue(charOccur, max));
			charOccur.remove(getKeyByValue(charOccur, max));
		}

		// Set contents of Character Spinner
		mAdapterChar = new ArrayAdapter<String>(StatsActivity.this,
				R.layout.spinner_text_layout, characters);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		mCursor.close();
		onDestroy();
	}

	/**
	 * Get the key of the HashMap based on the value.
	 * 
	 * @param map
	 * @param value
	 * @return
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
	 * Here we get the number of acts in the database
	 * 
	 */
	private void populateActs() {
		acts = new ArrayList<String>();
		mDbAdapter.open();
		if (mChar.getSelectedItem().toString().equals("All")) {
			mCursor = mDbAdapter.fetchAllLines();
		} else {
			mCursor = mDbAdapter.fetchActs(mChar.getSelectedItem().toString());
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
		acts.add("All");
		acts.addAll(h);

		// Set contents of Act Spinner
		mAdapterAct = new ArrayAdapter<String>(StatsActivity.this,
				R.layout.spinner_text_layout, acts);
		mAdapterAct
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAct.setAdapter(mAdapterAct);

		mCursor.close();
		onDestroy();

		for (int i = 0; i < mAct.getCount(); i++) {
			if (mAct.getItemAtPosition(i).equals(currentAct)) {
				mAct.setSelection(i);
				break;
			}
		}

		currentAct = "All";
	}

	/**
	 * Here we get the number of pages in the database
	 * 
	 */
	// TODO: Convert act numbers to roman numerals maybe
	private void populatePages(String act) {
		pages = new ArrayList<String>();
		mDbAdapter.open();
		// If both spinners are "All"
		if (act.equals("All")
				&& mChar.getSelectedItem().toString().equals("All")) {
			mCursor = mDbAdapter.fetchAllLines();
			// If character spinner is "All"
		} else if (mChar.getSelectedItem().toString().equals("All")
				&& !act.equals("All")) {
			mCursor = mDbAdapter.fetchAllPages(act);
			// If act spinner is "All"
		} else if (!mChar.getSelectedItem().toString().equals("All")
				&& act.equals("All")) {
			mCursor = mDbAdapter.fetchActs(mChar.getSelectedItem().toString());
			// If neither spinner is "All"
		} else {
			mCursor = mDbAdapter.fetchFilteredPages(act, mChar
					.getSelectedItem().toString());
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

		int pg = findMin(pages);
		ArrayList<String> temp = new ArrayList<String>();
		temp.add("All");
		for (int i = 0; i < pages.size(); i++) {
			if (Integer.parseInt(pages.get(i)) == pg) {
				temp.add(pages.get(i));
				pages.remove(i);
				i = -1;
				if (pages.size() > 0) {
					pg = findMin(pages);
				}
			}
		}

		// Set contents of Page Spinner
		mAdapterPage = new ArrayAdapter<String>(StatsActivity.this,
				R.layout.spinner_text_layout, temp);
		mAdapterPage
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPage.setAdapter(mAdapterPage);

		mCursor.close();
		onDestroy();

		for (int i = 0; i < mPage.getCount(); i++) {
			if (mPage.getItemAtPosition(i).equals(currentPage)) {
				mPage.setSelection(i);
				break;
			}
		}
		currentPage = "All";
	}

	/**
	 * Find minimum value in ArrayList
	 * 
	 * @param pages
	 * @return
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
	 * 
	 */
	public void showPopup() {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = layoutInflater.inflate(R.layout.confirm_popup_layout,
				null);
		final PopupWindow popupWindow = new PopupWindow(popupView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		Button yes = (Button) popupView.findViewById(R.id.yes);
		Button no = (Button) popupView.findViewById(R.id.no);

		yes.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				mViewsNum.setText("-");
				mViewsPercent.setText("-");
				mPromptsNum.setText("-");
				mPromptsPercent.setText("-");
				mCompleteNum.setText("-");
				mCompletePercent.setText("-");
				popupWindow.dismiss();
			}
		});

		no.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		popupWindow.showAsDropDown(mChar, 0, 0);
	}

	/**
	 * This class updates the page spinner depending on the selection made in
	 * the act spinner.
	 * 
	 * @author Dan
	 * 
	 */
	public class ActOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			// Extract the act number that we want to search for
			String act = mAct.getSelectedItem().toString();
			String words[] = act.split("\\s+");
			if (act.equals("All")) {
				act = words[0];
			} else {
				act = words[1];
			}

			populatePages(act);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

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

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * This class updates the page spinner depending on the selection made in
	 * the act spinner.
	 * 
	 * @author Dan
	 * 
	 */
	public class CharOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			populateActs();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}
}
