package com.lines.classes;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lines.R;
import com.lines.activitys.MainActivity;
import com.lines.activitys.NotesActivity;
import com.lines.database.notes.NoteDbAdapter;

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
		final int pos = position;
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
					//showNotes(pos);
				}
			});
		}
		return v;
	}

//	/**
//	 * This method goes to the NotesActivity and shows only the notes for the
//	 * selected line.
//	 * 
//	 * @param id
//	 *            - the selected item in the list
//	 */
//	private void showNotes(long id) {
//		// First check if there are notes to view. If not then tell user they
//		// must add note for current line first.
//		Cursor notes = mNDbAdapter.fetchNotes(Long.toString(getLineNumber(id)));
//		if (notes.getCount() > 0) {
//			Intent i = new Intent(MainActivity.this, NotesActivity.class);
//			i.putExtra("EXTRA_NUM", Long.toString(getLineNumber(id)));
//			MainActivity.this.startActivity(i);
//		} else {
//			Toast.makeText(getApplicationContext(),
//					"No saved notes for this line.", Toast.LENGTH_SHORT).show();
//		}
//	}
//
//	/**
//	 * Get the line number of the current selected item in the list
//	 * 
//	 * @param id
//	 *            - the list item the user selects
//	 * @return - the line number
//	 */
//	private long getLineNumber(long id) {
//		long lineNo;
//
//		lineNo = lines.get((int) id).getNumber();
//		lineNo++;
//
//		return lineNo;
//	}
}
