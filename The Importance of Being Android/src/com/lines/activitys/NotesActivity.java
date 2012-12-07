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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lines.R;
import com.lines.classes.LinesApp;
import com.lines.classes.NoteAdapter;
import com.lines.database.notes.NoteDbAdapter;
import com.lines.database.play.PlayDbAdapter;

/**
 * This activity displays the list of performance notes the user has created.
 * 
 * @author Dan
 * 
 */
// TODO: Order of notes is messed up if we delete a note then create a new one.
public class NotesActivity extends ListActivity {

	private static final String TAG = "NotesActivity";
	private NoteDbAdapter mNDbAdapter;
	private PlayDbAdapter mDbAdapter;
	private Cursor mCursor;
	private ArrayList<String> notes;
	private static final int DELETE_ID = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_notes_layout);
		this.getListView().setDividerHeight(0);

		LinesApp app = (LinesApp) this.getApplication();
		mDbAdapter = app.getPlayAdapter();
		mNDbAdapter = app.getNoteAdapter();

		mCursor = mNDbAdapter.fetchAllNotes();

		Log.d(TAG, "No. of notes: " + mCursor.getCount());

		startManagingCursor(mCursor);
		fillData();
		registerForContextMenu(getListView());
	}

	/**
	 * Initalise our Context Menu
	 * 
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, "Delete");
	}

	/**
	 * Delete entry when Delete is selected in the context menu
	 * 
	 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case DELETE_ID:
			deleteNote(info);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * When an item in the list is clicked on, expand the note for the user.
	 * 
	 */
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		viewNote(id, "", "", true);

	}

	/**
	 * Delete the selected note from the database.
	 * 
	 * @param info
	 *            - use this to delete the correct row
	 */
	// TODO: Not working correctly
	private void deleteNote(AdapterContextMenuInfo info) {
		long id = info.id + 1;
		mNDbAdapter.deleteNote(id);
		// TODO: Update play database if there are no notes left for a line.
	}

	/**
	 * Fill the list with the items in the Note database.
	 * 
	 */
	private void fillData() {
		String note;
		notes = new ArrayList<String>();

		// Populate arraylist with database data
		if (mCursor.moveToFirst()) {
			do {
				// Get current row's character and line
				note = mCursor.getString(mCursor.getColumnIndex("title"));
				notes.add(note);
			} while (mCursor.moveToNext());
		}

		// Fill list with our custom adapter
		NoteAdapter adapter = new NoteAdapter(this, R.layout.note_row_layout,
				notes);
		setListAdapter(adapter);
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
	private void viewNote(long id, String defaultTitle, String defaultNote,
			boolean fresh) {

		final int lineNumber;
		String title;
		String note;

		id++;

		final long newId = id;

		Log.d(TAG, Long.toString(newId));

		mCursor = mNDbAdapter.fetchNote(newId);

		lineNumber = (mCursor.getInt(mCursor.getColumnIndex("number")));
		title = (mCursor.getString(mCursor.getColumnIndex("title")));
		note = (mCursor.getString(mCursor.getColumnIndex("note")));

		// Create alert dialog to display current Note
		LayoutInflater li = LayoutInflater.from(this);
		View notesView = li.inflate(R.layout.add_note_layout, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setView(notesView);

		final EditText editTitle = (EditText) notesView
				.findViewById(R.id.editTitle);

		final EditText editNote = (EditText) notesView
				.findViewById(R.id.editNote);

		// If we are opening a fresh performance note dialog, then display the
		// saved title and note
		if (fresh) {
			// Set default title
			editTitle.setText(title);
			editNote.setText(note);
			// Otherwise set the text the user has already entered
		} else {
			editTitle.setText(defaultTitle);
			editNote.setText(defaultNote);
		}

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Check that both textboxes contain text and
								// then save.
								String textTitle = editTitle.getText()
										.toString();
								String noteTitle = editNote.getText()
										.toString();
								// If either textboxes are blank, then recall
								// method and close the current one
								if (textTitle.equals("")
										|| noteTitle.equals("")) {
									Toast.makeText(
											getApplicationContext(),
											"Invalid Note! Both fields must contain some text!",
											Toast.LENGTH_LONG).show();
									dialog.cancel();
									long oldId = newId - 1;
									viewNote(oldId, textTitle, noteTitle, false);
									// Otherwise save to Note database
								} else {
									// saveNote(lineNumber, textTitle,
									// noteTitle);
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
	 * When the user decides to save their note, save to a new database.
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
	}

}
