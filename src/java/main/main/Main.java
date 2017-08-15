package main;

import model.Parser;
import model.StoryFileWrongFormatException;

public class Main {

	public static void main(String[] args) {
		Parser p = new Parser();
		try {
//			p.parseFile("Story-File.story");
			p.parseAllFiles();
		} catch (StoryFileWrongFormatException e) {
			e.printStackTrace();
		}
	}

}
