package com.lines.classes;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;

/**
 * Our custom adapter which will handle populating listviews with our lines
 * 
 * @author Dan
 * 
 */
// TODO: This is not needed anymore. Keep it just incase
public class NoteAdapter extends ArrayAdapter<String> {

	private ArrayList<String> notes;

	public NoteAdapter(Context context, int textViewResourceId,
			ArrayList<String> notes) {
		super(context, textViewResourceId, notes);
		this.notes = notes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.note_row_layout, null);
		}
		String note = notes.get(position);
		if (note != null) {
			TextView mNote = (TextView) v.findViewById(R.id.textNote);
			if (mNote != null) {
				mNote.setText(note);
			}
		}
		return v;
	}
}
