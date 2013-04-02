/*
 * Copyright (C) 2013 The Android Open Source Project 
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

package com.lines.test;

import java.io.IOException;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;
import com.lines.R;
import com.lines.activitys.NotesActivity;
import com.lines.database.notes.NoteDbAdapter;

/**
 * Our test class for the Notes Activity.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestNotes extends ActivityInstrumentationTestCase2<NotesActivity> {

	private Solo solo;
	private String oldName;
	private Context context;
	private NoteDbAdapter mDbAdapter;

	public TestNotes() {
		super("com.lines", NotesActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * Rename the title of the note and check it is correctly updated.
	 * 
	 */
	public void testRenameNoteTitle() {
		int items = getActivity().getListView().getCount();
		// If no notes available, then end the test and create one for the next
		// test
		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
			createNote();
		} else {
			renameNote("testName", 0);
			solo.waitForText("Performance Note updated!");
			assertTrue(solo.searchText("testName"));
			cleanUp("testName", 0);
		}
	}

	/**
	 * Rename the note of the note and check it is correctly updated.
	 * 
	 */
	public void testRenameNote() {
		int items = getActivity().getListView().getCount();
		// If no notes available, then end the test and create one for the next
		// test
		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
			createNote();
		} else {
			renameNote("testName", 1);
			solo.waitForText("Performance Note updated!");
			assertTrue(solo.searchText("testName"));
			cleanUp("testName", 1);
		}
	}

	/**
	 * Delete the note and check it no longer exists
	 * 
	 * @throws IOException
	 */
	public void testDeleteNote() throws IOException {
		int items = getActivity().getListView().getCount();
		// If no notes available, then end the test and create one for the next
		// test
		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
			createNote();
		} else {
			// Get the name of the note we are about to delete
			TextView textview = (TextView) getActivity().getListView()
					.getChildAt(0).findViewById(R.id.textTitle);
			oldName = textview.getText().toString();
			solo.clickLongInList(0);
			solo.clickOnText("Delete");
			solo.clickOnButton("Ok");
			// Check that the old note doesn't exist
			assertFalse(solo.searchText(oldName));
			createNote();
		}
	}

	/**
	 * Rename note with the chosen text.
	 * 
	 * @param testFilename
	 *            - text to rename to
	 * @param index
	 *            - determines if it's the title or note we are changing
	 */
	private void renameNote(String testFilename, int index) {
		solo.clickInList(0);
		oldName = solo.getEditText(index).getText().toString();
		solo.clearEditText(index);
		solo.typeText(index, testFilename);
		solo.clickOnButton("Save");
	}

	/**
	 * Reset the note to what it was before any changes were made
	 * 
	 * @param testFilename
	 *            - text to change back to
	 * @param index
	 *            - determines if it's the title or the note we are changing
	 */
	private void cleanUp(String testFilename, int index) {
		solo.clickOnText(testFilename);
		solo.clearEditText(index);
		solo.typeText(index, oldName);
		solo.clickOnButton("Save");
	}

	/**
	 * If no note exists, create one.
	 * 
	 */
	private void createNote() {
		context = getActivity().getApplicationContext();
		mDbAdapter = new NoteDbAdapter(context);
		mDbAdapter.open();
		mDbAdapter.createNote(50, "title", "note");
		mDbAdapter.close();
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}
