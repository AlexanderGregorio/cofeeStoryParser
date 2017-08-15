package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser.java
 * Purpose: Creates a java class based on a ".story" file.
 * 
 * @author Alexander Gregorio
 */
public class Parser {
	private static final String[] TOKENS = {"Given ", "When ", "Then "};
	private static final String REPETITION_TOKEN = "And ";
	private static final String COMMENT_MARK = "#";
	
	private Path storiesPath;
	private Path targetPath;
	private Charset charset;
	
	/**
	 * Creates parser with default configuration.
	 * Default folder for ".story" files is "projectfolder\src\java\resources\exercises\".
	 * Default charset for files is "UTF-8".
	 */
	public Parser() {
		// System.getProperty("user.dir") -> Returns the absolute path to the project folder
		this(System.getProperty("user.dir") + "\\src\\java\\resources\\exercises\\", "UTF-8");
	}
	
	/**
	 * @param storiesSourceURI Source path for ".story" files.
	 * @param filesCharset Encoding used to read and write files.
	 */
	public Parser(String storiesSourceURI, String filesCharset) {
		super();
		this.storiesPath = Paths.get(storiesSourceURI);
		
		Path path = Paths.get(storiesSourceURI);
		String directoryName = path.getFileName().toString();
		
		this.targetPath = path.resolve(directoryName + "-JAVA\\");
		
		this.charset = Charset.forName(filesCharset);
		
		createPath(storiesPath);
		createPath(targetPath);
	}
	
	// BEGIN - HELPERS
	private static void createPath(Path path) {
		if (!Files.exists(path)) {
			path.toFile().mkdirs();
		}
	}

	private static void removeFile(Path filePath) {
		if(Files.exists(filePath)){
			try {
				Files.delete(filePath);
			} catch (IOException e) {
				System.err.println(String.format("Unable to delete file %", filePath.getFileName().toString()));
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes repeated white spaces and spaces in the beginning and ending
	 * @param string
	 */
	public static String formatInformation(String string) {
		return string.trim().replaceAll("\\s+", " ");
	}
	// END - HELPERS
	
	/**
	 * Creates a ".java" file for all ".story" files in the source path.
	 * 
	 * @throws StoryFileWrongFormatException
	 */
	public void parseAllFiles() throws StoryFileWrongFormatException {
		File folder = storiesPath.toFile();
		File[] allFilesInFolder = folder.listFiles();
	
		for(File file : allFilesInFolder) {
			if(file.getName().endsWith(".story")) {
				Path filePath = Paths.get(file.getAbsolutePath());
				parseFile(filePath);
			}
		}
	}	
	
	/**
	 * Creates a ".java" file for the ".story" file specified.
	 * 
	 * @param fileName Name of the file to be parsed.
	 * @throws StoryFileWrongFormatException
	 */
	public void parseFile(String fileName) throws StoryFileWrongFormatException {
		Path file = storiesPath.resolve(fileName);
		parseFile(file);
	}
	
	
	private void parseFile(Path file) throws StoryFileWrongFormatException{
		Map<String, String> tokenInformation = retrieveTokenInformation(file);
		Map<String, String> classAndMethodsNames = generateClassAndMethodsNames(file, tokenInformation);
		writeFile(tokenInformation, classAndMethodsNames);
	}
	
	/**
	 * Read ".story" file and retrieves relevant information.
	 * 
	 * @param file File that will be read
	 * @return Map with the relevant information of the file. It uses the tokens as keys.
	 * @throws StoryFileWrongFormatException
	 */
	private Map<String, String> retrieveTokenInformation(Path file) throws StoryFileWrongFormatException {
		Map<String, String> tokenInformation = new HashMap<String, String>();
		
		try(BufferedReader reader = Files.newBufferedReader(file, charset)){
			String line = null;
			int i = 0;
			
		    while ((line = reader.readLine()) != null) {
		    	if(line.startsWith(TOKENS[i])) {
		    		int endOfInformation = line.indexOf(COMMENT_MARK);
		    		
		    		if(endOfInformation == -1)
		    			endOfInformation = line.length();
		    		
		    		String information = line.substring(TOKENS[i].length(), endOfInformation);
		    		tokenInformation.put(TOKENS[i], formatInformation(information));
		    				    		
		    		i++;
		    	} else if(line.startsWith(REPETITION_TOKEN)) {
		    		// There is an "And" token before any other token
		    		if(i == 0) {
		    			throw new StoryFileWrongFormatException();
		    		} else {
		    			
		    		}
		    	}
		    }
		    
		    if(i != TOKENS.length)
		    	throw new StoryFileWrongFormatException();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tokenInformation;
	}
	
	
	private Map<String, String> generateClassAndMethodsNames(Path file, Map<String, String> tokenInformation){
		Map<String, String> classAndMethodsNames = new HashMap<String, String>();
		
		classAndMethodsNames.put("Class", processFileName(file));
		
		for(String token : TOKENS) {
			String[] split = tokenInformation.get(token).split(" ");
			String camelCaseName = transformToCamelCase(split, false);
			classAndMethodsNames.put(token, camelCaseName);
		}
		
		return classAndMethodsNames;
	}
	
	
	private String processFileName(Path file){
		String fileName = file.getFileName().toString();
		
		// If the file name has any special character, break it to use camel case
		String[] split = fileName.split("[^a-zA-z0-9]");
		
		// The last index is the type of the file (story)	
		String[] information = Arrays.copyOfRange(split, 0, split.length-1);
				
		return transformToCamelCase(information, true);
	}
	
	
	private String transformToCamelCase(String[] strings, boolean startsWithCapitalLetter) {		
		StringBuilder camelCase = new StringBuilder();
		
		if(startsWithCapitalLetter) {
			camelCase.append(strings[0].substring(0, 1).toUpperCase());
		} else {
			camelCase.append(strings[0].substring(0, 1).toLowerCase());
		}
		camelCase.append(strings[0].substring(1, strings[0].length()).toLowerCase());

		for(int i = 1; i < strings.length; i++) {
			camelCase.append(strings[i].substring(0, 1).toUpperCase());
			camelCase.append(strings[i].substring(1, strings[i].length()));
		}
		
		return camelCase.toString();
	}
	
	
	private void writeFile(Map<String, String> information, Map<String, String> classAndMethodsNames) {
		Path file = targetPath.resolve(classAndMethodsNames.get("Class") + ".java");
		
		// If the target file already exists, delete it before creating a new one
		// WARNING: If parsing multiple files and multiple ".story" are mapped to the same ".java" file name
		// only one ".java" file will be created
		removeFile(file);
		try(BufferedWriter writer = Files.newBufferedWriter(file, charset, StandardOpenOption.CREATE_NEW)){
			String classDefinitionTemplate = "public class %s {\n";
			String classDefinition = String.format(classDefinitionTemplate, classAndMethodsNames.get("Class"));
			writer.write(classDefinition);
			
			for(String token : TOKENS) {
				String methodsTemplate = "    @%s(\"%s\")\n"
									   + "    public void %s(){\n"
									   + "        //TODO\n"
									   + "    }\n\n";
				
				String methodString = String.format(methodsTemplate, token, information.get(token), classAndMethodsNames.get(token));
				
				writer.write(methodString);
			}
			
			writer.write("}");		
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
