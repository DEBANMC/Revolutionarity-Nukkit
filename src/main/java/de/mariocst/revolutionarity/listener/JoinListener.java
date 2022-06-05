package de.mariocst.revolutionarity.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.utils.FakePlayer;
import de.mariocst.revolutionarity.utils.Util;

import java.util.HashMap;

public class JoinListener implements Listener {
    private final Revolutionarity plugin;
    
    public JoinListener(Revolutionarity plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getLoginChainData().getDeviceOS() == 1) { // Android
            if (!this.plugin.getSettings().isToolBox()) return;

            if (player.hasPermission("revolutionarity.bypass.toolbox") ||
                    player.hasPermission("revolutionarity.bypass.*") ||
                    player.hasPermission("revolutionarity.*") ||
                    player.hasPermission("*") ||
                    player.isOp()) return;

            if (player.getLoginChainData().getDeviceModel().contains("nokia")) {
                if (player.getLoginChainData().getDeviceModel().equals(player.getLoginChainData().getDeviceModel().toLowerCase())) {
                    this.plugin.flag("ToolBox", "Device model: " + player.getLoginChainData().getDeviceModel(), player);
                    this.plugin.getSettings().velo.remove(player);
                    this.plugin.getSettings().velo.put(player, this.plugin.getSettings().getMaxVelo());
                    //Revolutionarity.banPlayer(player, "ToolBox");
                }
            }
            else {
                if (!player.getLoginChainData().getDeviceModel().equals(player.getLoginChainData().getDeviceModel().toUpperCase())) {
                    this.plugin.flag("ToolBox", "Device model: " + player.getLoginChainData().getDeviceModel(), player);
                    this.plugin.getSettings().velo.remove(player);
                    this.plugin.getSettings().velo.put(player, this.plugin.getSettings().getMaxVelo());
                    //Revolutionarity.banPlayer(player, "ToolBox");
                }
            }
        }

        if ((player.getLoginChainData().getDeviceOS() == 1 || player.getLoginChainData().getDeviceOS() == 2) && player.getLoginChainData().getDeviceModel().equals("")) { // Android, iOS
            if (!this.plugin.getSettings().isEditionFaker()) return;

            if (player.hasPermission("revolutionarity.bypass.editionfaker") ||
                    player.hasPermission("revolutionarity.bypass.*") ||
                    player.hasPermission("revolutionarity.*") ||
                    player.hasPermission("*") ||
                    player.isOp()) return;

            this.plugin.flag("EditionFaker", player);
            this.plugin.getSettings().velo.remove(player);
            this.plugin.getSettings().velo.put(player, this.plugin.getSettings().getMaxVelo());
            Revolutionarity.banPlayer(player, "EditionFaker");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        for(HashMap<String, Integer> map : Revolutionarity.getChecks()){
            map.remove(player.getName());
        }
    }
}
