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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.lines.activitys.SettingsActivity;

/**
 * Our test class for the Settings Activity
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestSettings extends
		ActivityInstrumentationTestCase2<SettingsActivity> {

	private Solo solo;
	private String script;
	private String prompts;
	private String autoplay;

	public TestSettings() {
		super("com.lines", SettingsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * Obtain the data stored in the Settings file
	 * 
	 * @throws IOException
	 */
	private void readSettingsFile() throws IOException {
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

		script = dis.readLine();
		prompts = dis.readLine();
		autoplay = dis.readLine();
	}

	/**
	 * This method ensures we select a random item in the Spinners that is not
	 * equal to the current selected item.
	 * 
	 * @throws InterruptedException
	 * 
	 */
	private void selectRandomItem() throws InterruptedException {
		int promptsPos = solo.getCurrentSpinners().get(2)
				.getSelectedItemPosition();

		// Select a random item in the Spinner that is not the current one
		// selected
		Random randomise = new Random();
		int index;
		do {
			index = randomise.nextInt(solo.getCurrentSpinners().get(2)
					.getCount());
		} while (index == promptsPos);

		solo.pressSpinnerItem(2, index);
		solo.goBackToActivity("SettingsActivity");

		int autoplayPos = solo.getCurrentSpinners().get(1)
				.getSelectedItemPosition();

		if (autoplayPos == 0) {
			solo.pressSpinnerItem(1, 1);
		} else {
			solo.pressSpinnerItem(1, 0);
		}

		solo.goBackToActivity("SettingsActivity");
	}

	/**
	 * Check whether the default values shown in the Spinners are the same as
	 * what is stored in the Settings file.
	 * 
	 * @throws IOException
	 * 
	 */
	public void testInitialValues() throws IOException {
		readSettingsFile();

		String scriptValue = solo.getCurrentSpinners().get(0).getSelectedItem()
				.toString();
		String promptValue = solo.getCurrentSpinners().get(2).getSelectedItem()
				.toString();
		String autoplayValue = solo.getCurrentSpinners().get(1)
				.getSelectedItem().toString();

		assertEquals(script, scriptValue);
		assertEquals(prompts, promptValue);
		assertEquals(autoplay, autoplayValue);
	}

	/**
	 * Test that the default values are shown correctly when the Restore to
	 * Default button is pressed.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public void testRestoreToDefault() throws IOException, InterruptedException {
		readSettingsFile();
		selectRandomItem();

		solo.clickOnButton("Restore to Default");

		String promptValue = solo.getCurrentSpinners().get(2).getSelectedItem()
				.toString();
		String autoplayValue = solo.getCurrentSpinners().get(1)
				.getSelectedItem().toString();

		assertEquals(prompts, promptValue);
		assertEquals(autoplay, autoplayValue);
	}

	/**
	 * Check that when the user makes some changes and saves, the Settings file
	 * is updated correctly.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public void testUpdatedValues() throws IOException, InterruptedException {
		selectRandomItem();

		String scriptValue = solo.getCurrentSpinners().get(0).getSelectedItem()
				.toString();
		String promptValue = solo.getCurrentSpinners().get(2).getSelectedItem()
				.toString();
		String autoplayValue = solo.getCurrentSpinners().get(1)
				.getSelectedItem().toString();

		solo.clickOnButton("Save Changes");

		readSettingsFile();

		assertEquals(script, scriptValue);
		assertEquals(prompts, promptValue);
		assertEquals(autoplay, autoplayValue);
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}
