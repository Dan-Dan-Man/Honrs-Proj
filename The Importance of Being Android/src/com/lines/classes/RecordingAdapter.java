package com.lines.classes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lines.R;

public class RecordingAdapter extends ArrayAdapter<String> {

	private ArrayList<String> recordings;

	public RecordingAdapter(Context context, int textViewResourceId,
			ArrayList<String> recordings) {
		super(context, textViewResourceId, recordings);
		this.recordings = recordings;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.recording_row_layout, null);
		}
		String recording = recordings.get(position);
		if (recording != null) {
			TextView mRecord = (TextView) v.findViewById(R.id.textRecord);
			if (mRecord != null) {
				mRecord.setText(recording);
			}
		}
		return v;
	}
}
