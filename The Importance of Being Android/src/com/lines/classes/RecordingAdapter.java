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

package com.lines.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lines.R;

/**
 * Custom adapter that will handle populating the listview of all the recordings
 * made by the user
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class RecordingAdapter extends ArrayAdapter<Recording> {

	private ArrayList<Recording> recordings;

	public RecordingAdapter(Context context, int textViewResourceId,
			ArrayList<Recording> recordings) {
		super(context, textViewResourceId, recordings);
		this.recordings = recordings;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.note_row_layout, null);
		}
		Recording record = recordings.get(position);
		String name = record.getName();
		int time = record.getTime();
		if (name != null) {
			TextView mName = (TextView) v.findViewById(R.id.textTitle);
			if (mName != null) {
				mName.setText(name);
			}
		}
		TextView mTime = (TextView) v.findViewById(R.id.textNote);
		if (mTime != null) {

			SimpleDateFormat outFormat = new SimpleDateFormat("mm:ss");
			outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			Date d = new Date(time);
			String result = outFormat.format(d);

			mTime.setText(result);
		}
		return v;
	}
}
