setlocal
set MOD_NAME=Informatis

@rem put this project's path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\user\Documents\GitHub
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\user\AppData\Roaming\Mindustry

if exist %PATH_TO%\mods\raw-%MOD_NAME%.jar del %PATH_TO%\mods\raw-%MOD_NAME%.jar
xcopy %PATH_FROM%\%MOD_NAME%\build\libs\raw-%MOD_NAME%.jar %PATH_TO%\mods\ /k /y
