package de.mariocst.revolutionarity.checks;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerToggleFlightEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.potion.Effect;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.listener.PlayerTasks;
import de.mariocst.revolutionarity.utils.CheckUtils;

import java.util.HashMap;

public class Flight implements Listener {
    private final Revolutionarity plugin;

    public static final HashMap<String, Integer> checks = new HashMap<>();

    private static final HashMap<Player, Boolean> isFlying = new HashMap<>();

    public Flight(Revolutionarity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        Player player = event.getPlayer();

        if (event.getPacket().pid() != ProtocolInfo.ADVENTURE_SETTINGS_PACKET) return;

        AdventureSettingsPacket adventureSettingsPacket = (AdventureSettingsPacket) event.getPacket();

        if (!player.getServer().getAllowFlight() && adventureSettingsPacket.getFlag(AdventureSettingsPacket.FLYING) && !player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT)) {
            if (!this.plugin.getSettings().isFlight()) return;

            if (player.hasPermission("revolutionarity.bypass.flight") ||
                    player.hasPermission("revolutionarity.bypass.*") ||
                    player.hasPermission("revolutionarity.*") ||
                    player.hasPermission("*") ||
                    player.isOp()) return;

            this.plugin.flag("FlightA", player);
            adventureSettingsPacket.setFlag(AdventureSettingsPacket.FLYING, false);
            return;
        }

        isFlying.remove(player);
        isFlying.put(player, adventureSettingsPacket.getFlag(AdventureSettingsPacket.FLYING));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!this.plugin.getSettings().isFlight()) return;

        Player player = event.getPlayer();

        if (player.hasPermission("revolutionarity.bypass.flight") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        if (player.getEffects().containsKey(Effect.JUMP_BOOST)) return; // Checks will be implemented later

        if (player.getAdventureSettings().get(AdventureSettings.Type.FLYING) || isFlying.containsKey(player)) return;

        if (CheckUtils.isOnGround(player)) return;

        if (!PlayerTasks.lastOnGround.containsKey(player)) return;

        if (AntiSpeedListener.teleports.contains(player.getName())) return;

        if (player.getInventory().getChestplate().getId() == ItemID.ELYTRA || player.getInventory().getItemInHand().getId() == ItemID.TRIDENT) return;

        if (PlayerTasks.lastOnGround.get(player).getY() < player.getY() - 3.5) {
            this.plugin.flag("FlightB", player);

            if (checks.containsKey(player.getName())){
                checks.put(player.getName(), checks.get(player.getName()) + 1);
            }else{
                checks.put(player.getName(), 1);
            }

            if (checks.get(player.getName()) > 7){
                Revolutionarity.banPlayer(player, "Flight");
                checks.remove(player.getName());
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (!this.plugin.getSettings().isFlight()) return;

        Player player = event.getPlayer();

        if (player.hasPermission("revolutionarity.bypass.flight") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        if (!player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT) && player.getAdventureSettings().get(AdventureSettings.Type.FLYING)) {
            player.getAdventureSettings().set(AdventureSettings.Type.FLYING, false);
            isFlying.remove(player);
            isFlying.put(player, player.getAdventureSettings().get(AdventureSettings.Type.FLYING));
            this.plugin.flag("FlightC", player);
        }
    }

    public static boolean isFlying(Player player) {
        if (!isFlying.containsKey(player)) return false;
        return isFlying.get(player);
    }
}
