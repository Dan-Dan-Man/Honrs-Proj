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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.lines.R;
import com.lines.classes.Line;
import com.lines.classes.LineAdapter;
import com.lines.classes.LinesApp;
import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * The Main Screen where the user will rehearse their lines.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class MainActivity extends ListActivity {

	private TextView mPage;
	private TextView mAct;
	private TextView mTimeDisplay;
	private Button mRecStart;
	private Button mRecStop;
	private Button mNext;
	private Button mPrev;
	private Button mPrompt;
	private Button mStopPlayBack;
	private ImageButton mAudioStart;
	private ImageButton mAudioStop;
	private Chronometer mTimer;
	private Cursor mCursor;
	private PlayDbAdapter mDbAdapter;
	private NoteDbAdapter mNDbAdapter;
	private String pageNo;
	private String actNo;
	private String character;
	private String currentLine;
	private String promptsSettings;
	private String autoSettings;
	private boolean rehearsal;
	private boolean cue;
	private boolean random;
	private boolean ownLines;
	private boolean stage;
	private boolean promptUsed = false;
	private boolean promptLimitReached = false;
	private int visibleWords = 1;
	private int visibleSentences = 1;
	private int lastViewedPos;
	private int topOffset;
	private int hiddenLineNo;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private ArrayList<String> availablePages = new ArrayList<String>();
	private int playbackPosition = 0;
	private static final int OPTIONS = 0;
	private static final int STATS = 1;
	private static final int SETTINGS = 2;
	private static final int RECORDINGS = 3;
	private static final int NOTES = 4;
	private static final int ADD_NOTE = 0;
	private static final int VIEW_NOTES = 1;
	private static final int PLAY_RECORD = 2;
	private static final int ADD_RECORD = 3;
	private static final int REMOVE_RECORD = 4;
	private MediaPlayer player;
	private MediaRecorder recorder;
	private static final String TEMP_FILE = Environment
			.getExternalStorageDirectory() + "/learnyourlines/audio/-.3gpp";
	private static final Pattern VALID_CHARS = Pattern.compile("[^a-zA-Z0-9]");
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper viewFlipper;
	private File temp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		this.getListView().setDividerHeight(0);

		mNext = (Button) findViewById(R.id.buttonNext);
		mPrev = (Button) findViewById(R.id.buttonPrev);
		mPrompt = (Button) findViewById(R.id.buttonPrompt);
		mRecStart = (Button) findViewById(R.id.buttonRecStart);
		mRecStop = (Button) findViewById(R.id.buttonRecStop);
		mStopPlayBack = (Button) findViewById(R.id.buttonStopAudio);
		mAudioStart = (ImageButton) findViewById(R.id.imageAudioStart);
		mAudioStop = (ImageButton) findViewById(R.id.imageAudioStop);
		mAct = (TextView) findViewById(R.id.textAct);
		mPage = (TextView) findViewById(R.id.textPage);
		mTimeDisplay = (TextView) findViewById(R.id.textRecordingTimer);
		mTimer = (Chronometer) findViewById(R.id.chrono);

		// Set up animations for detecting finger movement
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out);

		// This listener allows us to switch page based on finger movement
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};

		// Retrieve the User's configurations from the Options Screen
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			pageNo = extras.getString("EXTRA_PAGE");
			actNo = extras.getString("EXTRA_ACT");
			character = extras.getString("EXTRA_CHAR");
			rehearsal = extras.getBoolean("EXTRA_MODE");
			cue = extras.getBoolean("EXTRA_CUE");
			random = extras.getBoolean("EXTRA_RANDOM");
			ownLines = extras.getBoolean("EXTRA_OWN");
			stage = extras.getBoolean("EXTRA_STAGE");
		}

		mPage.setText(pageNo);
		mAct.setText(actNo);

		LinesApp app = (LinesApp) this.getApplication();
		mDbAdapter = app.getPlayAdapter();
		mNDbAdapter = app.getNoteAdapter();

		getAvailablePages();

		startManagingCursor(mCursor);
		registerForContextMenu(getListView());

		// Update view count for the selected character
		if (rehearsal) {
			Cursor line = mDbAdapter.fetchCharacter(character, pageNo);
			line.moveToFirst();
			int lineNum = line.getInt(line.getColumnIndex("number"));
			int viewCount = line.getInt(line.getColumnIndex("views"));
			viewCount++;
			lineNum++;
			mDbAdapter.updateViews(lineNum, viewCount);
		}

		// When Next button is pressed, play jumps until user's next line.
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (rehearsal) {
					visibleWords = 1;
					visibleSentences = 1;
					promptLimitReached = false;
					setListViewPos();
					fillData("forward");
				}
			}
		});

		// When Next button is long pressed, play jumps to next page.
		mNext.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (Integer.parseInt(mPage.getText().toString()) < getLastPage()) {
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
				visibleSentences = 1;
				promptLimitReached = false;
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
					if (promptsSettings.equals("Whole Sentence")) {
						revealSentence();
					} else {
						int wordsToReveal = Integer.parseInt(promptsSettings);
						for (int i = 0; i < wordsToReveal; i++) {
							revealWord();
						}
					}
					// Increment the number of prompts used for the current
					// page
					int number = lines.get(lines.size() - 1).getNumber();
					Cursor promptLine = mDbAdapter.fetchLine(number + 1);
					int promptsCount = promptLine.getInt(promptLine
							.getColumnIndex("prompts"));
					promptsCount++;
					mDbAdapter.updatePrompts(number + 1, promptsCount);
				} else {
					Toast.makeText(MainActivity.this,
							"Feature only available in Rehearsal mode!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// If audio is playing, then this button becomes visible and we can stop
		// playback when we press it
		mStopPlayBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ditchPlayer();
				playbackPosition = lines.size() - 1;
				mStopPlayBack.setEnabled(false);
				mStopPlayBack.setVisibility(View.INVISIBLE);
			}
		});

		// When the Audio button is pressed, show a dialog to allow the user to
		// record themselves
		mAudioStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					beginRecording();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mRecStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					beginRecording();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// When the Audio button is pressed, show a dialog to allow the user to
		// record themselves
		mAudioStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					saveRecordingDialog(temp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mRecStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					saveRecordingDialog(temp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Override this method so that the viewFlipper and the ListView can work
	 * concurrently.
	 * 
	 * @param event
	 * @return
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * When we leave the screen, stop audio playback.
	 * 
	 */
	@Override
	protected void onPause() {
		super.onPause();
		ditchPlayer();
		ditchRecorder();
	}

	/**
	 * When we come back to this screen, set everything back to the way we left
	 * it.
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		String oldSettings = promptsSettings;
		// Reload our settings file, in case anything has changed
		try {
			loadSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Decide which data we need to retrieve from the database
		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}
		// If we're in rehearsal mode, then we need to make sure we have the
		// correct amount of lines revealed
		if (rehearsal) {
			fillData("forward");
			fillData("back");
			getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
			// If the user has changed the amount of words to be revealed in the
			// prompt, then reset the number of words visible for the current
			// line
			if (!promptsSettings.equals(oldSettings)) {
				visibleSentences = 1;
				visibleWords = 1;
				// Show the correct amount of revealed words if there are any
			} else if (promptsSettings.equals("Whole Sentence")) {
				if (visibleSentences > 1) {
					visibleSentences--;
					revealSentence();
				}
			} else {
				int wordsToReveal = Integer.parseInt(promptsSettings);
				if (visibleWords > wordsToReveal) {
					visibleWords = visibleWords - wordsToReveal;
					for (int i = 0; i < wordsToReveal; i++) {
						revealWord();
					}
				}
			}
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
		menu.add(0, SETTINGS, 2, "Settings");
		menu.add(0, RECORDINGS, 3, "Recordings");
		menu.add(0, NOTES, 4, "Notes");
		return true;
	}

	/**
	 * Here we program what each menu item does.
	 * 
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case (OPTIONS):
			finish();
			break;
		case (STATS):
			setListViewPos();
			i = new Intent(MainActivity.this, StatsActivity.class);
			i.putExtra("EXTRA_ACT", mAct.getText());
			i.putExtra("EXTRA_PAGE", mPage.getText());
			i.putExtra("EXTRA_CHARACTER", character);
			MainActivity.this.startActivity(i);
			break;
		case (SETTINGS):
			setListViewPos();
			i = new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivity(i);
			break;
		case (RECORDINGS):
			setListViewPos();
			i = new Intent(MainActivity.this, RecordingsActivity.class);
			MainActivity.this.startActivity(i);
			break;
		case (NOTES):
			setListViewPos();
			i = new Intent(MainActivity.this, NotesActivity.class);
			MainActivity.this.startActivity(i);
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
				playSelectedRecording(info.id, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		case ADD_RECORD:
			setListViewPos();
			Intent i = new Intent(MainActivity.this, RecordingsActivity.class);
			i.putExtra("EXTRA_NUM", Long.toString(getLineNumber(info.id)));
			MainActivity.this.startActivity(i);
			return true;
		case REMOVE_RECORD:
			removeRecording(info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Read the Settings file and store data into attributes
	 * 
	 * @throws IOException
	 * 
	 */
	private void loadSettings() throws IOException {
		File settings = new File(Environment.getExternalStorageDirectory()
				+ "/learnyourlines/.settings.sav");

		InputStream is;
		BufferedInputStream bis;
		DataInputStream dis;

		try {
			is = new FileInputStream(settings);
			bis = new BufferedInputStream(is);
			dis = new DataInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		dis.readLine();
		promptsSettings = dis.readLine();
		autoSettings = dis.readLine();
	}

	/**
	 * The method stores all the available pages in the script in a list
	 * 
	 */
	private void getAvailablePages() {
		ArrayList<String> temp = new ArrayList<String>();

		if (rehearsal || ownLines) {
			mCursor = mDbAdapter.fetchAllFilteredPages(character);
		} else {
			mCursor = mDbAdapter.fetchAllLines();
		}

		// First get the data from "page" column
		if (mCursor.moveToFirst()) {
			do {
				temp.add(mCursor.getString(mCursor.getColumnIndex("page")));
			} while (mCursor.moveToNext());
		}

		// Then we remove duplicates to get exact number of pages
		HashSet<String> h = new HashSet<String>();
		h.addAll(temp);
		temp.clear();
		temp.addAll(h);

		// Finally sort the page numbers
		int currentPage = findMin(temp);
		for (int i = 0; i < temp.size(); i++) {
			if (Integer.parseInt(temp.get(i)) == currentPage) {
				availablePages.add(temp.get(i));
				temp.remove(i);
				i = -1;
				if (temp.size() > 0) {
					currentPage = findMin(temp);
				}
			}
		}
	}

	/**
	 * Find minimum value in ArrayList
	 * 
	 * @param pages
	 *            - arraylist to search
	 * @return - minimum value in list
	 * 
	 */
	private int findMin(ArrayList<String> pages) {
		int min = Integer.MAX_VALUE;
		for (String page : pages) {
			if (Integer.parseInt(page) < min) {
				min = Integer.parseInt(page);
			}
		}
		return min;
	}

	/**
	 * If the user has chosen to autoplay audio files, then we loop through all
	 * the visible lines, and playback in sequence the audio that has been
	 * applied to each line.
	 * 
	 */
	private void findNextAudio() {
		for (int i = playbackPosition; i < lines.size(); i++) {
			Cursor line = mDbAdapter.fetchLine(getLineNumber(i));
			String audio = line.getString(line.getColumnIndex("audio"));

			if (hiddenLineNo == getLineNumber(i)) {
				break;
			}

			// If we find a line with an audio file, then update the latest
			// playbackPosition, and play the audio file
			if (!audio.equals("N")) {
				try {
					playbackPosition = i;
					mStopPlayBack.setEnabled(true);
					mStopPlayBack.setVisibility(View.VISIBLE);
					playSelectedRecording(playbackPosition, true);
					playbackPosition++;
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the file associated with the selected line and play it.
	 * 
	 * @param id
	 *            - the position of the selected item in the list
	 * @throws Exception
	 */
	private void playSelectedRecording(final long id, final boolean autoplay)
			throws Exception {
		if (audioApplied(id)) {
			// If the selected line is hidden, then we don't want to play the
			// audio until the line is revealed
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

				// When the audio file has stopped playing back, then we want to
				// find the next audio file that we want to play
				player.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						mStopPlayBack.setEnabled(false);
						mStopPlayBack.setVisibility(View.INVISIBLE);
						if (autoplay) {
							findNextAudio();
						}
					}
				});
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
		String words[] = currentLine.split("\\s+");
		boolean note;
		boolean audio;
		int number;
		promptUsed = true;

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
					R.layout.script_list_layout, lines);
			setListAdapter(adapter);

			this.setSelection(adapter.getCount());

			visibleWords++;
		} else if (!promptLimitReached) {
			Toast.makeText(MainActivity.this,
					"No more hidden words for current line!",
					Toast.LENGTH_SHORT).show();
			promptLimitReached = true;
		}
	}

	/**
	 * This method will reveal the appropriate sentence from the current line to
	 * the user.
	 * 
	 */
	private void revealSentence() {
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.UK);
		String line = "";
		int start;
		int end;
		boolean note;
		boolean audio;
		int number;
		promptUsed = true;

		iterator.setText(currentLine);

		start = iterator.first();
		for (int i = 0; i < visibleSentences; i++) {
			end = iterator.next();
			line += currentLine.substring(start, end);
			start = end;
			if (start == currentLine.length()) {
				Toast.makeText(MainActivity.this,
						"No more hidden words for current line!",
						Toast.LENGTH_SHORT).show();
				break;
			}

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
				R.layout.script_list_layout, lines);
		setListAdapter(adapter);

		this.setSelection(adapter.getCount());
		visibleSentences++;
	}

	/**
	 * Use this method to obtain the last page in the script. Need this for a
	 * limit to number of pages user can view.
	 * 
	 */
	private int getLastPage() {
		mCursor = mDbAdapter.fetchAllLines();
		String page = "";

		if (mCursor.moveToLast()) {
			page = (mCursor.getString(mCursor.getColumnIndex("page")));
		}

		return Integer.parseInt(page);
	}

	/**
	 * Before displaying the script, this method identifys the cue words and
	 * colours them to highlight to the user.
	 * 
	 */
	private void identifyCueWords() {
		if (lines.size() > 1) {

			HashMap<String, String> temp;

			// If we're in rehearsal mode, then we need to identify cue words
			// only for the hidden line
			if (rehearsal) {
				String firstLine[] = (lines.get(lines.size() - 2).getLine()
						.replaceAll("([a-z]+)[?:!.,;]*", "$1")).split("\\s+");
				temp = new HashMap<String, String>();

				String hiddenLine[] = (currentLine.replaceAll(
						"([a-z]+)[?:!.,;]*", "$1")).split("\\s+");

				// Only continue if we aren't at the bottom of the page
				if (!Arrays.equals(firstLine, hiddenLine)) {
					for (int j = 0; j < firstLine.length; j++) {
						temp.put(firstLine[j], firstLine[j]);
					}

					// Compare the first string with the hidden one to find
					// duplicate occurences of a word
					for (int j = 0; j < hiddenLine.length; j++) {
						if (temp.containsValue(hiddenLine[j])
								&& hiddenLine[j].length() > 4) {
							// Update the line to contain HTML to highlight word
							String newLine = lines
									.get(lines.size() - 2)
									.getLine()
									.replace(
											hiddenLine[j],
											"<font color=\"#A50000\"><b>"
													+ hiddenLine[j]
													+ "</b></font>");
							lines.get(lines.size() - 2).setLine(newLine);
						}
					}
				}
				// If we're in Normal mode, the we want to show cue words for
				// all lines shown on page
			} else {
				for (int i = 0; i < lines.size() - 2; i++) {
					// Get the line that is going to contain the cue words.
					// Remove all punctuation and split into words
					String firstLine[] = (lines.get(i).getLine().replaceAll(
							"([a-z]+)[?:!.,;]*", "$1")).split("\\s+");
					temp = new HashMap<String, String>();

					// Store words of firstLine into Hashmap
					for (int j = 0; j < firstLine.length; j++) {
						temp.put(firstLine[j], firstLine[j]);
					}

					// Again remove punctuation and split into words for the
					// succeeding line
					String secondLine[] = (lines.get(i + 1).getLine()
							.replaceAll("([a-z]+)[?:!.,;]*", "$1"))
							.split("\\s+");

					// Check all the words of the succeeding line, and find any
					// that
					// appear in the previous line
					for (int j = 0; j < secondLine.length; j++) {
						if (temp.containsValue(secondLine[j])
								&& secondLine[j].length() > 4) {
							String newLine = lines
									.get(i)
									.getLine()
									.replace(
											secondLine[j],
											"<font color=\"#A50000\"><b>"
													+ secondLine[j]
													+ "</b></font>");
							lines.get(i).setLine(newLine);
						}
					}
				}
			}
		}
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
			if (lines.get(i).getCharacter().equals("STAGE")) {
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

		// If cuewords are toggled on, then we need to identify and show the
		// cuewords for each line
		if (cue) {
			identifyCueWords();
		}

		// Finally show data in our custom listview
		LineAdapter adapter = new LineAdapter(this,
				R.layout.script_list_layout, lines);
		setListAdapter(adapter);

		this.getListView().setSelectionFromTop(lastViewedPos, topOffset);

		// If we're in rehearsal mode, then we want to position the listview at
		// the bottem.
		if (rehearsal && command.equals("forward")) {
			getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
		} else if (rehearsal && command.equals("back")) {
			getListView().smoothScrollBy(-1, 1000);
		}

		if (autoSettings.equals("Yes")) {
			findNextAudio();
		}
	}

	/**
	 * Switch Page either up or down
	 * 
	 * @param pgUp
	 *            - Tells us if are going to the next or previous page.
	 */
	private void switchPage(boolean pgUp) {
		int index = -1;
		int lineNum;

		mCursor = mDbAdapter.fetchPage(pageNo);
		lineNum = mCursor.getCount();

		// Get the index of the current page
		for (int i = 0; i < availablePages.size(); i++) {
			if (availablePages.get(i).equals(pageNo)) {
				index = i;
				break;
			}
		}

		if (random) {
			Random randomise = new Random();
			index = randomise.nextInt(availablePages.size());
		} else {
			// Find the next available page
			if (pgUp && index != -1 && index < availablePages.size() - 1) {
				index++;
			} else if (!pgUp && index != -1 && index > 0) {
				index--;
			} else {
				Toast.makeText(getApplicationContext(),
						"No more pages in script!", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		pageNo = availablePages.get(index);

		// Return new page from database
		if (ownLines) {
			mCursor = mDbAdapter.fetchCharacter(character, pageNo);
		} else {
			mCursor = mDbAdapter.fetchPage(pageNo);
		}

		String act = "";
		if (mCursor.moveToFirst()) {
			act = "Act " + (mCursor.getString(mCursor.getColumnIndex("act")));
		}

		mPage.setText(pageNo);
		mAct.setText(act);
		visibleWords = 1;
		visibleSentences = 1;
		playbackPosition = 0;
		lastViewedPos = 0;
		topOffset = 0;
		ditchPlayer();
		mStopPlayBack.setVisibility(View.INVISIBLE);

		// Update completions count
		if (rehearsal && !promptUsed && lines.size() == lineNum) {
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).getCharacter().equals(character)) {
					Cursor line = mDbAdapter
							.fetchLine(lines.get(i).getNumber() + 1);
					int completionsCount = line.getInt(line
							.getColumnIndex("completions"));
					completionsCount++;
					mDbAdapter.updateCompletions(lines.get(i).getNumber() + 1,
							completionsCount);
					break;
				}
			}
		}
		if (rehearsal) {
			lines = new ArrayList<Line>();
			fillData("forward");
			fillData("back");
			getListView().smoothScrollBy(5000, mCursor.getCount() * 1000);
			promptUsed = false;
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).getCharacter().equals(character)) {
					Cursor line = mDbAdapter
							.fetchLine(lines.get(i).getNumber() + 1);
					int viewCount = line.getInt(line.getColumnIndex("views"));
					viewCount++;
					mDbAdapter.updateViews(lines.get(i).getNumber() + 1,
							viewCount);
					break;
				}
			}
		} else {
			fillData("");
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

	/**
	 * Setup a new temporary file and begin recording the user.
	 * 
	 */
	private void beginRecording() {
		temp = new File(TEMP_FILE);

		ditchRecorder();

		// Setup and start recording
		try {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(TEMP_FILE);
			recorder.prepare();
			recorder.start();
			mTimer.setBase(SystemClock.elapsedRealtime());
			mTimer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mAudioStart.setVisibility(View.INVISIBLE);
		mRecStart.setVisibility(View.INVISIBLE);
		mAudioStop.setVisibility(View.VISIBLE);
		mRecStop.setVisibility(View.VISIBLE);
		mTimeDisplay.setVisibility(View.VISIBLE);

		// Update our textview displaying the timer elapsed each second
		mTimer.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer chrono) {
				String asText = chrono.getText().toString();
				mTimeDisplay.setText(asText);
			}
		});
	}

	/**
	 * Here the user can preview and save their recording
	 * 
	 * @param temp
	 *            - The newly created audio file
	 */
	private void saveRecordingDialog(final File temp) throws Exception {

		mAudioStart.setVisibility(View.VISIBLE);
		mRecStart.setVisibility(View.VISIBLE);
		mAudioStop.setVisibility(View.INVISIBLE);
		mRecStop.setVisibility(View.INVISIBLE);

		ditchRecorder();
		mTimer.stop();

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
		stop.setEnabled(false);

		seekBar.setMax(player.getDuration());

		// Increment the progress bar each second
		final CountDownTimer timer = new CountDownTimer(player.getDuration(),
				50) {
			public void onTick(long millisUntilFinished) {
				seekBar.setProgress(seekBar.getProgress() + 50);
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
									mTimeDisplay.setVisibility(View.INVISIBLE);
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
								mTimeDisplay.setVisibility(View.INVISIBLE);
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

	/**
	 * Class which detects finger movement, and switches page accordingly.
	 * 
	 * @author Daniel Muir, s0930256
	 * 
	 */
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideLeftIn);
					viewFlipper.setOutAnimation(slideLeftOut);
					switchPage(true);
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
					switchPage(false);
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}
}
