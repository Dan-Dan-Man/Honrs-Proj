package com.lines.classes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;

/**
 * Our custom adapter which will handle populating listviews with our lines
 * 
 * @author Dan
 * 
 */
public class LineAdapter extends ArrayAdapter<Line> {

	private ArrayList<Line> lines;

	public LineAdapter(Context context, int textViewResourceId,
			ArrayList<Line> lines) {
		super(context, textViewResourceId, lines);
		this.lines = lines;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.play_list_layout, null);
		}
		Line line = lines.get(position);
		if (line != null) {
			TextView mCharacter = (TextView) v.findViewById(R.id.textCharacter);
			TextView mLine = (TextView) v.findViewById(R.id.textLine);
			ImageButton mNote = (ImageButton) v
					.findViewById(R.id.imageButtonNotes);
			boolean note;

			mNote.setFocusable(false);
			if (mCharacter != null) {
				mCharacter.setText(line.getCharacter());
			}
			if (mLine != null) {
				mLine.setText(line.getLine());
			}

			note = line.getNote();
			if (!note) {
				mNote.setVisibility(View.GONE);
			} else {
				mNote.setVisibility(View.VISIBLE);
			}

			mNote.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					test();
				}
			});
		}
		return v;
	}

	private void test() {
		// TODO: Need to somehow to the same functionality as found in 
		Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();
	}
}
