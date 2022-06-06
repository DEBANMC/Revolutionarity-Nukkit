package de.mariocst.revolutionarity.checks;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJumpEvent;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.listener.PlayerTasks;
import de.mariocst.revolutionarity.utils.CheckUtils;

import java.util.HashMap;

public class AirJump implements Listener {
    private final Revolutionarity plugin;

    public static final HashMap<String, Integer> checks = new HashMap<>();

    public AirJump(Revolutionarity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        if (!this.plugin.getSettings().isAirJump()) return;

        Player player = event.getPlayer();

        if (player.hasPermission("revolutionarity.bypass.airjump") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        if (player.getGamemode() == 1 || player.getGamemode() == 3) return;

        if (!CheckUtils.isOnGround(player)) {
            this.plugin.flag("AirJump", player);

            if (checks.containsKey(player.getName())){
                checks.put(player.getName(), checks.get(player.getName()) + 1);
            }else{
                checks.put(player.getName(), 1);
            }

            if (checks.get(player.getName()) > 4){
                Revolutionarity.banPlayer(player, "AirJump");
                checks.remove(player.getName());
            }
        }else{
            PlayerTasks.lastOnGround.remove(player);
            PlayerTasks.lastOnGround.put(player, player.getLocation());
        }
    }
}
