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

package com.importance;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
	private ArrayAdapter<CharSequence> mAdapterChar;
	private ArrayAdapter<CharSequence> mAdapterAct;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats_layout);

		// Set contents of Character Spinner
		mChar = (Spinner) findViewById(R.id.spinnerCharacter);
		mAdapterChar = ArrayAdapter.createFromResource(StatsActivity.this,
				R.array.stats_char_array, R.layout.spinner_text_layout);
		mAdapterChar
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChar.setAdapter(mAdapterChar);

		// Set contents of Act Spinner
		mAct = (Spinner) findViewById(R.id.spinnerAct);
		mAdapterAct = ArrayAdapter.createFromResource(StatsActivity.this,
				R.array.stats_act_array, R.layout.spinner_text_layout);
		mAdapterAct
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAct.setAdapter(mAdapterAct);
	}

}
