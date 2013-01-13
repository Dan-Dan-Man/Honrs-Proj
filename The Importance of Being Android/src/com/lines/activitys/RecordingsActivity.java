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
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;
import com.lines.classes.LinesApp;
import com.lines.classes.Recording;
import com.lines.classes.RecordingAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * This class displays the list of recordings the user has created
 * 
 * @author Dan
 * 
 */
public class RecordingsActivity extends ListActivity {

	private Button mDelete;
	private Button mSelect;
	private Button mBack;
	private ArrayList<Recording> audioFiles;
	private String lineNo;
	private MediaPlayer player;
	private static String DIRECTORY;
	private PlayDbAdapter mDbAdapter;
	private static final String TAG = "RecordingsActivity";
	private static final int PLAY_ID = Menu.FIRST;
	private static final int RENAME_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final Pattern VALID_CHARS = Pattern.compile("[^a-zA-Z0-9]");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_recordings_layout);
		this.getListView().setDividerHeight(0);

		LinesApp app = (LinesApp) this.getApplication();
		mDbAdapter = app.getPlayAdapter();

		mDelete = (Button) findViewById(R.id.buttonDelete);
		mSelect = (Button) findViewById(R.id.buttonSelect);
		mBack = (Button) findViewById(R.id.buttonBack);

		// Retrieve User choice from previous Activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			lineNo = extras.getString("EXTRA_NUM");
			Log.d(TAG, "Value passed: " + lineNo);
			mSelect.setEnabled(true);
			mSelect.setText("Select");
		}

		DIRECTORY = Environment.getExternalStorageDirectory()
				+ "/learnyourlines/audio/";

		try {
			populateList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		registerForContextMenu(getListView());

		mDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getUserConfirm(true, 0);
			}
		});

		mSelect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				applyRecording();
			}
		});

		mBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

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
		menu.add(0, PLAY_ID, 0, "Play");
		menu.add(0, RENAME_ID, 1, "Rename");
		menu.add(0, DELETE_ID, 2, "Delete");
	}

	/**
	 * Set Context items operations
	 * 
	 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case PLAY_ID:
			try {
				playRecording(info.id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case RENAME_ID:
			renameFile(info.id, "", true);
			return true;
		case DELETE_ID:
			getUserConfirm(false, info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * When an item in the list is clicked on, play the audio file.
	 * 
	 */
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			playRecording(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Populate the listview with the arraylist of saved audio files
	 * 
	 * @throws Exception
	 * 
	 */
	private void populateList() throws Exception {
		audioFiles = getAudioFiles();

		RecordingAdapter adapter = new RecordingAdapter(this,
				R.layout.note_row_layout, audioFiles);

		setListAdapter(adapter);
	}

	/**
	 * Populate an ArrayList of all the audio files saved by the user
	 * 
	 * @return - the list of audio files
	 * @throws Exception
	 */
	private ArrayList<Recording> getAudioFiles() throws Exception {
		audioFiles = new ArrayList<Recording>();
		Recording record;
		int duration;
		String file;

		File fDirectory = new File(DIRECTORY);

		// If the directory doesn't exist then we can just return
		if (!fDirectory.exists()) {
			return audioFiles;
		}

		File[] files = fDirectory.listFiles();

		// Again if there are no saved files, then just return
		if (files.length == 0) {
			return audioFiles;
		}

		player = new MediaPlayer();

		// Populate arraylist
		for (int i = 0; i < files.length; i++) {
			// Get the duration of the current audio file
			file = files[i].getName();
			player.reset();
			player.setDataSource(DIRECTORY + file);
			player.prepare();
			duration = player.getDuration();
			// Before we add to the list, remove the file extension
			file = file.replace(".3gpp", "");
			record = new Recording(file, duration);
			audioFiles.add(record);
		}

		ditchPlayer();

		return audioFiles;
	}

	/**
	 * Create dialog where the user can listen to their selected audio file
	 * 
	 * @param id
	 *            - the position of the selected item in the list
	 * @throws Exception
	 */
	private void playRecording(long id) throws Exception {

		final String dir = DIRECTORY + audioFiles.get((int) id).getName()
				+ ".3gpp";

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

		// Make our Edittext appear like a textview
		title.setFocusable(false);
		title.setLongClickable(false);
		title.setGravity(Gravity.CENTER);
		title.setKeyListener(null);
		title.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		title.setTextColor(Color.WHITE);
		title.setText(audioFiles.get((int) id).getName());

		// Setup and prepare MediaPlayer
		ditchPlayer();
		player = new MediaPlayer();
		player.setDataSource(dir);
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
					player.setDataSource(dir);
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

		alertDialogBuilder.setCancelable(false).setPositiveButton("Close",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
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
	 * Provide dialog for the user to rename their selected file
	 * 
	 * @param id
	 *            - position of the file in the listview
	 * @param title
	 *            - title of the selected file
	 * @param fresh
	 *            - true if dialog is created for first time. False when user
	 *            enters invalid filename and dialog reappears
	 */
	private void renameFile(long id, String title, boolean fresh) {
		final String oldFilename = audioFiles.get((int) id).getName();
		LayoutInflater li = LayoutInflater.from(this);
		View recordView = li.inflate(R.layout.save_recording_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(recordView);

		final EditText name = (EditText) recordView
				.findViewById(R.id.editTitle);

		final ImageButton preview = (ImageButton) recordView
				.findViewById(R.id.imagePreview);

		final SeekBar seekBar = (SeekBar) recordView
				.findViewById(R.id.seekBarAudio);

		final TextView text = (TextView) recordView
				.findViewById(R.id.textPreview);

		preview.setVisibility(View.GONE);
		seekBar.setVisibility(View.GONE);
		text.setVisibility(View.GONE);

		if (fresh) {
			name.setText(audioFiles.get((int) id).getName());
		} else {
			name.setText(title);
		}

		final long newId = id;

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String textTitle = name.getText().toString();
								// If textbox is blank, then recall
								// method and close the current one
								if (invalidFilename(textTitle)) {
									Toast.makeText(
											getApplicationContext(),
											"Invalid filename! Only letters and numbers are allowed!",
											Toast.LENGTH_LONG).show();
									dialog.cancel();
									renameFile(newId, textTitle, false);
									// Otherwise save to Note database
								} else {
									try {
										saveRecording(newId, textTitle,
												oldFilename);
									} catch (Exception e) {
										e.printStackTrace();
									}
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
	 * Rename the selected file with the new filename
	 * 
	 * @param filename
	 *            - new filename we are renaming to
	 * @param oldFilename
	 *            - the oldfilename
	 * @throws Exception
	 */
	private void saveRecording(long id, String filename, String oldFilename)
			throws Exception {

		File newFile = new File(DIRECTORY + filename + ".3gpp");
		File oldFile = new File(DIRECTORY + oldFilename + ".3gpp");

		if (newFile.exists()) {
			overwriteFile(id, filename, newFile, oldFile);
		} else {
			oldFile.renameTo(newFile);
			Toast.makeText(getApplicationContext(), "Recording renamed!",
					Toast.LENGTH_LONG).show();
			populateList();
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
	private void overwriteFile(long id, final String filename,
			final File newFile, final File temp) {

		final long newId = id;

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
								try {
									temp.renameTo(newFile);
									Toast.makeText(getApplicationContext(),
											"New recording saved!",
											Toast.LENGTH_LONG).show();
									populateList();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							renameFile(newId, filename, false);
							dialog.cancel();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).show();
	}

	/**
	 * Delete the audio file the user has selected
	 * 
	 * @param id
	 *            - the audio file to delete
	 * @throws Exception
	 */
	private void deleteRecording(boolean multiple, long id) throws Exception {
		String filename = audioFiles.get((int) id).getName();
		// Add the file extension back on
		filename = filename + ".3gpp";

		File file = new File(DIRECTORY + filename);

		// Delete the file
		if (file.exists()) {
			file.delete();
		}

		// After we delete the file, check if it had been associated with any
		// lines and update as necessary
		Cursor lines = mDbAdapter.fetchAllLines();
		// Move through every line
		if (lines.moveToFirst()) {
			do {
				// Get the value from the audio column
				String audio = lines.getString(lines.getColumnIndex("audio"));
				// If the value is equal to the selected file then update the
				// database
				if (audio.equals(DIRECTORY + filename)) {
					int num = lines.getInt(lines.getColumnIndex("number"));
					num++;
					mDbAdapter.updateAudio(num, "N");
				}
			} while (lines.moveToNext());
		}

		// If we are doing multiple deletions, then we don't want to re-populate
		// after every deletion
		if (!multiple) {
			populateList();
		}
	}

	/**
	 * When the user presses Delete, we loop through all the files, check which
	 * have been selected and delete the file.
	 * 
	 * @throws Exception
	 * 
	 */
	private void deleteRecordings() throws Exception {
		int length = this.getListView().getChildCount();

		// Loop through each row, check if an item has been checked and
		// delete the note from the database
		for (int i = 0; i < length; i++) {
			CheckBox currentCheck = (CheckBox) this.getListView().getChildAt(i)
					.findViewById(R.id.checkNote);
			if (currentCheck.isChecked()) {
				long id = this.getListView().getAdapter().getItemId(i);
				deleteRecording(true, id);
			}
		}
		populateList();
	}

	/**
	 * First check that exactly one item in the list has been selected and then
	 * update the play Db with the path of the selected file.
	 * 
	 */
	private void applyRecording() {
		int length = this.getListView().getChildCount();
		int numChecked = 0;
		int row = -1;

		// Obtain which of the files has been selected
		for (int i = 0; i < length; i++) {
			CheckBox currentCheck = (CheckBox) this.getListView().getChildAt(i)
					.findViewById(R.id.checkNote);
			if (currentCheck.isChecked()) {
				row = i;
				numChecked++;
			}
		}

		// Inform user if not exactly one is checked
		if (numChecked != 1) {
			Toast.makeText(getApplicationContext(),
					"You must select only one recording!", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Get the filename of the selected file
		String filename = audioFiles.get(row).getName();
		// Add the file extension back on
		filename = filename + ".3gpp";

		// Update the Play Db
		mDbAdapter.updateAudio(Long.valueOf(lineNo), DIRECTORY + filename);

		Toast.makeText(getApplicationContext(), "Recording set!",
				Toast.LENGTH_LONG).show();

		// Finally close the Activity
		finish();
	}

	/**
	 * Before we perform any deletions, get the user's confirmation
	 * 
	 * @param multiple
	 *            - Checks whether we are deleting multiple files, or just one
	 * @param id
	 *            - the position in the listview of the selected audio file
	 */
	private void getUserConfirm(final boolean multiple, long id) {

		// First check if no checkboxes have been checked. If none have then we
		// end it right here
		if (multiple) {
			if (checkSelection()) {
				return;
			}
		}

		final long newId = id;

		// Create Dialog to get user's confirmation
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Delete")
				.setMessage("The selected recording(s) will be deleted")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (multiple) {
							try {
								deleteRecordings();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							try {
								deleteRecording(false, newId);
							} catch (Exception e) {
								e.printStackTrace();
							}
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
	 * Check if no CheckBoxes have been checked, and inform user at least one
	 * must be before deletion
	 * 
	 * @return - true or false. True if none are checked
	 */
	private boolean checkSelection() {
		int length = this.getListView().getChildCount();
		boolean noneChecked = true;

		// First we want to check if none have been selected, and inform the
		// user they must first select one
		for (int i = 0; i < length; i++) {
			CheckBox currentCheck = (CheckBox) this.getListView().getChildAt(i)
					.findViewById(R.id.checkNote);
			if (currentCheck.isChecked()) {
				noneChecked = false;
				return noneChecked;
			}
		}

		// Inform user if none are checked
		if (noneChecked) {
			Toast.makeText(getApplicationContext(),
					"At least one item must be selected before deleting.",
					Toast.LENGTH_SHORT).show();
		}
		return noneChecked;
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
