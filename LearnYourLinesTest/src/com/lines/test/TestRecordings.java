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

package com.lines.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;
import com.lines.R;
import com.lines.activitys.RecordingsActivity;

/**
 * Our test class for the Recordings Activity.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestRecordings extends
		ActivityInstrumentationTestCase2<RecordingsActivity> {

	private Solo solo;
	private String oldName;
	private String oldFilename;
	private static final String DIRECTORY = Environment
			.getExternalStorageDirectory() + "/learnyourlines/audio/";

	public TestRecordings() {
		super("com.lines", RecordingsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * Rename an audio file to an accepted name, and check it is accepted.
	 * 
	 */
	public void testRenameRecording() {
		int items = getActivity().getListView().getCount();

		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
		} else {
			renameFile("testName");
			solo.waitForText("Recording renamed!");
			assertTrue(solo.searchText("testName"));
			cleanUp("testName");
		}
	}

	/**
	 * Rename an audio file to something with a number in it. Should be
	 * accepted.
	 * 
	 */
	public void testRenameRecordingNumber() {
		int items = getActivity().getListView().getCount();

		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
		} else {
			renameFile("testName2");
			solo.waitForText("Recording renamed!");
			assertTrue(solo.searchText("testName2"));
			cleanUp("testName2");
		}
	}

	/**
	 * Try to rename an audio file to something invalid. Only alpha-numeric
	 * characters are allowed.
	 * 
	 */
	public void testInvalidName() {
		int items = getActivity().getListView().getCount();

		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
		} else {
			renameFile("testName!");
			assertTrue(solo
					.searchText("Invalid filename! Only letters and numbers are allowed!"));
			solo.clickOnButton("Cancel");
			assertFalse(solo.searchText("testName!"));
		}
	}

	/**
	 * Delete a file and check that the filename no longer appears in the
	 * listview. Test will fail if the filename consists of a single character!
	 * 
	 * @throws IOException
	 * 
	 */
	public void testDeleteFile() throws IOException {
		int items = getActivity().getListView().getCount();

		if (items == 0) {
			assertTrue("No items in list, can't run test", true);
		} else {
			copyFile();
			solo.clickLongInList(0);
			solo.clickOnText("Delete");
			solo.clickOnButton("Ok");
			Log.e("", oldFilename);
			assertFalse(solo.searchText(oldFilename));

			oldName = oldFilename;
			cleanUp("temp");
		}
	}

	/**
	 * Before testing the deletion of a file, make a copy of it so we can
	 * restore the file.
	 * 
	 * @throws IOException
	 * 
	 */
	private void copyFile() throws IOException {
		TextView textview = (TextView) getActivity().getListView()
				.getChildAt(0).findViewById(R.id.textTitle);
		oldFilename = textview.getText().toString();
		String tempDir = DIRECTORY + "temp" + ".3gpp";
		String oldDir = DIRECTORY + oldFilename + ".3gpp";
		File temp = new File(tempDir);
		File oldFile = new File(oldDir);

		temp.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;

		// Transfer data from the selected file to the newly created one
		try {
			source = new FileInputStream(oldFile).getChannel();
			destination = new FileOutputStream(temp).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Select the first item in the list and rename it
	 * 
	 * @param testFilename
	 *            - what we are renaming the file to.
	 */
	private void renameFile(String testFilename) {
		solo.clickLongInList(0);
		solo.clickOnText("Rename");
		oldName = solo.getEditText(0).getText().toString();
		solo.clearEditText(0);
		solo.typeText(0, testFilename);
		solo.clickOnButton("Save");
	}

	/**
	 * After we are done, reset the changed filename back to its original name.
	 * 
	 * @param testFilename
	 *            - the new file name we need to reset,
	 */
	private void cleanUp(String testFilename) {
		solo.clickLongOnText(testFilename);
		solo.clickOnText("Rename");
		solo.clearEditText(0);
		solo.typeText(0, oldName);
		solo.clickOnButton("Save");
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}
