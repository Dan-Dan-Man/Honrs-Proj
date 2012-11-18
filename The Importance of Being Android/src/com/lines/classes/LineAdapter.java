package com.lines.classes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.R;

/**
 * Our custom adapter which will handle populating listviews with our lines
 * 
 * @author Dan
 *
 */
public class LineAdapter extends ArrayAdapter<Line> {

	private ArrayList<Line> lines;

    public LineAdapter(Context context, int textViewResourceId, ArrayList<Line> lines) {
            super(context, textViewResourceId, lines);
            this.lines = lines;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.play_list_layout, null);
            }
            Line line = lines.get(position);
            if (line != null) {
                    TextView mCharacter = (TextView) v.findViewById(R.id.textCharacter);
                    TextView mLine = (TextView) v.findViewById(R.id.textLine);
                    if (mCharacter != null) {
                          mCharacter.setText(line.getCharacter());                            }
                    if(mLine != null){
                          mLine.setText(line.getLine());
                    }
            }
            return v;
    }
}
