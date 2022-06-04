package de.mariocst.revolutionarity.tasks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;
import de.mariocst.revolutionarity.Revolutionarity;

import javax.swing.text.PlainDocument;
import java.util.HashMap;

public class BanTask extends PluginTask<Revolutionarity> {

    private Player player;

    public BanTask(Revolutionarity owner, Player player) {
        super(owner);

        this.player = player;
    }

    @Override
    public void onRun(int i) {
        String nick = "\"" + player.getName() + "\"";
        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), "b " + nick + " 0 1.3.1");

        Revolutionarity.banned.remove(player.getName());
    }
}
