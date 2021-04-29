echo off
set PATH=C:\Program Files\Java\jdk1.6.0_45\bin;%PATH%
set "JAVA_HOME=C:\Program Files\Java\jdk1.6.0_45"


java -version
java -jar ReplayV5Tasks.jar  admin bpm "D:/tomcat/bonita"  "jaas-standard.cfg" http://localhost:8080/bonita-server-rest