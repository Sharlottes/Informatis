package UnitInfo.core;

import UnitInfo.SVars;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.Stringify;

import static UnitInfo.SVars.modRoot;

public class PlayerParser{

    public void setEvent() {
        Events.on(EventType.PlayerJoin.class, e -> {
            writeJson(e.player);
        });
        Events.on(EventType.ServerLoadEvent.class, e->{
            Groups.player.each(this::writeJson);
        });
        Events.on(EventType.WorldLoadEvent.class, e->{
            if(Vars.net.active()) Groups.player.each(this::writeJson);
        });
        Events.run(EventType.Trigger.update, ()->{
            if(Vars.net.active())
                Groups.player.each(this::writeJson);
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
        save();
    }

    public void save() {
        JsonObject data = new JsonObject();
        SVars.playerInfos.each(pi->{
            JsonArray arr = new JsonArray();
            pi.names.each(arr::add);
            data.add("names", arr);
        });

        modRoot.child("players.hjson").writeString(data.toString(Stringify.HJSON));
    }
    public static class PlayerInfo{
        Player player;
        Seq<String> names = new Seq<>();
    }
}
