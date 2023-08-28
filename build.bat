@rem put this project's path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\user\Documents\GitHub
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\user\AppData\Roaming\Mindustry

@rem clean up
if exist %PATH_TO%\mods\SharlottesInformatis*.zip del %PATH_TO%\mods\SharlottesInformatis*.zip
if exist %PATH_TO%\mods\InformatisDesktop.jar del %PATH_TO%\mods\InformatisDesktop.jar

xcopy %PATH_FROM%\Informatis\build\libs\InformatisDesktop.jar %PATH_TO%\mods\ /k /y
