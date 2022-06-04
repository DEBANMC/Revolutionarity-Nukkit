package de.mariocst.revolutionarity.checks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Location;
import de.mariocst.revolutionarity.Revolutionarity;
import de.mariocst.revolutionarity.tasks.TeleportTask;
import de.mariocst.revolutionarity.utils.CheckUtils;
import de.mariocst.revolutionarity.utils.FakePlayer;
import de.mariocst.revolutionarity.utils.Util;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

public class KillAuraBot implements Listener {
    private final Revolutionarity plugin;

    public static HashMap<String, FakePlayer> bots = new HashMap<>();

    public static ArrayList<Long> waitTeleport = new ArrayList<>();

    public static final HashMap<String, Integer> checks = new HashMap<>();

    public KillAuraBot(Revolutionarity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!this.plugin.getSettings().isKillAura()) return;

        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        if (player.hasPermission("revolutionarity.bypass.killaura") ||
                player.hasPermission("revolutionarity.bypass.*") ||
                player.hasPermission("revolutionarity.*") ||
                player.hasPermission("*") ||
                player.isOp()) return;

        if (!(event.getEntity() instanceof FakePlayer)) {
            bots.get(player.getName()).teleport(player.getPosition().add(0.5, 2.5, 0.5));

            if(!waitTeleport.contains(bots.get(player.getName()).getId())){
                Server.getInstance().getScheduler().scheduleDelayedTask(new TeleportTask(Revolutionarity.getInstance(), bots.get(player.getName()), bots.get(player.getName()).getPosition().add(0, 400)), 15);
                waitTeleport.add(bots.get(player.getName()).getId());
            }
            return;
        }

        FakePlayer fplayer = (FakePlayer) event.getEntity();

        if(!waitTeleport.contains(fplayer.getId())){
            Server.getInstance().getScheduler().scheduleDelayedTask(new TeleportTask(Revolutionarity.getInstance(), fplayer, fplayer.getPosition().add(0, 400)), 65);
            waitTeleport.add(fplayer.getId());
        }

        this.plugin.flag("KillAura", "Bot of: " + player.getName(), player);

        if (checks.containsKey(player.getName())){
            checks.put(player.getName(), checks.get(player.getName()) + 1);
        }else{
            checks.put(player.getName(), 1);
        }

        if (checks.get(player.getName()) > 4){
            Revolutionarity.banPlayer(player);
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        FakePlayer fplayer = new FakePlayer(player.getPosition().add(0, 3), Util.skinArray);
        fplayer.spawnFakePlayer(player);

        if(!waitTeleport.contains(fplayer.getId())){
            Server.getInstance().getScheduler().scheduleDelayedTask(new TeleportTask(Revolutionarity.getInstance(), fplayer, fplayer.getPosition().add(0, 400)), 65);
            waitTeleport.add(fplayer.getId());
        }

        bots.put(player.getName(), fplayer);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (bots.containsKey(player.getName()) && bots.get(player.getName()) != null){
            bots.get(player.getName()).despawnFromAll();
            bots.get(player.getName()).close();

            bots.remove(player.getName());
        }
    }

    @EventHandler
    public void onEntDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof FakePlayer){
            event.setCancelled();
        }
    }
}
