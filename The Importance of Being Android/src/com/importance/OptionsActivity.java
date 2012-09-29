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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
	private ArrayAdapter<CharSequence> adapter;
	private int resource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options_layout);

		mChar = (Spinner) findViewById(R.id.spinnerCharacter);
		mAct = (Spinner) findViewById(R.id.spinnerAct);
		mPage = (Spinner) findViewById(R.id.spinnerPage);

		mAct.setOnItemSelectedListener(new MyOnItemSelectedListener());

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
			adapter = ArrayAdapter.createFromResource(OptionsActivity.this,
					resource, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mPage.setAdapter(adapter);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}

	}

}
