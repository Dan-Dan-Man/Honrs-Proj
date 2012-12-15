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

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;
import com.lines.classes.Line;
import com.lines.classes.LineAdapter;
import com.lines.classes.LinesApp;
import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Main Screen where the user will rehearse their lines.
 * 
 * @author Dan
 * 
 */
// TODO: This class is rather large. Need to refactor a bit
// TODO: If in rehearsal mode, we cannot allow the user to play recordings for
// the hidden line!
public class MainActivity extends ListActivity {

	private static final String TAG = "MainActivity";
	private TextView mPage;
	private TextView mAct;
	private Button mNext;
	private Button mPrev;
	private Button mPrompt;
	private ImageButton mAudio;
	private Cursor mCursor;
	private PlayDbAdapter mDbAdapter;
	private NoteDbAdapter mNDbAdapter;
	private String pageNo;
	private String actNo;
	private String character;
	private String currentLine;
	private boolean rehearsal;
	private boolean cue;
	private boolean breakUp;
	private boolean ownLines;
	private boolean stage;
	private int pgNum;
	private int lastPage;
	private int visibleWords = 1;
	private int lastViewedPos;
	private int topOffset;
	private int hiddenLineNo;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private static final int OPTIONS = 0;
	private static final int STATS = 1;
	private static final int QUICK_SEARCH = 2;
	private static final int ADD_NOTE = 0;
	private static final int VIEW_NOTES = 1;
	private static final int PLAY_RECORD = 2;
	private static final int ADD_RECORD = 3;
	private static final int REMOVE_RECORD = 4;
	private static final int STRIKE = 5;
	private MediaPlayer player;
	private MediaRecorder recorder;
	private static final String TEMP_FILE = Environment
			.getExternalStorageDirectory() + "/learnyourlines/audio/-.3gpp";
	private static final Pattern VALID_CHARS = Pattern.compile("[^a-zA-Z0-9]");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		this.getListView().setDividerHeight(0);

		mNext = (Button) findViewById(R.id.buttonNext);
		mPrev = (Button) findViewById(R.id.buttonPrev);
		mPrompt = (Button) findViewById(R.id.buttonPrompt);
		mAudio = (ImageButton) findViewById(R.id.imageAudio);
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

		LinesApp app = (LinesApp) this.getApplication();
		mDbAdapter = app.getPlayAdapter();
		mNDbAdapter = app.getNoteAdapter();

