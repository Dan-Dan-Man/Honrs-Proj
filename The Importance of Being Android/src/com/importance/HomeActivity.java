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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.R;

/**
 * The Home Screen where the user will start off.
 * 
 * @author Dan
 * 
 */

public class HomeActivity extends Activity {

	private ImageButton mUpArrow;
	private ImageButton mDownArrow;
	private TextView mTop;
	private TextView mMiddle;
	private TextView mBottom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);

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
			// TODO: Create performance notes screen
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
}
