package UnitInfo.ui.windows;

import UnitInfo.ui.OverScrollPane;
import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class CommandDisplay extends WindowTable {
    Vec2 scrollPos = new Vec2(0, 0);

    public CommandDisplay() {
        super("Command Display", Icon.commandRally, t -> {
        });
    }

    @Override
    public void build() {
        scrollPos = new Vec2(0, 0);
        top();
        topBar();

        table(Styles.black8, table -> {
            table.add(new OverScrollPane(rebuild(), Styles.nonePane, scrollPos).disableScroll(true, false)).grow().name("player-pane");
        }).top().right().grow().get().parent = null;

        resizeButton();
    }

    public Table rebuild() {
        return new Table(table -> {
            for(CommandHandler.Command cmd : Vars.netServer.clientCommands.getCommandList()) {
                table.table(cmdtable-> {
                    Seq<TextField> fields = new Seq<>();
                    cmdtable.table(body->{
                        body.left();
                        body.table(main->{
                            main.add(cmd.text);
                            for(CommandHandler.CommandParam param : cmd.params) {
                                TextField field = main.field(null, f->{}).get();
                                field.setMessageText(param.name);
                                fields.add(field);
                            }
                        }).left().row();
                        body.add(cmd.description).color(Pal.gray).left().row();
                    }).minWidth(400f).left();
                    cmdtable.button(Icon.play, ()->{
                        final String[] params = {""};
                        fields.forEach(f-> params[0] +=" "+f.getText());
                        Vars.netServer.clientCommands.handleMessage(Vars.netServer.clientCommands.getPrefix()+cmd.text+params[0], Vars.player);
                    });
                }).row();
                table.image().height(4f).color(Pal.gray).growX().row();
            }
        });
    }
}