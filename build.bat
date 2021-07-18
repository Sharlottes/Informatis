@rem put this project path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\Administrator\Documents\GitHub\UnitInfo
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\Administrator\AppData\Roaming\Mindustry

if exist %PATH_TO%\mods\UnitInfo.jar del %PATH_TO%\mods\UnitInfo.jar
xcopy %PATH_FROM%\build\libs\UnitInfo.jar %PATH_TO%\mods\ /k
del %PATH_FROM%\build\libs\UnitInfo.jar