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

import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;
import com.lines.R;
import com.lines.activitys.OptionsActivity;

/**
 * Our test class for the Options Activity
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestOptionsAndMain extends
		ActivityInstrumentationTestCase2<OptionsActivity> {

	private Solo solo;
	private int randomCharPos;
	private int randomActPos;
	private int randomPagePos;
	private String randomChar;
	private String randomAct;
	private String randomPage;
	private TextView views;
	private TextView prompts;
	private TextView completions;

	public TestOptionsAndMain() {
		super("com.lines", OptionsActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	// /**
	// * Test that the character selection Spinner is disabled while the mode
	// * Spinner is on "Normal".
	// *
	// */
	// public void testCharIsDisabled() {
	// solo.pressSpinnerItem(3, 0);
	// solo.goBackToActivity("OptionsActivity");
	// assertFalse(solo.getCurrentSpinners().get(0).isEnabled());
	// }
	//
	// /**
	// * Test that the character selection Spinner is enabled when the mode
	// * Spinner is on "Rehearsal".
	// *
	// */
	// public void testRehearsal() {
	// solo.pressSpinnerItem(3, 1);
	// solo.goBackToActivity("OptionsActivity");
	// assertTrue(solo.getCurrentSpinners().get(0).isEnabled());
	// }
	//
	// /**
	// * When we toggle the cue words checkbox on, check that the own lines
	// * checkbox is toggled off and disabled.
	// *
	// */
	// public void testToggleCueWords() {
	// solo.clickOnCheckBox(0);
	// solo.clickOnCheckBox(0);
	// assertFalse(solo.getCurrentCheckBoxes().get(2).isChecked());
	// assertFalse(solo.getCurrentCheckBoxes().get(2).isEnabled());
	// }
	//
	// /**
	// * When we toggle the own lines checkbox to on, check that the cuew words
	// * checkbox is toggled off and disabled, and the character spinner becomes
	// * enabled.
	// *
	// */
	// public void testToggleOwnLines() {
	// solo.clickOnCheckBox(0);
	// solo.clickOnCheckBox(2);
	// assertFalse(solo.getCurrentCheckBoxes().get(0).isChecked());
	// assertFalse(solo.getCurrentCheckBoxes().get(0).isEnabled());
	// assertTrue(solo.getCurrentSpinners().get(0).isEnabled());
	// }

	// /////////////// Tests for Main Activity ////////////////////////////////
	// // Need to start on Options Activity so we can first use the correct ///
	// //////////////////// configurations ////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Make some random selections and check the correct page and character is
	 * returned.
	 * 
	 */
	public void testZCorrectSelections() {
		solo.pressSpinnerItem(3, 1);
		solo.goBackToActivity("OptionsActivity");
		selectRandom();
		solo.clickOnText("Begin Rehearsing");
		assertTrue(solo.searchText(randomAct));
		assertTrue(solo.searchText(randomPage));

		int bottom = solo.getCurrentListViews().get(0).getChildCount();

		TextView character = (TextView) solo.getCurrentListViews().get(0)
				.getChildAt(bottom - 1).findViewById(R.id.textCharacter);

		TextView line = (TextView) solo.getCurrentListViews().get(0)
				.getChildAt(bottom - 1).findViewById(R.id.textLine);

		assertTrue(character.getText().toString()
				.equals(randomChar.toUpperCase()));
		assertTrue(line.getText().toString().equals(""));
	}

	/**
	 * Create a recording and check the file has been created.
	 * 
	 */
	public void testZRecordingCreation() {
		solo.pressSpinnerItem(3, 1);
		solo.clickOnText("Begin Rehearsing");
		solo.clickOnText("Rec");
		solo.clickOnText("Rec");
		solo.typeText(0, "testName");
		solo.clickOnText("Save");
		// If file exists already then overwrite it
		if (solo.searchButton("Yes")) {
			solo.clickOnButton("Yes");
		}
		solo.pressMenuItem(4);
		assertTrue(solo.searchText("testName"));
	}

	/**
	 * Create some controlled stats by playing around with the app, and then
	 * check the recorded values are as expected.
	 * 
	 */
	public void testZStats() {
		// Make sure we clear all stats before beginning test
		solo.pressSpinnerItem(3, 1);
		solo.goBackToActivity("OptionsActivity");
		solo.clickOnText("Begin Rehearsing");
		solo.pressMenuItem(2);
		solo.pressSpinnerItem(0, 0);
		solo.goBackToActivity("StatsActivity");
		solo.pressSpinnerItem(1, -1);
		solo.goBackToActivity("StatsActivity");
		solo.pressSpinnerItem(2, -1);
		solo.goBackToActivity("StatsActivity");
		solo.clickOnButton("Clear Stats");
		solo.clickOnButton("Yes");
		solo.waitForText("Stats cleared for the current selection.");
		solo.goBackToActivity("MainActivity");

		// Generate prompt stats (3 of them)
		solo.clickOnText("LINE!");
		solo.clickOnText("Next");
		solo.clickOnText("LINE!");
		solo.clickOnText("Next");
		solo.clickOnText("LINE!");
		solo.clickLongOnText("Next");

		// Generate completion stats (1 of them)
		for (int i = 0; i < 15; i++) {
			solo.clickOnText("Next");
		}

		// Finally create another viewed stat (2 in total)
		solo.clickLongOnText("Next");

		solo.pressMenuItem(2);
		solo.pressSpinnerItem(0, -5);
		solo.goBackToActivity("StatsActivity");
		solo.pressSpinnerItem(1, -5);
		solo.goBackToActivity("StatsActivity");
		solo.pressSpinnerItem(2, -5);
		solo.goBackToActivity("StatsActivity");

		views = (TextView) solo.getView(R.id.textViewsNum);
		prompts = (TextView) solo.getView(R.id.textPromptsNum);
		completions = (TextView) solo.getView(R.id.textCompleteNum);

		assertEquals(views.getText().toString(), "2");
		assertEquals(prompts.getText().toString(), "3");
		assertEquals(completions.getText().toString(), "1");
	}

	/**
	 * Select random items in the Spinners
	 * 
	 */
	private void selectRandom() {

		Random randomise;

		// Select random char from Spinner only if it is enabled
		if (solo.getCurrentSpinners().get(0).isEnabled()) {
			randomise = new Random();
			randomCharPos = randomise.nextInt(solo.getCurrentSpinners().get(1)
					.getCount());

			solo.pressSpinnerItem(0, randomCharPos);
			solo.goBackToActivity("OptionsActivity");

			randomChar = solo.getCurrentSpinners().get(0).getSelectedItem()
					.toString();
		}

		// Select random act from Spinner
		randomise = new Random();
		randomActPos = randomise.nextInt(solo.getCurrentSpinners().get(1)
				.getCount());

		solo.pressSpinnerItem(1, randomActPos);
		solo.goBackToActivity("OptionsActivity");

		randomAct = solo.getCurrentSpinners().get(1).getSelectedItem()
				.toString();

		// Select random page from Spinner
		randomise = new Random();
		randomPagePos = randomise.nextInt(solo.getCurrentSpinners().get(2)
				.getCount());

		solo.pressSpinnerItem(2, randomPagePos);
		solo.goBackToActivity("OptionsActivity");

		randomPage = solo.getCurrentSpinners().get(2).getSelectedItem()
				.toString();
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}