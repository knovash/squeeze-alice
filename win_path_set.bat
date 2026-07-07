
set "JAVA_HOME_PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "MAVEN_HOME_PATH=C:\apache-maven-3.9.16"

setx /M JAVA_HOME "%JAVA_HOME_PATH%"
setx /M MAVEN_HOME "%MAVEN_HOME_PATH%"
setx /M PATH "%PATH%;%JAVA_HOME_PATH%\bin;%MAVEN_HOME_PATH%\bin"

echo Done
pause