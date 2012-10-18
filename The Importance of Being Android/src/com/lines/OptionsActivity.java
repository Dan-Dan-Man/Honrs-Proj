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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.R;

/**
 * The Options Screen where the user can choose the part they want to select and
 * the part of the play to rehearse. They can also make diffirent configurations
 * to suit their needs.
 * 
 * @author Dan
 * 
 */

public class OptionsActivity extends Activity {

	private Spinner mChar;
	private Spinner mAct;
	private Spinner mPage;
	private ImageButton mCueHelp;
	private ImageButton mAudioHelp;
	private ImageButton mOwnLineHelp;
	private ImageButton mStageHelp;
	private ArrayAdapter<CharSequence> mAdapterChar;
	private ArrayAdapter<CharSequence> mAdapterAct;
	private ArrayAdapter<CharSequence> mAdapterPage;
	private int resource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options_layout);

		mChar = (Spinner) findViewById(R.id.spinnerCharacter);
		mAct = (Spinner) findViewById(R.id.spinnerAct);
		mPage = (Spinner) findViewById(R.id.spinnerPage);

		mCueHelp = (ImageButton) findViewById(R.id.imageButtonCue);
		mAudioHelp = (ImageButton) findViewById(R.id.imageButtonAudio);
		mOwnLineHelp = (ImageButton) findViewById(R.id.imageButtonOwnLine);
		mStageHelp = (ImageButton) findViewById(R.id.imageButtonStage);

		// Set contents of Character Spinner
		mAdapterChar = ArrayAdapter.createFromResource(OptionsActivity.this,
				R.array.char_array, R.layout.spinner_text_layout);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		// Set contents of Act Spinner
		mAdapterAct = ArrayAdapter.createFromResource(OptionsActivity.this,
				R.array.act_array, R.layout.spinner_text_layout);
		mAdapterAct
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAct.setAdapter(mAdapterAct);

		mAct.setOnItemSelectedListener(new MyOnItemSelectedListener());

		// Listeners to display relevent help messages to the user.
		mCueHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("cue");
			}
		});

		mAudioHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup("audio");
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
	 * 
	 * When the user has finished their configurations, they move on to the Main
	 * Screen.
	 * 
	 * @param v
	 */
	public void cont(View v) {
		Intent i = new Intent(this, MainActivity.class);
		startActivityForResult(i, 0);
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
		} else if (msg.equals("audio")) {
			text.setText(R.string.audio_help);
		} else if (msg.equals("own line")) {
			text.setText(R.string.own_line_help);
		} else if (msg.equals("stage")) {
			text.setText(R.string.stage_help);
		}

		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		popupWindow.showAsDropDown(mCueHelp, 50, -30);
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
			if (mAct.getSelectedItem().equals("Act I")) {
				resource = R.array.act1_array;
			} else if (mAct.getSelectedItem().equals("Act II")) {
				resource = R.array.act2_array;
			} else if (mAct.getSelectedItem().equals("Act III")) {
				resource = R.array.act3_array;
			}
			mAdapterPage = ArrayAdapter.createFromResource(
					OptionsActivity.this, resource,
					R.layout.spinner_text_layout);
			mAdapterPage
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mPage.setAdapter(mAdapterPage);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}

}
