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

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.lines.activitys.OptionsActivity;

/**
 * Our test class for the Options Activity
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestOptions extends
		ActivityInstrumentationTestCase2<OptionsActivity> {

	private Solo solo;

	public TestOptions() {
		super("com.lines", OptionsActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * Test that the character selection Spinner is disabled while the mode
	 * Spinner is on "Normal".
	 * 
	 */
	public void testCharIsDisabled() {
		solo.pressSpinnerItem(3, 0);
		solo.goBackToActivity("OptionsActivity");
		assertFalse(solo.getCurrentSpinners().get(0).isEnabled());
	}

	/**
	 * Test that the character selection Spinner is enabled when the mode
	 * Spinner is on "Rehearsal".
	 * 
	 */
	public void testRehearsal() {
		solo.pressSpinnerItem(3, 1);
		solo.goBackToActivity("OptionsActivity");
		assertTrue(solo.getCurrentSpinners().get(0).isEnabled());
	}

	/**
	 * When we toggle the cue words checkbox on, check that the own lines
	 * checkbox is toggled off and disabled.
	 * 
	 */
	public void testToggleCueWords() {
		solo.clickOnCheckBox(0);
		solo.clickOnCheckBox(0);
		assertFalse(solo.getCurrentCheckBoxes().get(2).isChecked());
		assertFalse(solo.getCurrentCheckBoxes().get(2).isEnabled());
	}

	/**
	 * When we toggle the own lines checkbox to on, check that the cuew words
	 * checkbox is toggled off and disabled, and the character spinner becomes
	 * enabled.
	 * 
	 */
	public void testToggleOwnLines() {
		solo.clickOnCheckBox(0);
		solo.clickOnCheckBox(2);
		assertFalse(solo.getCurrentCheckBoxes().get(0).isChecked());
		assertFalse(solo.getCurrentCheckBoxes().get(0).isEnabled());
		assertTrue(solo.getCurrentSpinners().get(0).isEnabled());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}