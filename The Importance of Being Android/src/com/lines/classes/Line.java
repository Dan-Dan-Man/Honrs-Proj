package com.lines.classes;

/**
 * Class for handling each line of the play.
 * 
 * @author Dan
 *
 */
public class Line {

	private String character;
	private String line;
	
	public Line(String character, String line) {
		this.character = character;
		this.line = line;
	}
	
	public String getCharacter() {
		return character;
	}
	
	public String getLine() {
		return line;
	}
}
