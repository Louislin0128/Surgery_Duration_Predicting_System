PATH C:\Program Files\Java\jdk-17.0.2\bin
javac -cp Jar\*; -d classes src\gui\*.java src\guiComponent\*.java src\guiFunction\*.java src\predict\*.java src\preprocess\*.java
java -Xms64m -Xmx8000m -cp Jar\*;classes; --add-opens java.base/java.lang=ALL-UNNAMED gui.ProcessFrame

pause