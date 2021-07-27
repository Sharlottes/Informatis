package UnitInfo.core;

import UnitInfo.*;
import arc.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import org.hjson.*;

import static UnitInfo.SVars.modRoot;

public class PlayerParser{
    ObjectMap<Player, Seq<String>> chats = new ObjectMap<>();
    public void setEvent() {
        Events.on(EventType.PlayerChatEvent.class, e -> {
            if(chats.containsKey(e.player)) chats.get(e.player).add(e.message);
            else chats.put(e.player, Seq.with(e.message));
            writeJson(e.player);
            save();
        });

        Events.run(EventType.Trigger.update, ()->{
            if(Vars.net.active()) Groups.player.each(this::writeJson);

            save();
        });
    }

    public void createFile() {
        if(!modRoot.child("players.hjson").exists()) save();
    }

    public void writeJson(Player player1){
        PlayerInfo info = SVars.playerInfos.find(pi -> pi.player == player1);
        if(info != null){
            if(!info.names.contains(player1.name)) info.names.add(player1.name);
        }
        else{
            SVars.playerInfos.add(new PlayerInfo(){{
                player = player1;
                names.add(player1.name);
            }});
        }
    }

    public void save() {
        JsonObject data = new JsonObject();
        SVars.playerInfos.each(pi->{
            JsonArray arr = new JsonArray();
            JsonArray chatArr = new JsonArray();
            pi.names.each(arr::add);
            if(chats.get(pi.player) != null) chats.get(pi.player).each(chatArr::add);
            data.add("names", arr);
            data.add("chats", chatArr);
        });
        modRoot.child("players.hjson").writeString(data.toString(Stringify.HJSON));
    }
    public static class PlayerInfo{
        Player player;
        Seq<String> names = new Seq<>();
    }
}
