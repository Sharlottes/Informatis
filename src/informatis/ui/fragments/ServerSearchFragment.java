package informatis.ui.fragments;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.ui.*;
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
    Seq<ServerGroup> originServerGroup = new Seq<>();
    ObjectMap<ServerGroup, String[]> originServerAddresses = new ObjectMap<>();
    boolean loading = false;
    Label loadingLabel = new Label("");
    public ServerSearchFragment() {
        super();

        Core.app.post(() -> {
            originServerGroup.set(Vars.defaultServers.copy());
            originServerGroup.each(group -> originServerAddresses.put(group, group.addresses));
            Log.info("[Informatis] Fetched @ community servers.", originServerGroup.size);
        });

        final int[] i = {0};
        final int[] count = {0};
        Events.run(EventType.Trigger.update, () -> {
            if(!loading) return;
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
            Seq<Element> saved = serverTable.getChildren().copy();
            serverTable.clear();
            serverTable.add(saved.get(0)).row();
            serverTable.add(this).row();
            serverTable.add(loadingLabel).pad(5).row();
            serverTable.add(saved.get(1)).row();
            serverTable.add(saved.get(2));
        });
    }

    void refreshAll() {
        try {
            Method refreshAll = Vars.ui.join.getClass().getDeclaredMethod("refreshAll");
            refreshAll.setAccessible(true);
            try {
                refreshAll.invoke(Vars.ui.join);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    void loadingEnd() {
        loadingLabel.setText("");
        loading = false;
        getChildren().each(elem -> ((TextButton)elem).setDisabled(false));
        refreshAll(); 
    }
    
    void refresh() {
        loading = true;
        getChildren().each(elem -> ((TextButton)elem).setDisabled(true));
        Time.run(15 * 60, () -> {
            if(loading) loadingEnd();
        });
        final int[] iStack = {0};
        for(int i = 0; i < originServerGroup.size; i ++){
            int j = i;
            ServerGroup group = originServerGroup.get((i + originServerGroup.size/2) % originServerGroup.size);
            Seq<String> tmp = new Seq<>();
            final int[] iiStack = {0};
            int addressesLength = group.addresses.length;
            Runnable checkLast = () -> {
                if(!loading) return;
                if(++iiStack[0] < addressesLength) return;                
                if(++iStack[0] < originServerGroup.size) return;
                loadingEnd();
            };
            for(int ii = 0; ii < addressesLength; ii++){
                String address = group.addresses[ii];
                Vars.net.pingHost(
                    address.contains(":") ? address.split(":")[0] : address,
                    address.contains(":") ? Strings.parseInt(address.split(":")[1]) : Vars.port,
                    res -> {
                        if(loading && !tmp.contains(address) &&
                            (res.modeName != null
                                ? Objects.equals(mode, "custom") || res.modeName.equals(mode)
                                : !Objects.equals(mode, "custom") && res.mode.equals(Gamemode.valueOf(mode))
                            )
                        ) tmp.add(address);
                        group.addresses = tmp.toArray(String.class);
                        Vars.defaultServers.set((j + Vars.defaultServers.size/2) % Vars.defaultServers.size, group);
                        checkLast.run();
                    },
                    e -> checkLast.run()
                );
            }
        }
    }

    void addButton(String string) {
        TextButton button = new TextButton("@mode." + string + ".name", Styles.flatTogglet);
        button.getLabel().setWrap(false);
        button.clicked(()-> {
            mode = string;
            getChildren().each(elem -> !elem.equals(button), elem -> ((TextButton)elem).setChecked(false));
            Vars.defaultServers.set(originServerGroup);
            defaultServers.each(group -> group.addresses = originServerAddresses.get(group));
            if(button.isChecked()) refresh();
            else refreshAll();
        });
        add(button).minWidth(100).height(50);
    }
}
