package de.mariocst.revolutionarity.checks;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.listener.PacketListener;

import java.util.HashMap;

public class NoSwing implements Listener {
    private final Revolutionarity plugin;

    public static final HashMap<String, Integer> checks = new HashMap<>();

    public NoSwing(Revolutionarity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        if (!this.plugin.getSettings().isNoSwing()) return;

        if (!(event.getPacket() instanceof InventoryTransactionPacket)) return;

        Player player = event.getPlayer();

        if (player.hasPermission("revolutionarity.bypass.noswing") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        InventoryTransactionPacket packet = (InventoryTransactionPacket) event.getPacket();

        if (packet.transactionType != 3) return;

        if (!PacketListener.containsAnimatePacket(player)) {
            event.setCancelled(true);
            this.plugin.flag("NoSwing", player);

            /*if (checks.containsKey(player.getName())){
                checks.put(player.getName(), checks.get(player.getName()) + 1);
            }else{
                checks.put(player.getName(), 1);
            }

            if (checks.get(player.getName()) > 8){
                Revolutionarity.banPlayer(player);
            }*/
        }
    }
}
