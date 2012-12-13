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
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;
import com.lines.classes.RecordingAdapter;

/**
 * This class displays the list of recordings the user has created
 * 
 * @author Dan
 * 
 */
public class RecordingsActivity extends ListActivity {

	private Button mDelete;
	private Button mBack;
	ArrayList<String> audioFiles;
	private static final String DIRECTORY = Environment
			.getExternalStorageDirectory() + "/learnyourlines/audio/";
	private static final String TAG = "RecordingsActivity";
	private static final int PLAY_ID = Menu.FIRST;
	private static final int RENAME_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: Could use notes layout, wait and see
		setContentView(R.layout.view_recordings_layout);
		this.getListView().setDividerHeight(0);

		mDelete = (Button) findViewById(R.id.buttonDelete);
		mBack = (Button) findViewById(R.id.buttonBack);

		populateList();
		registerForContextMenu(getListView());

		mDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getUserConfirm(true, 0);
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
	 * Populate the listview with the arraylist of saved audio files
	 * 
	 */
	// TODO: Also populate list with the duration of each audio file (maybe)
	private void populateList() {
		audioFiles = getAudioFiles();

		RecordingAdapter adapter = new RecordingAdapter(this,
				R.layout.recording_row_layout, audioFiles);

		setListAdapter(adapter);
	}

	/**
	 * Populate an ArrayList of all the audio files saved by the user
	 * 
	 * @return - the list of audio files
	 */
	private ArrayList<String> getAudioFiles() {
		audioFiles = new ArrayList<String>();
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

		// Populate arraylist
		for (int i = 0; i < files.length; i++) {
			// Before we add to the list, remove the file extension
			String file = files[i].getName();
			file = file.replace(".3gpp", "");
			audioFiles.add(file);
		}

		return audioFiles;
	}

	private void renameFile(long id, String title, boolean fresh) {
		final String oldFilename = audioFiles.get((int) id);
		LayoutInflater li = LayoutInflater.from(this);
		View recordView = li.inflate(R.layout.save_recording_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(recordView);

		final EditText name = (EditText) recordView
				.findViewById(R.id.editTitle);

		final ImageButton preview = (ImageButton) recordView
				.findViewById(R.id.imagePreview);

		// final ImageButton stop = (ImageButton) recordView
		// .findViewById(R.id.imageStop);

		final SeekBar seekBar = (SeekBar) recordView
				.findViewById(R.id.seekBarAudio);

		final TextView text = (TextView) recordView
				.findViewById(R.id.textPreview);

		preview.setVisibility(View.GONE);
		seekBar.setVisibility(View.GONE);
		text.setVisibility(View.GONE);
		// stop.setVisibility(View.GONE);

		if (fresh) {
			name.setText(audioFiles.get((int) id));
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
									// TODO: We delete what user entered. Do we
									// want this?
									renameFile(newId, textTitle, false);
									// Otherwise save to Note database
								} else {
									saveRecording(textTitle, oldFilename);
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

	private void saveRecording(String filename, String oldFilename) {

		File newFile = new File(DIRECTORY + filename + ".3gpp");
		File oldFile = new File(DIRECTORY + oldFilename + ".3gpp");
		oldFile.renameTo(newFile);

		Toast.makeText(getApplicationContext(), "Recording name changed!",
				Toast.LENGTH_LONG).show();

		populateList();
	}

	/**
	 * Delete the audio file the user has selected
	 * 
	 * @param id
	 *            - the audio file to delete
	 */
	private void deleteRecording(boolean multiple, long id) {
		String filename = audioFiles.get((int) id);
		// Add the file extension back on
		filename = filename + ".3gpp";

		File file = new File(DIRECTORY + filename);

		// Delete the file
		if (file.exists()) {
			file.delete();
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
	 */
	private void deleteRecordings() {
		int length = this.getListView().getChildCount();

		// Loop through each row, check if an item has been checked and
		// delete the note from the database
		for (int i = 0; i < length; i++) {
			CheckBox currentCheck = (CheckBox) this.getListView().getChildAt(i)
					.findViewById(R.id.checkRecord);
			if (currentCheck.isChecked()) {
				long id = this.getListView().getAdapter().getItemId(i);
				deleteRecording(true, id);
			}
		}
		populateList();
	}

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
							deleteRecordings();
						} else {
							deleteRecording(false, newId);
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
					.findViewById(R.id.checkRecord);
			if (currentCheck.isChecked()) {
				noneChecked = false;
				break;
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
}
