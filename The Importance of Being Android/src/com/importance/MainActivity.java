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
import android.widget.Button;
import android.widget.TextView;

import com.example.android.R;

/**
 * The Main Screen where the user will rehearse their lines.
 * 
 * @author Dan
 * 
 */

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private TextView line1;
	private TextView line2;
	private TextView char3;
	private TextView line3;
	private TextView char4;
	private TextView mPage;
	private Button mNext;
	private Button mPrev;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		line1 = (TextView) findViewById(R.id.textLine1);
		line2 = (TextView) findViewById(R.id.textLine2);
		char3 = (TextView) findViewById(R.id.textChar3);
		line3 = (TextView) findViewById(R.id.textLine3);
		char4 = (TextView) findViewById(R.id.textChar4);

		mNext = (Button) findViewById(R.id.buttonNext);
		mPrev = (Button) findViewById(R.id.buttonPrev);
		mPage = (TextView) findViewById(R.id.textPage);

		// When Next button is pressed, play jumps until user's next line.
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mPage.getText().toString().contains("12")) {
					line2.setVisibility(View.VISIBLE);
					line3.setVisibility(View.VISIBLE);
					char3.setVisibility(View.VISIBLE);
					char4.setVisibility(View.VISIBLE);
				}
			}
		});

		// When Next button is long pressed, play jumps to next page.
		mNext.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				mPage.setText(" Page 13");
				line2.setVisibility(View.INVISIBLE);
				line3.setVisibility(View.INVISIBLE);
				char3.setVisibility(View.INVISIBLE);
				char4.setVisibility(View.INVISIBLE);

				line1.setText("I never knew you when you weren’t...");
				return true;
			}
		});

		// When Prev button is pressed, play jumps until user's prev line.
		mPrev.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				line2.setVisibility(View.INVISIBLE);
				line3.setVisibility(View.INVISIBLE);
				char3.setVisibility(View.INVISIBLE);
				char4.setVisibility(View.INVISIBLE);
			}
		});

		// When Prev button is long pressed, play jumps to prev page.
		mPrev.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				mPage.setText(" Page 12");

				line1.setText("Is that clever?");

				return true;
			}
		});
	}

}
