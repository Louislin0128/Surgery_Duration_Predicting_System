javac -cp Jar\*; -d classes src\gui\*.java src\guiComponent\*.java src\guiFunction\*.java src\predict\*.java src\preprocess\*.java
java -Xms64m -Xmx4096m -cp Jar\*;.\classes; gui.ProcessFrame

pause