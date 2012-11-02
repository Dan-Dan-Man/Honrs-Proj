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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.R;
import com.lines.database.PlayDbAdapter;

/**
 * The Main Screen where the user will rehearse their lines.
 * 
 * @author Dan
 * 
 */

public class MainActivity extends ListActivity {

	private static final String TAG = "MainActivity";
	private TextView mPage;
	private Button mNext;
	private Button mPrev;
	private Cursor mCursor;
	private String[] mFrom;
	private int[] mTo;
	private PlayDbAdapter mDbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		mNext = (Button) findViewById(R.id.buttonNext);
		mPrev = (Button) findViewById(R.id.buttonPrev);
		mPage = (TextView) findViewById(R.id.textPage);

		mDbAdapter = new PlayDbAdapter(this);
		mDbAdapter.open();

		mCursor = mDbAdapter.fetchAllLines();
		startManagingCursor(mCursor);
		fillData();
		registerForContextMenu(getListView());

		// When Next button is pressed, play jumps until user's next line.
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

			}
		});

		// When Next button is long pressed, play jumps to next page.
		mNext.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				return true;
			}
		});

		// When Prev button is pressed, play jumps until user's prev line.
		mPrev.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

		// When Prev button is long pressed, play jumps to prev page.
		mPrev.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {

				return true;
			}
		});
	}

	// Called with the result of the other activity
	// requestCode was the origin request code send to the activity
	// resultCode is the return code, 0 is everything is ok
	// intend can be used to get data
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();

	}

	/**
	 * Here we fill the list with lines from the play.
	 * 
	 */
	private void fillData() {
		mFrom = new String[] { PlayDbAdapter.KEY_CHARACTER,
				PlayDbAdapter.KEY_LINE };
		mTo = new int[] { R.id.textCharacter, R.id.textLine };

		// Now create an array adapter and set it to display using our row
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.play_list_layout, mCursor, mFrom, mTo);

		setListAdapter(adapter);

	}

}
