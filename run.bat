@echo off
if "%1" == "" (goto help) else goto run

:run
echo run.java.class [%1] [%2] [%3]
java -Xms1536m -Xmx1536m -Xmn512m -Xss4m -cp QuantS1711.jar;dom4j-1.6.1.jar;DQuant.jar;dom4j-1.6.1.jar;htmlparser-1.6.jar;jeromq-0.3.4.jar;json-20170516.jar;protobuf-java-2.4.1.jar;servlet-api.jar;THSApi.jar;xmlParserAPIs-2.0.2.jar; %1 %2 %3
goto :end

:help
echo Param: run java class
echo     class list:
echo     QuantExtend1711.QS1711
echo     QuantExtend1801.QS1802T1
echo     QuantExtend2002.RunQEStrategy2002T1
echo     QuantExtend2002.RunCommon
goto :end

:end  
