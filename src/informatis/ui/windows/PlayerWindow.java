package informatis.ui.windows;

import informatis.ui.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class PlayerWindow extends Window {
    Vec2 scrollPos = new Vec2(0, 0);
    TextField search;
    ImageButton.ImageButtonStyle ustyle;
    @Nullable Player target;
    float heat;

    public PlayerWindow() {
        super(Icon.players, "player");
        only = true;

        Events.run(EventType.Trigger.update, () -> {
            heat += Time.delta;
            if(heat >= 60f) {
                heat = 0f;
                ScrollPane pane = find("player-pane");
                pane.setWidget(rebuild());
            }
            if(target != null) {
                if(control.input instanceof DesktopInput desktopInput) desktopInput.panning = true;
                Core.camera.position.set(target.x, target.y);
            }
        });
    }

    @Override
    public void buildBody(Table table) {
        scrollPos = new Vec2(0, 0);
        search = Elem.newField(null, f->{});
        search.setMessageText(Core.bundle.get("players.search"));

        ustyle = new ImageButton.ImageButtonStyle(){{
            down = Styles.none;
            up = Styles.none;
            imageDownColor = Pal.accent;
            imageUpColor = Color.white;
            imageOverColor = Color.lightGray;
        }};

        table.background(Styles.black8).top();
        table.label(()-> Core.bundle.format(Groups.player.size() == 1 ? "players.single" : "players", Groups.player.size())).row();
        table.add(search).growX().pad(8).name("search").maxTextLength(maxNameLength).row();
        table.add(new OverScrollPane(rebuild(), Styles.noBarPane, scrollPos).disableScroll(true, false)).grow().name("player-pane");
    }

    public Table rebuild(){
        return new Table(table -> {
            float h = 74f;

            Seq<Player> players = Groups.player.copy(new Seq<>());

            players.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin)));
            if(search.getText().length() > 0){
                players.filter(p -> Strings.stripColors(p.name().toLowerCase()).contains(search.getText().toLowerCase()));
            }

            if(players.isEmpty()){
                table.add(Core.bundle.format("players.notfound")).center();
                return;
            }

            for(Player user : players){
                table.table(userTable-> {
                    userTable.left().margin(5).marginBottom(10);

                    Table table1 = new Table(){
                        @Override
                        public void draw(){
                            super.draw();

                            Draw.color(target==user?Pal.accent:Pal.gray);
                            Draw.alpha(parentAlpha);
                            Lines.stroke(Scl.scl(4f));
                            Lines.rect(x, y, width, height);
                            Draw.reset();
                        }
                    };
                    table1.margin(8);
                    table1.add(new Image(user.icon()).setScaling(Scaling.bounded)).grow();
                    table1.clicked(() -> {
                        if(target==user) target = null;
                        else target = user;
                    });

                    userTable.add(table1).size(h).name(user.name()); //unit icon
                    userTable.labelWrap(user.name()).color(user.color()).width(170f).pad(10); //name
                    userTable.image(Icon.admin).padRight(5).visible(()->user.admin); //admin
                    userTable.button(Icon.hammer, ustyle, () -> { //vote kick
                        ui.showConfirm("@confirm", Core.bundle.format("confirmvotekick",  user.name()), () -> {
                            Call.sendChatMessage("/votekick " + user.name());
                        });
                    }).right().visible(()->user.team()==player.team()&&user!=player&&!user.admin);
                }).padBottom(-6).maxHeight(h + 14).row();
                table.image().height(4f).color(state.rules.pvp ? user.team().color : Pal.gray).growX().row();
            }
        });
    }
}
