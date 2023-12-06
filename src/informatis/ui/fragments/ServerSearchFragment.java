package informatis.ui.fragments;

import arc.Core;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.Tex;
import mindustry.net.*;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

public class ServerSearchFragment extends Table {
    final ObjectMap<ServerGroup, ObjectSet<Host>> servers = new ObjectMap<>();
    static final String[] modeNames = new String[]{ "Survival", "PvP", "Attack", "Sandbox", "Custom" };

    public ServerSearchFragment() {
        super();
        setBackground(Tex.button);
        addButtons(modeNames);
        Core.app.post(() -> {
            Table header = (Table) Vars.ui.join.getChildren().get(0);
            header.row();
            header.add(this);

            loadServers();
        });
    }

    void addButtons(String[] modes) {
        TextButton[] buttons = new TextButton[modes.length];
        for(int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            TextButton button = new TextButton("@mode." + mode.toLowerCase() + ".name", Styles.flatTogglet) {{
                getLabel().setWrap(false);
                clicked(()-> {
                    for(TextButton otherButton : buttons) {
                        if(otherButton == this) continue;
                        otherButton.setChecked(false);
                    }
                    if(isChecked()) filterServers(mode);
                    else defaultServers.set(servers.keys().toSeq());
                    Reflect.invoke(Vars.ui.join, "refreshCommunity");
                });
            }};
            buttons[i] = button;
            add(button).minWidth(100).height(50);
        }
    }

    void filterServers(String mode) {
        Vars.defaultServers.clear();
        Seq<ServerGroup> serverGroups = new Seq<>();

        ObjectMap.Entries<ServerGroup, ObjectSet<Host>> serverEntries = servers.entries();
        int i = 0;
        while(serverEntries.hasNext) {
            ObjectMap.Entry<ServerGroup, ObjectSet<Host>> serverEntry = serverEntries.next();
            ServerGroup serverGroup = serverEntry.key;
            Seq<Host> hosts = serverEntry.value.toSeq();
            Seq<String> addresses = new Seq<>();
            for(Host host : hosts) {
                if(mode.equals("Custom")
                        ? host.modeName != null
                            ? !Structs.contains(modeNames, h -> host.modeName.equals(h.toLowerCase()))
                            : host.mode.equals(Gamemode.editor)
                        : host.modeName != null
                            ? host.modeName.equals(mode)
                            : host.mode.equals(Gamemode.valueOf(mode.toLowerCase()))
                ) {
                    addresses.add(host.address +":"+ host.port);
                }
            }
            if(addresses.any()) {
                i++;
                serverGroups.add(new ServerGroup(serverGroup.name, addresses.toArray(String.class)));
            }
        }
        defaultServers.set(serverGroups);
    }

    void loadServers() {
        var url = becontrol.active() ? serverJsonBeURL : serverJsonURL;
        Log.info("[Informatis] Fetching community servers at @", url);

        //get servers
        Http.get(url)
            .error(t -> Log.err("[Informatis] Failed to fetch community servers", t))
            .submit(result -> {
                Jval val = Jval.read(result.getResultAsString());
                Seq<ServerGroup> serverGroups = new Seq<>();
                val.asArray().each(child -> {
                    String name = child.getString("name", "");
                    String[] addresses;
                    if(child.has("addresses") || (child.has("address") && child.get("address").isArray())){
                        addresses = (child.has("addresses") ? child.get("addresses") : child.get("address")).asArray().map(Jval::asString).toArray(String.class);
                    }else{
                        addresses = new String[]{child.getString("address", "<invalid>")};
                    }
                    serverGroups.add(new ServerGroup(name, addresses));
                });
                serverGroups.sort(s -> s.name == null ? Integer.MAX_VALUE : s.name.hashCode());
                Log.info("[Informatis] Fetched @ community servers at @", serverGroups.size, url);

                refreshServers(serverGroups);
            });
    }

    void refreshServers(Seq<ServerGroup> serverGroups) {
        refreshServers(serverGroups, () -> {});
    }

    void refreshServers(Seq<ServerGroup> serverGroups, Runnable onDone) {
        Runnable onDoneWrapper = () -> {
            Log.info("[Informatis] Fetched @ community servers.", servers.size);
            onDone.run();
        };

        Log.info("[Informatis] Fetching community servers");
        final int[] doneCount = {0, 0};
        for(ServerGroup server : serverGroups) {
            servers.put(server, new ObjectSet<>());

            for(String address : server.addresses) {
                doneCount[1]++;
                net.pingHost(
                    address.contains(":") ? address.split(":")[0] : address,
                    address.contains(":") ? Strings.parseInt(address.split(":")[1]) : Vars.port,
                    host -> {
                        servers.get(server).add(host);

                        if (++doneCount[0] == doneCount[1]) onDoneWrapper.run();
                    },
                    (e) -> {
                        if (++doneCount[0] == doneCount[1]) onDoneWrapper.run();
                    });
            }
        }
    }
}
