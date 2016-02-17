# email-crawler

Email crawler written in java

Download .jar from https://github.com/dipenpradhan/email-crawler/releases

Run the JAR file and enter the start domain as a parameter, as shown below:

```java -jar email_crawler.jar syr.edu```

To build a jar from source (can be found at build/libs/):

``./gradlew jar```

To build a distributable .tar/.zip containing the executable jar, library jar and a script that runs it (located at build/distributions/):

```./gradlew assembleDist```


To build & run from source:

```./gradlew run -Dargs=syr.edu```
