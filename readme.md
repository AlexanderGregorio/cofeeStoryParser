# CofeeStoryParser

Based on a story file it creates a template for the java file used by JBehave.

### How it works

When reading the file it looks for lines that start with the keywords "Given", "When", "Then" and "And" to parse to an approprietade method. If there is no space after the keyword, the line will also be ignored.
If there is as "#" in the line, everything that comes after is ignored.
To create the java file only letters and numbers will be used, if any other symbol is used to name the story file it will be lost, and it will be handled as it was an space for the purpose of transforming the file name to a camelcase pattern.

### Know issues

The parser ignores any other line that starts with anything different of the keywords.
It only throws StoryFileFormatException IF the keywords appear in a different order, i.e., "When" appears before "Given" or "And" appears before any other keyword.
When you are trying to create a java class for a story, if there is alredy a class with the same name, it will be deleted.

### To implement

The parser still doesn't recognizes parameters to the functions.
