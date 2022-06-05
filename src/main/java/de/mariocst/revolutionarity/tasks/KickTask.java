package de.mariocst.revolutionarity.tasks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;
import de.mariocst.revolutionarity.Revolutionarity;

public class KickTask extends PluginTask<Revolutionarity> {

    private Player player;

    private String reason;

    public KickTask(Revolutionarity owner, Player player, String reason) {
        super(owner);

        this.reason = reason;
        this.player = player;
    }

    @Override
    public void onRun(int i) {
        player.kick("Подозрение на читы, причина: " + reason);

        Revolutionarity.banned.remove(player.getName());
    }
}
