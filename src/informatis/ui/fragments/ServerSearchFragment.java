package informatis.ui.fragments;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Prov;
import arc.scene.actions.DelayAction;
import arc.scene.actions.RepeatAction;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.gen.Tex;
import mindustry.net.ServerGroup;
import mindustry.ui.Styles;

import java.lang.reflect.*;
import java.util.Objects;

import static mindustry.Vars.*;

public class ServerSearchFragment extends Table {
    String mode;
    Seq<ServerGroup> tempGroup = new Seq<>();
    ObjectMap<ServerGroup, String[]> addresses = new ObjectMap<>();
    boolean loading = false;
    Label loadingLabel = new Label("loading");
    public ServerSearchFragment() {
        super();

        Core.app.post(() -> {
            tempGroup.set(Vars.defaultServers.copy());
            tempGroup.each(group -> addresses.put(group, group.addresses));
            Log.info("[Informatis] Fetched @ community servers.", tempGroup.size);
        });

        final int[] i = {0};
        final int[] count = {0};
        Events.run(EventType.Trigger.update, () -> {
            if(!loading) {
                
                return;
            };
            i[0] += Time.delta;
            if(i[0] >= 5) {
                i[0] = 0;
                count[0]++;
                loadingLabel.setText("loading" + (count[0] % 4 == 0 ? "" : count[0] % 4 == 1 ? "." : count[0] % 4 == 2 ? ".." : count[0] % 4 == 3 ? "..." : ""));
            }
        });
        setBackground(Tex.button);
        addButton("survival");
        addButton("pvp");
        addButton("attack");
        addButton("sandbox");
        addButton("custom");
        Vars.ui.join.shown(() -> {
            Table serverTable = (Table) Vars.ui.join.getChildren().get(1);
            var saved = serverTable.getChildren().copy();
            serverTable.clear();
            serverTable.add(saved.get(0)).row();
            serverTable.add(this).row();
            serverTable.add(loadingLabel).pad(5).row();
            serverTable.add(saved.get(1)).row();
            serverTable.add(saved.get(2));
        });
    }

    void refreshAll() {
        Method refreshAll;
        try {
            refreshAll = Vars.ui.join.getClass().getDeclaredMethod("refreshAll");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        refreshAll.setAccessible(true);
        try {
            refreshAll.invoke(Vars.ui.join);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    void loadingEnd() {
        loadingLabel.setText("");
        loading = false;
        mode = "";
        getChildren().each(elem -> ((TextButton)elem).setDisabled(false));
        refreshAll(); 
    }
    
    void refresh() {
        loading = true;
        getChildren().each(elem -> ((TextButton)elem).setDisabled(true));
        Time.run(15 * 60, this::loadingEnd);
        final int[] iStack = {0};
        for(int i = 0; i < tempGroup.size; i ++){
            int j = i;
            ServerGroup group = tempGroup.get((i + tempGroup.size/2) % tempGroup.size);
            Seq<String> tmp = Seq.with(group.addresses);
            final int[] iiStack = {0};
            int addressesLength = group.addresses.length;
            Cons<Void> checkLast = ignore -> {
                if(!loading) return;
                if(++iiStack[0] < addressesLength) return;                
                if(++iStack[0] < tempGroup.size) return;
                loadingEnd();  
            };
            for(int ii = 0; ii < addressesLength; ii++){
                String address = tmp.get(ii);
                String resaddress = address.contains(":") ? address.split(":")[0] : address;
                int resport = address.contains(":") ? Strings.parseInt(address.split(":")[1]) : Vars.port;
                Vars.net.pingHost(resaddress, resport, res -> {
                    if(!loading || (tmp.contains(address) && res.mode == null
                        ? Objects.equals(res.modeName, mode)
                        : !Objects.equals(mode, "custom") && res.mode.equals(Gamemode.valueOf(mode))
                    )) {
                        tmp.remove(address);
                        group.addresses = tmp.toArray();
                        Vars.defaultServers.set((j + Vars.defaultServers.size/2) % Vars.defaultServers.size, group);
                    }
                    checkLast.get();
                }, e -> checkLast.get());
            }
        }
    }

    void addButton(String string) {
        TextButton button = new TextButton("@mode." + string + ".name", Styles.flatTogglet);
        button.getLabel().setWrap(false);
        button.clicked(()-> {
            getChildren().each(elem -> {
                if(!elem.equals(button)) ((TextButton)elem).setChecked(false);
            });
            mode = string;
            if(button.isChecked()) {
                refresh();
            } else {
              Log.info("trying backdown");
              Vars.defaultServers.set(tempGroup);
              defaultServers.each(group -> group.addresses = addresses.get(group));
              refreshAll();
              return;
            }
        });
        add(button).minWidth(100).height(50);
    }
}
