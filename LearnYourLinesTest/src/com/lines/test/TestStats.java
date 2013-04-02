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

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;
import com.lines.R;
import com.lines.activitys.StatsActivity;
import com.lines.database.play.PlayDbAdapter;

/**
 * Our test class for the Statistics Activity
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class TestStats extends ActivityInstrumentationTestCase2<StatsActivity> {

	private Solo solo;
	private TextView views;
	private TextView prompts;
	private TextView completions;
	private PlayDbAdapter mDbAdapter;

	public TestStats() {
		super("com.lines", StatsActivity.class);
	}

	private void createStats() {
		Context context = getActivity().getApplicationContext();
		mDbAdapter = new PlayDbAdapter(context);
		mDbAdapter.open();
		mDbAdapter.updateViews(8, 5);
		mDbAdapter.updateViews(7, 3);
		mDbAdapter.updatePrompts(7, 2);
		mDbAdapter.updateCompletions(8, 1);
		mDbAdapter.close();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		views = (TextView) solo.getView(R.id.textViewsNum);
		prompts = (TextView) solo.getView(R.id.textPromptsNum);
		completions = (TextView) solo.getView(R.id.textCompleteNum);
	}

	/**
	 * Check that when we select a different set of items in Spinners, the stats
	 * are updated based on selection.
	 * 
	 */
	public void testASelectionChange() {

		createStats();

		String viewsBefore = views.getText().toString();
		String promptsBefore = prompts.getText().toString();
		String completionsBefore = completions.getText().toString();

		solo.pressSpinnerItem(0, 4);
		solo.pressSpinnerItem(1, 2);
		solo.pressSpinnerItem(2, 5);

		String viewsAfter = views.getText().toString();
		String promptsAfter = prompts.getText().toString();
		String completionsAfter = completions.getText().toString();

		assertNotSame(viewsBefore, viewsAfter);
		assertNotSame(promptsBefore, promptsAfter);
		assertNotSame(completionsBefore, completionsAfter);
	}

	/**
	 * Cancel the decision to reset stats and check the values have not changed.
	 * 
	 */
	public void testClearStatsCancel() {
		String viewsBefore = views.getText().toString();
		String promptsBefore = prompts.getText().toString();
		String completionsBefore = completions.getText().toString();

		solo.clickOnButton("Clear Stats");
		solo.clickOnButton("No");

		assertEquals(viewsBefore, views.getText().toString());
		assertEquals(promptsBefore, prompts.getText().toString());
		assertEquals(completionsBefore, completions.getText().toString());
	}

	/**
	 * Clear the stats and check the values have been reset.
	 * 
	 */
	public void testClearStatsConfirm() {
		solo.clickOnButton("Clear Stats");
		solo.clickOnButton("Yes");
		solo.waitForText("Stats cleared for current selection.");
		assertEquals("0", views.getText().toString());
		assertEquals("0", prompts.getText().toString());
		assertEquals("0", completions.getText().toString());
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}