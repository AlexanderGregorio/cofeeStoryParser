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
import java.util.HashMap;
import java.util.Map;

public class Parser {
	private static final String[] TOKENS = {"Given ", "When ", "Then "};
	private static final String COMMENT_MARK = "#";
	
	private String storiesSource;
	private Path storiesPath;
	private String target;
	private Path targetPath;
	private Charset charset; 
	
	
	public Parser() {
		this(System.getProperty("user.dir") + "\\src\\java\\resources\\exercises\\", "UTF-8");
	}
	
	public Parser(String storiesSourceURI, String filesCharset) {
		super();
		this.storiesSource = storiesSourceURI;
		this.storiesPath = Paths.get(storiesSourceURI);
		
//		String[] split = storiesSourceURI.split("\\");
//		String s1 = split[split.length-1];
		
		Path p = Paths.get(storiesSourceURI);
		String s2 = p.getFileName().toString();
		
		this.target = storiesSourceURI + s2 + "-JAVA\\";
		this.targetPath = Paths.get(storiesSourceURI + s2 + "-JAVA\\");
		
		this.charset = Charset.forName(filesCharset);
		
		createPath(storiesSource);
		createPath(target);
	}
	
	private static void createPath(String URI) {
		Path path = Paths.get(URI);
		if (!Files.exists(path)) {
			new File(URI).mkdirs();
		}
	}

	public void parseAllFiles() throws StoryFileWrongFormatException {
		File folder = new File(storiesSource);
		File[] allFilesInFolder = folder.listFiles();
	
		for(File file : allFilesInFolder) {
			if(file.getName().endsWith(".story")) {
				Path filePath = Paths.get(file.getAbsolutePath());
				parseFile(filePath);
			}
		}
	}	
	
	public void parseFile(String fileName) throws StoryFileWrongFormatException {
		Path file = Paths.get((storiesSource + fileName));
		parseFile(file);
	}
	
	private void parseFile(Path file) throws StoryFileWrongFormatException{
		Map<String, String> tokenInformation = retrieveTokenInformation(file);
		Map<String, String> classAndMethodsNames = generateClassAndMethodsNames(file, tokenInformation);
		writeFile(tokenInformation, classAndMethodsNames);
	}

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
		    		tokenInformation.put(TOKENS[i], information);
		    		
		    		i++;
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
		String fileNameSeparator = "-";
		String fileName = file.getFileName().toString();
		
		String[] split = fileName.split(fileNameSeparator);
		int dotIndex = split[split.length-1].indexOf(".");
		split[split.length-1] = split[split.length-1].substring(0, dotIndex);
		
		return transformToCamelCase(split, true);
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
		Path file = Paths.get(target + classAndMethodsNames.get("Class") + ".java");
		
		try(BufferedWriter writer = Files.newBufferedWriter(file, charset, StandardOpenOption.CREATE_NEW)){
			String classDefinitionTemplate = "public class # {\n";
			String classDefinition = classDefinitionTemplate.replace("#", classAndMethodsNames.get("Class"));
			writer.write(classDefinition);
			
			for(String token : TOKENS) {
				String methodsTemplate = "    @#1(\"#2\")\n"
									   + "    public void #3(){\n"
									   + "        //TODO\n"
									   + "    }\n\n";
				
				String methodString = methodsTemplate
											.replace("#1", token)
											.replace("#2", information.get(token))
											.replace("#3", classAndMethodsNames.get(token));
				
				writer.write(methodString);
			}
			
			writer.write("}");		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
