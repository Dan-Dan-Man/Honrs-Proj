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

import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.lines.activitys.HomeActivity;

/**
 * Our test class for the Home Activity
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestHome extends ActivityInstrumentationTestCase2<HomeActivity> {

	private Solo solo;

	public TestHome() {
		super("com.lines", HomeActivity.class);
	}

	/**
	 * Select a random item in the list and check we have gone to the expected
	 * activity.
	 * 
	 */
	public void testRandomScreen() {
		solo.getCurrentImageButtons();

		// Generate a random number of presses for down arrow so we are moving
		// to a random screen
		Random randomise = new Random();
		int presses = randomise.nextInt(5);

		for (int i = 0; i < presses; i++) {
			solo.clickOnImage(2);
		}

		// Click on the text that is currently selected
		String selection = solo.getText(1).getText().toString();
		solo.clickOnText(selection);

		// Get the expected Activity we should be on depending on which item in
		// the list we selected
		if (selection.equals("Start")) {
			selection = "OptionsActivity";
		} else if (selection.equals("Statistics")) {
			selection = "StatsActivity";
		} else if (selection.equals("Performance Notes")) {
			selection = "NotesActivity";
		} else if (selection.equals("Recordings")) {
			selection = "RecordingsActivity";
		} else if (selection.equals("Settings")) {
			selection = "SettingsActivity";
		}

		solo.assertCurrentActivity("", selection);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}
