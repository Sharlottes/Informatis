package UnitInfo.ui.windows;

import UnitInfo.ui.OverScrollPane;
import UnitInfo.ui.Updatable;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.Elem;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.ui.*;

import static mindustry.Vars.*;


public class PlayerDisplay extends WindowTable implements Updatable {
    Vec2 scrollPos = new Vec2(0, 0);
    TextField search;
    ImageButton.ImageButtonStyle ustyle;
    @Nullable Player target;
    float heat;

    public PlayerDisplay() {
        super("Player Display", Icon.players, t -> {});
    }

    @Override
    public void build() {
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

        top();
        topBar();

        table(Styles.black8, table -> {
            table.label(()-> Core.bundle.format(Groups.player.size() == 1 ? "players.single" : "players", Groups.player.size())).row();
            table.add(search).growX().pad(8).name("search").maxTextLength(maxNameLength).row();
            table.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).grow().name("player-pane");
        }).top().right().grow().get().parent = null;

        resizeButton();
    }

    @Override
    public void update() {
        heat += Time.delta;
        if(heat >= 60f) {
            heat = 0f;
            ScrollPane pane = find("player-pane");
            pane.setWidget(rebuild());
        }
        if(target!=null) {
            if(control.input instanceof DesktopInput)
            ((DesktopInput) control.input).panning = true;
            Core.camera.position.set(target.x, target.y);
        }
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
