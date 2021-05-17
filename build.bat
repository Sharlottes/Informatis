: del /q C:\Users\lg\AppData\Roaming\Mindustry\mods\UnitInfo.jar;
: move C:\Users\lg\Documents\GitHub\UnitInfo\build\libs\UnitInfo.jar C:\Users\lg\AppData\Roaming\Mindustry\mods;
: java -jar C:\Users\lg\Desktop\Mindustry.jar


: del D:\SteamLibrary\steamapps\common\Mindustry\saves\mods\CoreDesktop.jar
del C:\Users\lg\AppData\Roaming\Mindustry\mods\UnitInfo.jar
: xcopy D:\workspace\Java\Core\build\libs\CoreDesktop.jar D:\SteamLibrary\steamapps\common\Mindustry\saves\mods\ /k
xcopy C:\Users\lg\Documents\GitHub\UnitInfo\build\libs\UnitInfo.jar C:\Users\lg\AppData\Roaming\Mindustry\mods\ /k
del C:\Users\lg\Documents\GitHub\UnitInfo\build\libs\UnitInfo.jar
: cd C:\Users\lg\AppData\Roaming\Mindustry\
: java -jar Mindustry.jar
: start /max steam://rungameid/1127400
: java -jar D:\SteamLibrary\steamapps\common\Mindustry\jre\desktop.jar