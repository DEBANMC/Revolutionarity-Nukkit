package de.mariocst.revolutionarity.checks;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;

public class AntiSpeedListener implements Listener {

    public static ArrayList<String> teleports = new ArrayList<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();

        if (!teleports.contains(player.getName())) {
            teleports.add(player.getName());
        }
    }
}
