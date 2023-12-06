package informatis.core;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.func.*;
import arc.util.*;
import arc.util.serialization.Jval;

import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;

import java.util.Objects;

public class UpdateChecker {
    private static boolean updateAvailable = false;
    private static String updateBuild;

    public static void init() {
        Vars.ui.menuGroup.fill(c -> {
            c.bottom().right();
            c.button("@informatis.update-check", Icon.refresh, () -> {
                Vars.ui.loadfrag.show();
                checkUpdate(result -> {
                    Vars.ui.loadfrag.hide();
                    if(!result){
                        Vars.ui.showInfo("@be.noupdates");
                    }
                });
            }).size(200, 60).padBottom(45).update(t -> {
                t.getLabel().setColor(updateAvailable ? Tmp.c1.set(Color.white).lerp(Pal.accent, Mathf.absin(5f, 1f)) : Color.white);
            });
        });
    }

    private static void checkUpdate(Boolc done) {
        Http.get("https://api.github.com/repos/Sharlottes/Informatis/releases/latest")
            .error(e -> {
                done.get(false);
            })
            .submit(res -> {
                Jval json = Jval.read(res.getResultAsString());
                String version = json.getString("tag_name");
                if(Objects.equals("v"+ModMetadata.metadata.version, version)) {
                    done.get(false);
                } else {
                    updateAvailable = true;
                    updateBuild = version;
                    showUpdateDialog();
                    done.get(true);
                }
            });
    }

    private static void showUpdateDialog(){
        Vars.ui.showCustomConfirm(
                Core.bundle.format("informatis.update", updateBuild), "@be.update.confirm",
                "@ok", "@be.ignore",
                () -> {
                    Vars.ui.showCustomConfirm("", "@informatis.update-reloadexit", "@ok", "@be.ignore",
                        () -> Core.app.exit(), () -> {}
                    );
                    Vars.ui.mods.githubImportMod("Sharlottes/Informatis", true);
                    updateAvailable = false;
                },
                () -> {}
        );
    }
}
