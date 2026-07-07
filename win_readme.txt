Установка LMS

https://lyrion.org/getting-started/



Установка Java (JDK 17)

1. Скачай .msi установщик с https://adoptium.net/ (выбрать JDK 17, Windows, x64).
2. Запусти .msi, нажимай «Далее» до завершения.
3. Открой «Переменные среды» (поиск в Пуске). В системных переменных создай JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.xx-hotspot (подставь свою версию).
4. В переменной Path добавь %JAVA_HOME%\bin.

Установка Maven
1. Скачай бинарный ZIP с https://maven.apache.org/download.cgi (файл apache-maven-3.9.x-bin.zip).
   https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip
2. Распакуй в C:\apache-maven-3.9.x.
3. Создай системную переменную MAVEN_HOME = C:\apache-maven-3.9.x.
4. В Path добавь %MAVEN_HOME%\bin.

Проверка
Открой новую командную строку и выполни:
  java -version
  mvn -version
— должны показать версии без ошибок.