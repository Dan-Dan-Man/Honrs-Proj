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

/**
 * Class for handling each line of the play.
 * 
 * @author Daniel Muir, s0930256
 * 
 */
public class Line {

	private int number;
	private String character;
	private String line;
	private boolean note;
	private boolean audio;

	// Constructor
	public Line(int number, String character, String line, boolean note,
			boolean audio) {
		this.number = number;
		this.character = character;
		this.line = line;
		this.note = note;
		this.audio = audio;
	}

	// Getters and Setters
	public int getNumber() {
		return number;
	}

	public String getCharacter() {
		return character;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String newLine) {
		this.line = newLine;
	}

	public boolean getNote() {
		return note;
	}

	public void setNote(boolean note) {
		this.note = note;
	}

	public boolean getAudio() {
		return audio;
	}

	public void setAudio(boolean audio) {
		this.audio = audio;
	}
}
