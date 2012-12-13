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
	private ArrayList<Line> lines = new ArrayList<Line>();
	private static final int OPTIONS = 0;
	private static final int STATS = 1;
	private static final int QUICK_SEARCH = 2;
	private static final int ADD_NOTE = 0;
	private static final int VIEW_NOTES = 1;
	private static final int RECORD = 2;
	private static final int ADD_RECORD = 3;
	private static final int STRIKE = 4;
	private MediaPlayer player;
	private MediaRecorder recorder;
	private String OUTPUT_FILE;

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
				showRecordingDialog();
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
		menu.add(0, RECORD, 2, "Begin recording");
		menu.add(0, ADD_RECORD, 3, "Apply recording");
		menu.add(0, STRIKE, 4, "Strikeout text");
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
		case RECORD:
			return true;
		case ADD_RECORD:
			return true;
		case STRIKE:
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Here we reveal the appropriate word from the current line to the user.
	 * 
	 */
	private void revealWord() {
		Log.d(TAG, currentLine);
		String words[] = currentLine.split("\\s+");
		boolean note;
		int number;

		// Only obtain next word if there are any left
		if (visibleWords <= words.length) {
			String line = "";
			// Construct the new line revealing the correct amount of words
			for (int i = 0; i < visibleWords; i++) {
				line += words[i] + " ";
			}
			// When we create a new line, we want to keep the note value and the
			// line number the same
			note = lines.get(lines.size() - 1).getNote();
			number = lines.get(lines.size() - 1).getNumber();
			// Update the line we are working with
			lines.remove(lines.size() - 1);
			Line newLine = new Line(number, character, line, note);
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
				number = lines.get(i).getNumber();
				lines.remove(i);
				Line newLine = new Line(number, character, sb.toString(), note);
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
		boolean note;
		int number;
		int visibleLines = 0;

		Log.d(TAG, "No. of lines: " + Integer.toString(mCursor.getCount()));

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
				// int lineNo =
				// mCursor.getInt(mCursor.getColumnIndex("number"));
				// Log.d(TAG, "Current line No.: " + Integer.toString(lineNo));
				// Get current row's character and line
				currentChar = mCursor.getString(mCursor
						.getColumnIndex("character"));
				newLine = " ";
				// Get the current value of whether there is a performance note
				// or not
				getNote = mCursor.getString(mCursor.getColumnIndex("note"));
				number = mCursor.getInt(mCursor.getColumnIndex("number"));
				if (getNote.equals("Y")) {
					note = true;
				} else {
					note = false;
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
							// Before we exit, store the current line
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
				line = new Line(number, currentChar, newLine, note);
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
	 * Before we start the recording, we ask the user if they are ready.
	 * 
	 */
	private void showRecordingDialog() {
		new AlertDialog.Builder(this)
				// TODO: Maybe set a different icon here
				.setIcon(android.R.drawable.radiobutton_on_background)
				.setTitle("Record")
				.setMessage("When ready, press Ok to begin recording")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							beginRecording();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						}).show();
	}

	/**
	 * Here we record the user rehearsing their line(s) and display to them a
	 * timer showing time elapsed.
	 * 
	 * @throws Exception
	 */
	private void beginRecording() throws Exception {
		LayoutInflater li = LayoutInflater.from(this);
		View recordView = li.inflate(R.layout.record_layout, null);

		// Create temporary file to store audio recording
		// TODO: If user saves their file as "temp" then it will be overwritten.
		OUTPUT_FILE = Environment.getExternalStorageDirectory()
				+ "/learnyourlines/audio/temp.3gpp";
		ditchRecorder();
		final File temp = new File(OUTPUT_FILE);

		if (temp.exists()) {
			temp.delete();
		}

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(recordView);

		alertDialogBuilder.setCancelable(false).setPositiveButton(
				"Stop Recording", new DialogInterface.OnClickListener() {
					// Stop recording and move to next popup to name the audio
					// file
					public void onClick(DialogInterface dialog, int id) {
						recorder.stop();
						nameRecording(temp);
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

		// Setup and start recording
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(OUTPUT_FILE);
		recorder.prepare();
		recorder.start();

		Chronometer timer = (Chronometer) recordView.findViewById(R.id.chrono);
		final TextView text = (TextView) recordView.findViewById(R.id.textTime);

		// Update our textview displaying the timer elapsed each second
		timer.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer chrono) {
				String asText = chrono.getText().toString();
				text.setText(asText);
			}
		});
		timer.start();
	}

	/**
	 * Here the user can preview and save their recording
	 * 
	 * @param temp
	 *            - The newly created audio file
	 */
	private void nameRecording(final File temp) {
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
					playRecording(seekBar, preview, stop);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Stop recording
		// TODO: Need to find out how to stop CountDownTimer when this button is
		// pressed
		stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				preview.setVisibility(View.VISIBLE);
				stop.setVisibility(View.INVISIBLE);
				preview.setEnabled(true);
				stop.setEnabled(false);
				player.stop();
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
								// TODO: Need to make sure there are valid
								// filenames being created! Maybe theres some
								// library to handle this.
								// TODO: Also check filename doesn't already
								// exist
								if (textTitle.equals("")) {
									Toast.makeText(
											getApplicationContext(),
											"You must name the recording before saving!",
											Toast.LENGTH_LONG).show();
									dialog.cancel();
									nameRecording(temp);
									// Otherwise save to Note database
								} else {
									saveRecording(temp, textTitle);
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
	 * Playback the recording
	 * 
	 * @throws Exception
	 */
	private void playRecording(final SeekBar seekBar,
			final ImageButton preview, final ImageButton stop) throws Exception {
		ditchPlayer();
		player = new MediaPlayer();
		player.setDataSource(OUTPUT_FILE);
		player.prepare();
		player.start();

		seekBar.setMax(player.getDuration());

		// Increment the progress bar each second
		new CountDownTimer(player.getDuration(), 250) {
			public void onTick(long millisUntilFinished) {
				seekBar.setProgress(seekBar.getProgress() + 250);
			}

			public void onFinish() {
				preview.setVisibility(View.VISIBLE);
				stop.setVisibility(View.INVISIBLE);
				preview.setEnabled(true);
				stop.setEnabled(false);
				seekBar.setProgress(0);
			}
		}.start();
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
		temp.renameTo(newFile);

		Toast.makeText(getApplicationContext(), "New recording saved!",
				Toast.LENGTH_LONG).show();
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
