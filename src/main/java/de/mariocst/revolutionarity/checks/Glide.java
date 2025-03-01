package de.mariocst.revolutionarity.checks;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.utils.CheckUtils;

import java.util.HashMap;

public class Glide implements Listener {
    private final Revolutionarity plugin;

    public static final HashMap<String, Integer> checks = new HashMap<>();

    public Glide(Revolutionarity plugin) {
        this.plugin = plugin;
    }

    private final HashMap<Player, Integer> ticksInAir = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!this.plugin.getSettings().isGlide()) return;

        Player player = event.getPlayer();

        if (player.hasPermission("revolutionarity.bypass.glide") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        if (player.getAdventureSettings().get(AdventureSettings.Type.FLYING)) return;

        if (player.getGamemode() == 1 || player.getGamemode() == 3) return;

        if (!CheckUtils.isOnGround(player) && event.getFrom().getY() == player.getY()) {
            int airTime = this.ticksInAir.containsKey(player) ? this.ticksInAir.get(player) + 1 : 1;

            this.ticksInAir.remove(player);
            this.ticksInAir.put(player, airTime);
        }
        else {
            this.ticksInAir.remove(player);
        }

        if (!this.ticksInAir.containsKey(player)) return;

        if (this.ticksInAir.get(player) >= this.plugin.getSettings().getMaxTicksInAir()) {
            this.plugin.flag("Glide", "AirTime: " + this.ticksInAir.get(player) + "/" + this.plugin.getSettings().getMaxTicksInAir(), player);

            if (checks.containsKey(player.getName())){
                checks.put(player.getName(), checks.get(player.getName()) + 1);
            }else{
                checks.put(player.getName(), 1);
            }

            if (checks.get(player.getName()) > 5){
                Revolutionarity.banPlayer(player, "Glide");
                checks.remove(player.getName());
            }
        }
    }
}
