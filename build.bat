@rem put this project path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\user\Documents\GitHub\UnitInfo
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\user\AppData\Roaming\Mindustry
@rem put your minudstry *jar* path into MINDUSTRY
setlocal
set MINDUSTRY=C:\Users\user\Downloads\Mindustry.jar

if exist %PATH_TO%\mods\UnitInfo.jar del %PATH_TO%\mods\UnitInfo.jar
xcopy %PATH_FROM%\build\libs\UnitInfo.jar %PATH_TO%\mods\ /k
del %PATH_FROM%\build\libs\UnitInfo.jar
java -jar %MINDUSTRY%