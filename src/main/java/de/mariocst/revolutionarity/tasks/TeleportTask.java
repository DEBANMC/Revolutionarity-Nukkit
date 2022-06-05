package de.mariocst.revolutionarity.tasks;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.PluginTask;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.checks.KillAuraBot;
import de.mariocst.revolutionarity.utils.FakePlayer;

public class TeleportTask extends PluginTask<Revolutionarity> {

    private FakePlayer fplayer;
    private Position pos;

    public TeleportTask(Revolutionarity owner, FakePlayer player, Position position) {
        super(owner);

        this.fplayer = player;
        this.pos = position;
    }

    @Override
    public void onRun(int i) {
        if (fplayer != null && KillAuraBot.waitTeleport.contains(fplayer.getId())){
            fplayer.teleport(pos);
            KillAuraBot.waitTeleport.remove(fplayer.getId());
        }
    }
}
