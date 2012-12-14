package com.lines.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lines.R;

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
		Log.d("", name);
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
