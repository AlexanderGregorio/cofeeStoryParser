package main;

import model.Parser;
import model.StoryFileWrongFormatException;

public class Main {

	public static void main(String[] args) {		
		Parser p = new Parser();
		try {
			p.parseFile("StoryFile.story");
		} catch (StoryFileWrongFormatException e) {
			e.printStackTrace();
		}
		
//		System.out.println("  asldk asdl   asd apsod   ".trim().replaceAll("\\s+", " "));
	}

}
