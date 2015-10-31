set CREATION=C:\java\openshift\wildfly\database\create
set MIGRATION=C:\java\openshift\wildfly\database\migrate
set /p REQUIRED_VER=<C:\java\openshift\wildfly\database\version.properties
set PGLOGIN=wildfly
set PGPASSWORD=wildfly
for /f %%i in ('C:\PROGRA~1\POSTGR~1\9.2\bin\psql.exe -c "copy (select id from version) to stdout;" -q -U wildfly') do set GIVEN_VER=%%i

for /f %%i in ('C:\Programs\Python26\python.exe ssmw.py %CREATION% %MIGRATION% %GIVEN_VER% %REQUIRED_VER%') do C:\PROGRA~1\POSTGR~1\9.2\bin\psql.exe -q -a -U %PGLOGIN% -f %%i