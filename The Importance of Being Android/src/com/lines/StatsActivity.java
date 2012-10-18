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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
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
	private ArrayAdapter<CharSequence> mAdapterChar;
	private ArrayAdapter<CharSequence> mAdapterAct;
	private ArrayAdapter<CharSequence> mAdapterPage;
	private int resource;

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

		// Set contents of Character Spinner
		mAdapterChar = ArrayAdapter.createFromResource(StatsActivity.this,
				R.array.stats_char_array, R.layout.spinner_text_layout);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		// Set contents of Act Spinner
		mAdapterAct = ArrayAdapter.createFromResource(StatsActivity.this,
				R.array.stats_act_array, R.layout.spinner_text_layout);
		mAdapterAct
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAct.setAdapter(mAdapterAct);

		mAct.setOnItemSelectedListener(new MyOnItemSelectedListener());

		mClear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup();
			}
		});
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
	public class MyOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {

			// Initialise resource
			resource = R.array.stats_act1_array;

			if (mAct.getSelectedItem().equals("Act II")) {
				resource = R.array.stats_act2_array;
			} else if (mAct.getSelectedItem().equals("Act III")) {
				resource = R.array.stats_act3_array;
			}

			mAdapterPage = ArrayAdapter.createFromResource(StatsActivity.this,
					resource, R.layout.spinner_text_layout);
			mAdapterPage
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mPage.setAdapter(mAdapterPage);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}

}
