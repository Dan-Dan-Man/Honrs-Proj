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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.lines.classes.Line;
import com.lines.classes.LineAdapter;
import com.lines.database.PlayDbAdapter;

/**
 * The Main Screen where the user will rehearse their lines.
 * 
 * @author Dan
 * 
 */

// TODO: Bottom of list is hidden behind buttons
public class MainActivity extends ListActivity {

	private static final String TAG = "MainActivity";
	private TextView mPage;
	private TextView mAct;
	private Button mNext;
	private Button mPrev;
	private Cursor mCursor;
	private PlayDbAdapter mDbAdapter;
	private String pageNo;
	private String actNo;
	private String character;
	private boolean rehearsal;
	private boolean cue;
	private boolean breakUp;
	private boolean ownLines;
	private boolean stage;
	private int pgNum;
	private int lastPage;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private static final int OPTIONS = 0;
	private static final int STATS = 1;
	private static final int QUICK_SEARCH = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		this.getListView().setDividerHeight(0);

		mNext = (Button) findViewById(R.id.buttonNext);
		mPrev = (Button) findViewById(R.id.buttonPrev);
		mAct = (TextView) findViewById(R.id.textAct);
		mPage = (TextView) findViewById(R.id.textPage);

		// Retrieve User choice from previous Activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			pageNo = extras.getString("EXTRA_PAGE");
			actNo = extras.getString("EXTRA_ACT");
			character = extras.getString("EXTRA_CHAR");
			rehearsal = extras.getBoolean("EXTRA_MODE");
			cue = extras.getBoolean("EXTRA_CUE");
			breakUp = extras.getBoolean("EXTRA_BREAKUP");
			ownLines = extras.getBoolean("EXTRA_OWN");
			stage = extras.getBoolean("EXTRA_STAGE");
		} else {
			Log.e(TAG, "Unable to pass user choice through");
		}

		mPage.setText(pageNo);
		mAct.setText(actNo);

		mDbAdapter = new PlayDbAdapter(this);
		mDbAdapter.open();
		getLastPage();

		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}

		startManagingCursor(mCursor);
		fillData("");
		registerForContextMenu(getListView());

		mDbAdapter.close();

		// When Next button is pressed, play jumps until user's next line.
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (rehearsal) {
					fillData("forward");
				}
			}
		});

		// When Next button is long pressed, play jumps to next page.
		mNext.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (Integer.parseInt(mPage.getText().toString()) < lastPage) {
					switchPage(true);
				} else {
					Toast.makeText(MainActivity.this,
							"No more pages available in script!",
							Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

		// When Prev button is pressed, play jumps until user's prev line.
		mPrev.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fillData("back");
			}
		});

		// When Prev button is long pressed, play jumps to prev page.
		mPrev.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (Integer.parseInt(mPage.getText().toString()) > 1) {
					switchPage(false);
				}
				return true;
			}
		});
	}

	/**
	 * Initialise our Menu
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTIONS, 0, "Options");
		menu.add(0, STATS, 1, "Statistics");
		menu.add(0, QUICK_SEARCH, 2, "Quick Search");
		return true;
	}

	/**
	 * Here we program what each menu item does.
	 * 
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (OPTIONS):
			finish();
			break;
		case (STATS):
			Intent i = new Intent(MainActivity.this, StatsActivity.class);
			i.putExtra("EXTRA_ACT", mAct.getText());
			i.putExtra("EXTRA_PAGE", mPage.getText());
			i.putExtra("EXTRA_CHARACTER", character);
			MainActivity.this.startActivity(i);
			mCursor.close();
			mDbAdapter.close();
			break;
		case (QUICK_SEARCH):
			break;
		}
		return false;
	}

	/**
	 * Use this method to obtain the last page in the script. Need this for a
	 * limit to number of pages user can view.
	 * 
	 */
	private void getLastPage() {
		mCursor = mDbAdapter.fetchAllLines();
		String page = "";

		if (mCursor.moveToLast()) {
			page = (mCursor.getString(mCursor.getColumnIndex("page")));
		}

		lastPage = Integer.parseInt(page);
	}

	/**
	 * This method filters out stage directions from the script
	 * 
	 * @param lines
	 * @return
	 */
	private ArrayList<Line> filterStage(ArrayList<Line> lines) {
		ArrayList<Character> lineArray;
		// If the character's name is "STAGE." then we remove the whole line
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).getCharacter().equals("STAGE.")) {
				lines.remove(i);
				i--;
				// Otherwise we need to check if the line contains a stage
				// direction, and remove the characters
			} else {
				// Create list of chars of the current line
				lineArray = new ArrayList<Character>();
				for (int j = 0; j < lines.get(i).getLine().toCharArray().length; j++) {
					lineArray.add(lines.get(i).getLine().toCharArray()[j]);
				}
				// Loop through the list of chars and remove everything in
				// between '[' and ']'
				for (int j = 0; j < lineArray.size(); j++) {
					if (lineArray.get(j) == '[') {
						do {
							lineArray.remove(j);
						} while (lineArray.get(j) != ']');
						lineArray.remove(j);
					}
				}
				// Concatenate our chars list to one string
				StringBuilder sb = new StringBuilder();
				for (char s : lineArray) {
					sb.append(s);
				}
				// Finally delete the old line and replace it with the new
				// filtered one
				String character = lines.get(i).getCharacter();
				lines.remove(i);
				Line newLine = new Line(character, sb.toString());
				lines.add(i, newLine);
			}
		}
		return lines;
	}

	// TODO: Find out exactly what this does. Don't think I need it
	// // Called with the result of the other activity
	// // requestCode was the origin request code send to the activity
	// // resultCode is the return code, 0 is everything is ok
	// // intend can be used to get data
	// @Override
	// protected void onActivityResult(int requestCode, int resultCode,
	// Intent intent) {
	// super.onActivityResult(requestCode, resultCode, intent);
	// fillData();
	//
	// }

	/**
	 * Here we fill the list with lines from the play. Command tells us if we
	 * are to reveal next line, hide current line, or do nothing.
	 * 
	 * @param command
	 */
	private void fillData(String command) {
		Line line;
		String currentChar;
		String currentLine;
		int visibleLines = 0;

		// Get the number of visible lines
		for (Line l : lines) {
			if (l.getCharacter().equals(character)) {
				visibleLines++;
			}
		}

		lines = new ArrayList<Line>();
		// Loop through each row in the Cursor
		if (mCursor.moveToFirst()) {
			do {
				// Get current row's character and line
				currentChar = mCursor.getString(mCursor
						.getColumnIndex("character"));
				currentLine = "\n\n\n\n\n";
				// If we're in rehearsal mode then we need to check which lines
				// to show/hide
				if (rehearsal) {
					if (currentChar.equals(character)) {
						// If we are revealing the next line, then we need to
						// keep a count of when to stop
						if (command.equals("forward") && visibleLines >= 1) {
							currentLine = mCursor.getString(mCursor
									.getColumnIndex("line"));
							visibleLines--;
							// Similar for hiding current line, we need to keep
							// a count of when to stop
						} else if (command.equals("back") && visibleLines >= 3) {
							currentLine = mCursor.getString(mCursor
									.getColumnIndex("line"));
							visibleLines--;
						} else {
							mCursor.moveToLast();
						}
					} else {
						// If we haven't reached the user's current line, then
						// store it for showing
						currentLine = mCursor.getString(mCursor
								.getColumnIndex("line"));
					}
					// Since we're not in rehearsal mode, we just want all the
					// lines from the Cursor
				} else {
					currentLine = mCursor.getString(mCursor
							.getColumnIndex("line"));
				}
				// Create new line and add it to ArrayList
				line = new Line(currentChar, currentLine);
				lines.add(line);
			} while (mCursor.moveToNext());
		}
		// If stage directions are toggled off, then we need to filter these out
		if (!stage) {
			filterStage(lines);
		}

		// Finally show data in our custom listview
		LineAdapter adapter = new LineAdapter(this, R.layout.play_list_layout,
				lines);
		setListAdapter(adapter);

		// If we're in rehearsal mode, then we want to position the listview at
		// the bottem.
		if (rehearsal) {
			this.setSelection(adapter.getCount());
		}
	}

	/**
	 * Switch Page either up or down
	 * 
	 * @param pgUp
	 */
	private void switchPage(boolean pgUp) {
		boolean valid = true;
		mDbAdapter.open();
		pgNum = Integer.parseInt(pageNo);

		// Decide if we want to increment or decrement pages
		if (pgUp) {
			pgNum++;
		} else if (!pgUp) {
			pgNum--;
		}

		pageNo = Integer.toString(pgNum);

		// Here we handle if the user has selected to display own lines
		// TODO: Works but is very slow when we reach page limits. Look to make
		// this more efficent. It needs two clicks because it finds the page its
		// currently on and treats it as a valid page.
		if (ownLines) {
			valid = true;
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
			// Here we make sure to filter out any empty pages (where the
			// character doesn't appear)
			while (mCursor.getCount() == 0 && pgNum < lastPage && pgNum > 1) {
				// valid = true;
				if (pgUp) {
					pgNum++;
				} else if (!pgUp) {
					pgNum--;
				}
				pageNo = Integer.toString(pgNum);
				mCursor = mDbAdapter.fetchCharacter(character, pageNo);
			}
			if (mCursor.getCount() == 0 || pgNum >= lastPage || pgNum <= 1) {
				valid = false;
			}
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}
		if (valid) {
			String act = "";
			if (mCursor.moveToFirst()) {
				act = "Act "
						+ (mCursor.getString(mCursor.getColumnIndex("act")));
			}
			mPage.setText(pageNo);
			mAct.setText(act);
			startManagingCursor(mCursor);
			lines = new ArrayList<Line>();
			fillData("");
		} else {
			Toast.makeText(MainActivity.this,
					"No more pages where this character appears.",
					Toast.LENGTH_SHORT).show();
		}
		mDbAdapter.close();
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

}
