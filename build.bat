@rem put this project path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\jun\Documents\GitHub\Informatis
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\jun\AppData\Roaming\Mindustry

if exist %PATH_TO%\mods\raw-Informatis.jar del %PATH_TO%\mods\raw-Informatis.jar
xcopy %PATH_FROM%\build\libs\raw-Informatis.jar %PATH_TO%\mods\ /k /y