		getLastPage();

		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}

		startManagingCursor(mCursor);
		fillData("");
		registerForContextMenu(getListView());

		// When Next button is pressed, play jumps until user's next line.
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (rehearsal) {
					visibleWords = 1;
					setListViewPos();
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
				visibleWords = 1;
				setListViewPos();
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

		// Reveal word from current line if prompt button is pressed.
		mPrompt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (rehearsal) {
					revealWord();
				} else {
					Toast.makeText(MainActivity.this,
							"Feature only available in Rehearsal mode!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mAudio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					showRecordingDialog();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}
		if (rehearsal) {
			fillData("forward");
			fillData("back");
			getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
		} else {
			fillData("");
		}
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
			setListViewPos();
			Intent i = new Intent(MainActivity.this, StatsActivity.class);
			i.putExtra("EXTRA_ACT", mAct.getText());
			i.putExtra("EXTRA_PAGE", mPage.getText());
			i.putExtra("EXTRA_CHARACTER", character);
			MainActivity.this.startActivity(i);
			break;
		case (QUICK_SEARCH):
			// TODO: Allow user to jump to any page in script
			break;
		}
		return false;
	}

	/**
	 * Initalise our Context Menu
	 * 
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Option");
		menu.add(0, ADD_NOTE, 0, "Add new note");
		menu.add(0, VIEW_NOTES, 1, "View available notes");
		menu.add(0, PLAY_RECORD, 2, "Play recording");
		menu.add(0, ADD_RECORD, 3, "Apply recording");
		menu.add(0, REMOVE_RECORD, 4, "Remove recording");
		menu.add(0, STRIKE, 5, "Strikeout text");
	}

	/**
	 * Program what each context item does.
	 * 
	 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case ADD_NOTE:
			newNote(info.id, "", "", true);
			return true;
		case VIEW_NOTES:
			showNotes(info.id);
			return true;
		case PLAY_RECORD:
			try {
				playSelectedRecording(info.id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case ADD_RECORD:
			setListViewPos();
			Intent i = new Intent(MainActivity.this, RecordingsActivity.class);
			Log.d(TAG,
					"LineNo being passed through: "
							+ Long.toString(getLineNumber(info.id)));
			i.putExtra("EXTRA_NUM", Long.toString(getLineNumber(info.id)));
			MainActivity.this.startActivity(i);
			return true;
		case REMOVE_RECORD:
			removeRecording(info.id);
			return true;
		case STRIKE:
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Get the file associated with the selected line and play it.
	 * 
	 * @param id
	 *            - the position of the selected item in the list
	 * @throws Exception
	 */
	private void playSelectedRecording(long id) throws Exception {
		if (audioApplied(id)) {
			// If the selected line is hidden, then we don't want to
			Log.d(TAG, "HiddenLineNo: " + hiddenLineNo);
			Log.d(TAG, "getLineNo: " + getLineNumber(id));
			if (rehearsal && (hiddenLineNo == getLineNumber(id))) {
				Toast.makeText(
						getApplicationContext(),
						"Cannot play recording while line is hidden. No cheating!",
						Toast.LENGTH_LONG).show();
			} else {
				Cursor line = mDbAdapter.fetchLine(getLineNumber(id));
				String filename = line.getString(line.getColumnIndex("audio"));

				ditchPlayer();
				player = new MediaPlayer();
				player.setDataSource(filename);
				player.prepare();
				player.start();
			}
		}
	}

	/**
	 * Update the Play Db to remove the filename of the audio file that is
	 * associated with this Line.
	 * 
	 * @param id
	 *            - the position of the selected line in the list
	 */
	private void removeRecording(long id) {
		if (audioApplied(id)) {
			mDbAdapter.updateAudio(getLineNumber(id), "N");
			if (ownLines) {
				mCursor = mDbAdapter.fetchCharacter(character, pageNo);
			} else {
				mCursor = mDbAdapter.fetchPage(pageNo);
			}
			setListViewPos();
			if (rehearsal) {
				fillData("forward");
				fillData("back");
				getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
			} else {
				fillData("");
			}
		}
	}

	/**
	 * Checks if there is an audio file that has been applied to the selected
	 * line
	 * 
	 * @param id
	 *            - the position of the selected line in the list
	 * @return - true if file found, false if not
	 */
	private boolean audioApplied(long id) {
		// Check database
		Cursor line = mDbAdapter.fetchLine(getLineNumber(id));

		String audio = line.getString(line.getColumnIndex("audio"));

		if (audio.equals("N")) {
			Toast.makeText(
					getApplicationContext(),
					"No audio file found! You must first record and apply an audio file.",
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	/**
	 * Here we reveal the appropriate word from the current line to the user.
	 * 
	 */
	private void revealWord() {
		Log.d(TAG, currentLine);
		String words[] = currentLine.split("\\s+");
		boolean note;
		boolean audio;
		int number;

		// Only obtain next word if there are any left
		if (visibleWords <= words.length) {
			String line = "";
			// Construct the new line revealing the correct amount of words
			for (int i = 0; i < visibleWords; i++) {
				line += words[i] + " ";
			}
			// When we create a new line, we want to keep the note value, audio
			// value and the line number the same
			note = lines.get(lines.size() - 1).getNote();
			audio = lines.get(lines.size() - 1).getAudio();
			number = lines.get(lines.size() - 1).getNumber();
			// Update the line we are working with
			lines.remove(lines.size() - 1);
			Line newLine = new Line(number, character, line, note, audio);
			lines.add(newLine);

			// Update the Listview
			LineAdapter adapter = new LineAdapter(this,
					R.layout.play_list_layout, lines);
			setListAdapter(adapter);

			this.setSelection(adapter.getCount());

			visibleWords++;
		} else {
			Toast.makeText(MainActivity.this,
					"No more hidden words for current line!",
					Toast.LENGTH_SHORT).show();
		}
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
		boolean note;
		int number;
		boolean audio;

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
				note = lines.get(i).getNote();
				audio = lines.get(i).getAudio();
				number = lines.get(i).getNumber();
				lines.remove(i);
				Line newLine = new Line(number, character, sb.toString(), note,
						audio);
				lines.add(i, newLine);
			}
		}
		return lines;
	}

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
	 * This method extracts the stage directions from the current line when the
	 * user wants to reveal a word and stage directions are toggled off.
	 * 
	 */
	private void filterStageFromLine() {
		// Create list of chars of the current line
		ArrayList<Character> lineArray = new ArrayList<Character>();
		for (int j = 0; j < currentLine.toCharArray().length; j++) {
			lineArray.add(currentLine.toCharArray()[j]);
		}
		// Loop through the list of chars and remove everything in
		// between '[' and ']'
		for (int j = 0; j < lineArray.size(); j++) {
			if (lineArray.get(j) == '[') {
				do {
					lineArray.remove(j);
				} while (lineArray.get(j) != ']');
				lineArray.remove(j);
				lineArray.remove(j);
			}
		}
		// Concatenate our chars list to one string
		StringBuilder sb = new StringBuilder();
		for (char s : lineArray) {
			sb.append(s);
		}

		currentLine = sb.toString();
	}

	/**
	 * Here we fill the list with lines from the play. Command tells us if we
	 * are to reveal next line, hide current line, or do nothing.
	 * 
	 * @param command
	 *            - if we are in rehearsal mode, then we need to decide to
	 *            reveal or hide words, based on whether the user clicks Next or
	 *            Prev.
	 */
	private void fillData(String command) {
		Line line;
		String currentChar;
		String newLine;
		String getNote;
		String getAudio;
		boolean note;
		boolean audio;
		int number;
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
				newLine = " ";
				// Get the current value of whether there is a performance note
				// or not
				getNote = mCursor.getString(mCursor.getColumnIndex("note"));
				getAudio = mCursor.getString(mCursor.getColumnIndex("audio"));
				number = mCursor.getInt(mCursor.getColumnIndex("number"));
				if (getNote.equals("Y")) {
					note = true;
				} else {
					note = false;
				}

				if (getAudio.equals("N")) {
					audio = false;
				} else {
					audio = true;
				}

				// If we're in rehearsal mode then we need to check which lines
				// to show/hide
				if (rehearsal) {
					if (currentChar.equals(character)) {
						// If we are revealing the next line, then we need to
						// keep a count of when to stop
						if (command.equals("forward") && visibleLines >= 1) {
							newLine = mCursor.getString(mCursor
									.getColumnIndex("line"));
							visibleLines--;
							// Similar for hiding current line, we need to keep
							// a count of when to stop
						} else if (command.equals("back") && visibleLines >= 3) {
							newLine = mCursor.getString(mCursor
									.getColumnIndex("line"));
							visibleLines--;
						} else {
							// TODO:
							// Before we exit, store the current line
							hiddenLineNo = mCursor.getInt(mCursor
									.getColumnIndex("number"));
							hiddenLineNo++;
							currentLine = mCursor.getString(mCursor
									.getColumnIndex("line"));
							if (!stage) {
								filterStageFromLine();
							}
							mCursor.moveToLast();
						}
					} else {
						// If we haven't reached the user's current line, then
						// store it for showing
						newLine = mCursor.getString(mCursor
								.getColumnIndex("line"));
					}
					// Since we're not in rehearsal mode, we just want all the
					// lines from the Cursor
				} else {
					newLine = mCursor.getString(mCursor.getColumnIndex("line"));
				}
				// Create new line and add it to ArrayList
				line = new Line(number, currentChar, newLine, note, audio);
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

		this.getListView().setSelectionFromTop(lastViewedPos, topOffset);

		// If we're in rehearsal mode, then we want to position the listview at
		// the bottem.
		if (rehearsal && command.equals("forward")) {
			getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
		} else if (rehearsal && command.equals("back")) {
			getListView().smoothScrollBy(-1, 1000);
		}

		// TODO: Bringing up the context menu takes two clicks. Its because we
		// finish with a smoothScrollBy, and we lose focus. Need to find out
		// what we are losing focus on
	}

	/**
	 * Switch Page either up or down
	 * 
	 * @param pgUp
	 *            - Tells us if are going to the next or previous page.
	 */
	private void switchPage(boolean pgUp) {
		boolean validPage = true;
		pgNum = Integer.parseInt(pageNo);

		// Decide if we want to increment or decrement pages
		if (pgUp) {
			pgNum++;
		} else if (!pgUp) {
			pgNum--;
		}

		pageNo = Integer.toString(pgNum);

		// Here we handle if the user has selected to display own lines
		if (ownLines || rehearsal) {
			validPage = true;
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
			// Here we make sure to filter out any empty pages (where the
			// character doesn't appear)
			while (mCursor.getCount() == 0 && pgNum < lastPage && pgNum > 1) {
				if (pgUp) {
					pgNum++;
				} else if (!pgUp) {
					pgNum--;
				}
				pageNo = Integer.toString(pgNum);
				mCursor = mDbAdapter.fetchCharacter(character, pageNo);
			}
			if (mCursor.getCount() == 0 || pgNum >= lastPage || pgNum <= 1) {
				validPage = false;
			}
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}
		// Only display the page if we are on a valid page
		if (validPage) {
			if (rehearsal) {
				mCursor = mDbAdapter.fetchPage(pageNo);
			}
			String act = "";
			if (mCursor.moveToFirst()) {
				act = "Act "
						+ (mCursor.getString(mCursor.getColumnIndex("act")));
			}
			mPage.setText(pageNo);
			mAct.setText(act);
			visibleWords = 1;
			lastViewedPos = 0;
			topOffset = 0;
			startManagingCursor(mCursor);
			lines = new ArrayList<Line>();
			if (rehearsal) {
				fillData("forward");
				fillData("back");
				getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
			} else {
				fillData("");
			}
		} else {
			Toast.makeText(MainActivity.this,
					"No more pages where this character appears.",
					Toast.LENGTH_SHORT).show();
			pageNo = (String) mPage.getText();
		}
	}

	/**
	 * This method goes to the NotesActivity and shows only the notes for the
	 * selected line.
	 * 
	 * @param id
	 *            - the selected item in the list
	 */
	private void showNotes(long id) {
		// First check if there are notes to view. If not then tell user they
		// must add note for current line first.
		Cursor notes = mNDbAdapter.fetchNotes(Long.toString(getLineNumber(id)));
		if (notes.getCount() > 0) {
			setListViewPos();
			Intent i = new Intent(MainActivity.this, NotesActivity.class);
			i.putExtra("EXTRA_NUM", Long.toString(getLineNumber(id)));
			MainActivity.this.startActivity(i);
		} else {
			Toast.makeText(getApplicationContext(),
					"No saved notes for this line.", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Store the current scroll y value in listview.
	 * 
	 */
	private void setListViewPos() {
		// get scroll position
		lastViewedPos = this.getListView().getFirstVisiblePosition();
		// get offset
		View v = this.getListView().getChildAt(0);
		topOffset = (v == null) ? 0 : v.getTop();
		Log.d(TAG, "lastViewedPos after = " + lastViewedPos);
		Log.d(TAG, "topOffset after = " + topOffset);
	}

	/**
	 * Get the line number of the current selected item in the list
	 * 
	 * @param id
	 *            - the list item the user selects
	 * @return - the line number
	 */
	private long getLineNumber(long id) {
		long lineNo;

		lineNo = lines.get((int) id).getNumber();
		lineNo++;

		return lineNo;
	}

	/**
	 * This method creates and shows a popup to the user when they are creating
	 * a new performance note.
	 * 
	 * @param id
	 *            - pass this through so we can obtain the overall line number
	 * @param defaultTitle
	 *            - if we are recalling this method cause the user made and
	 *            error, then display his previous title
	 * @param defaultNote
	 *            - if we are recalling this method cause the user made and
	 *            error, then display his previous note
	 * @param fresh
	 *            - denotes if we are calling this method for the first time for
	 *            the current note.
	 * 
	 */
	private void newNote(long id, String defaultTitle, String defaultNote,
			boolean fresh) {

		LayoutInflater li = LayoutInflater.from(this);
		View notesView = li.inflate(R.layout.add_note_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(notesView);

		final EditText title = (EditText) notesView
				.findViewById(R.id.editTitle);

		final EditText note = (EditText) notesView.findViewById(R.id.editNote);

		final long newId = id;

		// If we are opening a fresh performance note dialog, then display the
		// default title
		if (fresh) {
			defaultTitle = "Page " + mPage.getText();
			defaultTitle += " - Line " + Long.toString(id + 1);
			// Otherwise set the text the user has already entered
		} else {
			note.setText(defaultNote);
		}

		// Set default title
		title.setText(defaultTitle);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Check that both textboxes contain text and
								// then save.
								String textTitle = title.getText().toString();
								String noteTitle = note.getText().toString();
								// If either textboxes are blank, then recall
								// method and close the current one
								if (textTitle.equals("")
										|| noteTitle.equals("")) {
									Toast.makeText(
											getApplicationContext(),
											"Invalid Note! Both fields must contain some text!",
											Toast.LENGTH_LONG).show();
									dialog.cancel();
									newNote(newId, textTitle, noteTitle, false);
									// Otherwise save to Note database
								} else {
									saveNote(getLineNumber(newId), textTitle,
											noteTitle);
								}

							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	/**
	 * When the user decides to save their note, save to Note database and
	 * update our Play database
	 * 
	 * @param number
	 * @param title
	 * @param note
	 * 
	 */
	private void saveNote(long number, String title, String note) {
		mNDbAdapter.createNote((int) number, title, note);
		mDbAdapter.updateNotes(number, "Y");
		Toast.makeText(getApplicationContext(), "New performance note saved!",
				Toast.LENGTH_LONG).show();
		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}
		setListViewPos();
		if (rehearsal) {
			fillData("forward");
			fillData("back");
		} else {
			fillData("");
		}
	}

	// //////////// RECORDING METHODS //////////////////

	/**
	 * Here we record the user rehearsing their line(s) and display to them a
	 * timer showing time elapsed.
	 * 
	 * @throws Exception
	 */
	private void showRecordingDialog() throws Exception {
		// Create temporary file to store audio recording
		final File temp = new File(TEMP_FILE);

		LayoutInflater li = LayoutInflater.from(this);
		View recordView = li.inflate(R.layout.record_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(recordView);

		final Chronometer timer = (Chronometer) recordView
				.findViewById(R.id.chrono);
		final TextView text = (TextView) recordView.findViewById(R.id.textTime);
		final ImageButton startRecord = (ImageButton) recordView
				.findViewById(R.id.imageButtonRecordStart);
		final ImageButton stopRecord = (ImageButton) recordView
				.findViewById(R.id.imageButtonRecordStop);

		startRecord.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					// Update buttons
					startRecord.setEnabled(false);
					startRecord.setVisibility(View.INVISIBLE);
					stopRecord.setEnabled(true);
					stopRecord.setVisibility(View.VISIBLE);

					ditchRecorder();

					// Setup and start recording
					recorder = new MediaRecorder();
					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					recorder.setOutputFile(TEMP_FILE);
					recorder.prepare();
					recorder.start();
					timer.setBase(SystemClock.elapsedRealtime());
					timer.start();
				} catch (Exception e) {
					// TODO: Handle exception
				}
			}
		});

		stopRecord.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Update buttons
				stopRecord.setEnabled(false);
				stopRecord.setVisibility(View.INVISIBLE);
				startRecord.setEnabled(true);
				startRecord.setVisibility(View.VISIBLE);

				recorder.stop();
				timer.stop();
			}
		});

		// Update our textview displaying the timer elapsed each second
		timer.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer chrono) {
				String asText = chrono.getText().toString();
				text.setText(asText);
			}
		});

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save Recording",
						new DialogInterface.OnClickListener() {
							// Stop recording and move to next popup to name the
							// audio file
							public void onClick(DialogInterface dialog, int id) {
								// If the user hasn't created a temporary file,
								// then don't let them try and save
								if (!temp.exists()) {
									try {
										showRecordingDialog();
										Toast.makeText(
												getApplicationContext(),
												"You must create recording before saving!",
												Toast.LENGTH_LONG).show();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} else {
									if (stopRecord.isEnabled()) {
										recorder.stop();
									}
									try {
										saveRecordingDialog(temp);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Delete temporary file if user cancels
								temp.delete();
								if (stopRecord.isEnabled()) {
									recorder.stop();
								}
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	/**
	 * Here the user can preview and save their recording
	 * 
	 * @param temp
	 *            - The newly created audio file
	 */
	private void saveRecordingDialog(final File temp) throws Exception {
		LayoutInflater li = LayoutInflater.from(this);
		View recordView = li.inflate(R.layout.save_recording_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(recordView);

		final EditText title = (EditText) recordView
				.findViewById(R.id.editTitle);

		final ImageButton preview = (ImageButton) recordView
				.findViewById(R.id.imagePreview);

		final ImageButton stop = (ImageButton) recordView
				.findViewById(R.id.imageStop);

		final SeekBar seekBar = (SeekBar) recordView
				.findViewById(R.id.seekBarAudio);

		// Setup and prepare MediaPlayer
		ditchPlayer();
		player = new MediaPlayer();
		player.setDataSource(TEMP_FILE);
		player.prepare();

		seekBar.setMax(player.getDuration());

		// Increment the progress bar each second
		final CountDownTimer timer = new CountDownTimer(player.getDuration(),
				55) {
			public void onTick(long millisUntilFinished) {
				seekBar.setProgress(seekBar.getProgress() + 100);
			}

			public void onFinish() {
				preview.setVisibility(View.VISIBLE);
				stop.setVisibility(View.INVISIBLE);
				preview.setEnabled(true);
				stop.setEnabled(false);
				seekBar.setProgress(0);
			}
		};

		// Disable SeekBar from being touched
		seekBar.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// Play recording
		preview.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					preview.setVisibility(View.INVISIBLE);
					stop.setVisibility(View.VISIBLE);
					preview.setEnabled(false);
					stop.setEnabled(true);
					player.release();
					player = new MediaPlayer();
					player.setDataSource(TEMP_FILE);
					player.prepare();
					player.start();
					timer.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Stop recording
		stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				preview.setVisibility(View.VISIBLE);
				stop.setVisibility(View.INVISIBLE);
				preview.setEnabled(true);
				stop.setEnabled(false);
				player.stop();
				timer.cancel();
				seekBar.setProgress(0);
			}
		});

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String textTitle = title.getText().toString();
								// If textbox is blank, then recall
								// method and close the current one
								if (invalidFilename(textTitle)) {
									Toast.makeText(
											getApplicationContext(),
											"Invalid filename! Only letters and numbers are allowed!",
											Toast.LENGTH_LONG).show();
									dialog.cancel();
									if (stop.isEnabled()) {
										player.stop();
									}
									try {
										saveRecordingDialog(temp);
									} catch (Exception e) {
										e.printStackTrace();
									}
									// Otherwise save to Note database
								} else {
									if (stop.isEnabled()) {
										player.stop();
									}
									saveRecording(temp, textTitle);
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Delete temporary file if user cancels
								temp.delete();
								if (stop.isEnabled()) {
									player.stop();
								}
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	/**
	 * Finally rename the temporary audio file with the user's own filename
	 * 
	 * @param temp
	 *            - the audio file
	 * @param filename
	 *            - user's filename choice
	 */
	private void saveRecording(File temp, String filename) {
		String directory = Environment.getExternalStorageDirectory()
				+ "/learnyourlines/audio/";

		File newFile = new File(directory + filename + ".3gpp");

		if (newFile.exists()) {
			overwriteFile(filename, newFile, temp);
		} else {
			temp.renameTo(newFile);
			Toast.makeText(getApplicationContext(), "New recording saved!",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Checks the String for any invalid characters. Only alphanumeric
	 * characters are valid
	 * 
	 * @param filename
	 *            - the String we are checking
	 * @return - true if the String contains only alphanumeric chars. False
	 *         otherwise
	 */
	private boolean invalidFilename(String filename) {
		Log.d(TAG, "Valid: " + VALID_CHARS.matcher(filename).find());
		if (filename.equals("") || VALID_CHARS.matcher(filename).find()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the desired filename already exists, ask user if they wish to
	 * overwrite it.
	 * 
	 * @param filename
	 *            - the filename the user wishes to save
	 * @param newFile
	 *            - the File object of the user's filename
	 * @param temp
	 *            - the temporary file of the recording
	 */
	private void overwriteFile(String filename, final File newFile,
			final File temp) {

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Overwrite file?")
				.setMessage(
						"The file \""
								+ filename
								+ "\" already exists. Do you want to overwrite it?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								temp.renameTo(newFile);
								Toast.makeText(getApplicationContext(),
										"New recording saved!",
										Toast.LENGTH_LONG).show();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							saveRecordingDialog(temp);
							dialog.cancel();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).show();
	}

	/**
	 * Handle our MediaRecorder
	 * 
	 */
	private void ditchRecorder() {
		if (recorder != null) {
			recorder.release();
		}
	}

	/**
	 * Handle our MediaPlayer
	 * 
	 */
	private void ditchPlayer() {
		if (player != null) {
			player.release();
		}
	}
}
