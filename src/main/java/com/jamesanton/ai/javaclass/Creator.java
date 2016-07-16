package com.jamesanton.ai.javaclass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import com.jamesanton.ai.util.FileUtil;
import com.jamesanton.ai.util.RandomUtil;

public class Creator {

	private List<String> keywords;
	private List<String> operators;
	private List<String> symbols;
	private int count = 0;
	private final static String alphabet = "abcde";
	private final static int numAlphabetItems = alphabet.length();
	private ClassLoader classLoader = getClass().getClassLoader();
	private String rootPath = "/java/randomtest";
	private String packageName = "test";
	private String className = "Test";
	private File root = new File(rootPath);
	private File sourceFile;
	private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private List<ClassBody> compiledList = new ArrayList<ClassBody>();
	private ClassBody classBody;
	private int longestSourceCode = 0;

	{
		File keywordsFile = new File(classLoader.getResource("java-keywords.txt").getFile());
		File operatorsFile = new File(classLoader.getResource("java-operators.txt").getFile());
		File symbolsFile = new File(classLoader.getResource("java-symbols.txt").getFile());
		
		try {
			keywords = FileUtils.readLines(keywordsFile);
			operators = FileUtils.readLines(operatorsFile);
			symbols = FileUtils.readLines(symbolsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
		
	public static void main(String[] args) {
		Creator c = new Creator();
		c.create();
	}
	
	private void create() {
		while (true) {
			try {
				// Generate and check if it compiles
				if (generate() == 0) {
					if (!compiledList.contains(classBody)) {
						String trimmedClassBody = classBody.toString().replace("  ", " ");
						int trimmedClassBodyLength = trimmedClassBody.length();

						
						if (trimmedClassBodyLength > longestSourceCode) {
							longestSourceCode = trimmedClassBodyLength;
							appendToFile(classBody);
							// Add the item once to the list for each of its length
							// (to increase the probability of it being picked for
							// mutation)
							for (int i = 0; i < trimmedClassBodyLength; i++) {
								compiledList.add(classBody);
							}
						}
					}
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
				// Do nothing
				e.printStackTrace();
			}
		}

	}

	private void appendToFile(ClassBody classBody2) {
		String fPath = "C:\\Users\\James\\Desktop\\out.txt";
		FileUtil.createFileIfNotExists(fPath);
		try {
			FileUtils.writeStringToFile(new File(fPath), classBody2.toString() + "\n", true);
		} catch (IOException e) {
			
		}
	}

	

	private int generate() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		count++;
		System.out.println(count);
		FileUtil.removeFilesAndFolder(rootPath);

		classBody = getAClassBody();
		String activeString = "package " + packageName + "; public class " + className + " { " + classBody.toString()
				+ " }";

		root = new File(rootPath);
		sourceFile = new File(root, "test/Test.java");
		sourceFile.getParentFile().mkdirs();
		Files.write(sourceFile.toPath(), activeString.getBytes(StandardCharsets.UTF_8));
		return compiler.run(null, null, null, sourceFile.getPath());
	}

	private ClassBody getAClassBody() {
		// 50 percent chance of choosing a new body vs mutating an existing body
		int typesOfRandom = 2;
		int randomTypeIndex = RandomUtil.getRandomNumberStartingFromZero(typesOfRandom);

		if (compiledList.size() == 0 || randomTypeIndex == 0) {
			return generateNewClassBody();
		} else {
			return generateMutatedClassBody();
		}
	}

	private ClassBody generateMutatedClassBody() {
		// Pick a random item from compiledSet
		ClassBody newBody = getRandomClassBody();
		return generateMutatedClassBody(newBody);
	}

	/**
	 * Adds up to 4 random items into random indexes of the classbody 
	 * @param newBody
	 * @return
	 */
	private ClassBody generateMutatedClassBody(ClassBody newBody) {
		for (int i = 0; i < RandomUtil.getRandomNumberStartingFromZero(5); i++) {
			newBody = mutateItem(newBody);
		}
		return newBody;
	}

	/**
	 * 2 types of mutations. 1.) Add item 2.) Remove item
	 * 
	 * @param newBody
	 * @return
	 */
	private ClassBody mutateItem(ClassBody newBody) {
		int typesOfRandom = 2;
		int randomTypeIndex = RandomUtil.getRandomNumberStartingFromZero(typesOfRandom);

		if (randomTypeIndex == 0) {
			newBody.removeRandomItem();
		} else if (randomTypeIndex == 1) {
			newBody.addToRandomIndex(getRandom());
		}
		return newBody;
	}

	private ClassBody generateNewClassBody() {
		ClassBody cb = new ClassBody();
		for (int i = 0; i < RandomUtil.getRandomNumberBetween(3, 6); i++) {
			cb.addToRandomIndex(getRandom());
		}
		return cb;
	}

	

	private String getRandom() {
		int typesOfRandom = 5;
		int randomTypeIndex = RandomUtil.getRandomNumberStartingFromZero(typesOfRandom);

		if (randomTypeIndex == 0) {
			return getRandomItemFromCollection(keywords);
		} else if (randomTypeIndex == 1) {
			return getRandomItemFromCollection(operators);
		} else if (randomTypeIndex == 2) {
			return getRandomItemFromCollection(symbols);
		} else if (randomTypeIndex == 3) {
			return getRandomVariableName();
		} else if (randomTypeIndex == 4) {
			return RandomUtil.getRandomAsStringStartingFromZero(10);
		}
		return null;
	}

	private String getRandomVariableName() {		
		return "VAR" + getRandomLetter() + getRandomLetter();
	}

	private String getRandomLetter() {
		return String.valueOf(alphabet.charAt(RandomUtil.getRandomNumberStartingFromZero(numAlphabetItems)));
	}

	private String getRandomItemFromCollection(Collection<String> col) {
		return col.toArray(new String[col.size()])[RandomUtil.getRandomNumberStartingFromZero(col.size())];
	}

	private ClassBody getRandomClassBody() {
		return new ClassBody(compiledList.get(RandomUtil.getRandomNumberStartingFromZero(compiledList.size())));
	}

}