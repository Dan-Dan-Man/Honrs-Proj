package com.lines.classes;

/**
 * Class for handling each line of the play.
 * 
 * @author Dan
 * 
 */
// TODO: Maybe we can store the line number for each line here and refer to that
// when we need it. Could solve a lot of problems. GET THIS DONE TOMORROW!
public class Line {

	private String character;
	private String line;
	private boolean note;

	public Line(String character, String line, boolean note) {
		this.character = character;
		this.line = line;
		this.note = note;
	}

	public String getCharacter() {
		return character;
	}

	public String getLine() {
		return line;
	}

	public boolean getNote() {
		return note;
	}

	public void setNote(boolean note) {
		this.note = note;
	}
}
