package de.mariocst.revolutionarity.checks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector2;
import cn.nukkit.scheduler.PluginTask;
import de.mariocst.revolutionarity.Revolutionarity;

import java.util.HashMap;

public class AntiSpeed extends PluginTask<Revolutionarity> {

    public static HashMap<String, Position> positions = new HashMap<>();

    public static final HashMap<String, Integer> checks = new HashMap<>();

    public AntiSpeed(Revolutionarity owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        if (!this.getOwner().getSettings().isSpeed()) return;

        HashMap<String, Position> newPositions = new HashMap<>();

        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (player.hasPermission("revolutionarity.bypass.speed") ||
                    player.hasPermission("revolutionarity.bypass.*") ||
                    player.hasPermission("revolutionarity.*") ||
                    player.hasPermission("*") ||
                    player.isOp()) return;

            if (positions.containsKey(player.getName())){
                Position pos = positions.get(player.getName());

                if (AntiSpeedListener.teleports.contains(player.getName())){
                    AntiSpeedListener.teleports.remove(player.getName());
                    newPositions.put(player.getName(), player.getPosition());
                    continue;
                }

                double maxSpeed = 35;
                double dist = new Vector2(pos.getFloorX(), pos.getFloorZ()).distance(new Vector2(player.getPosition().getFloorX(), player.getPosition().getFloorZ()));

                if (dist == 0) return;

                if (pos.getLevel().getName().equalsIgnoreCase(player.getLevel().getName())
                        && player.getGamemode() == 0
                        && dist > maxSpeed
                        && !Flight.isFlying(player)
                        && !isRider(player)
                        && !(player.getInventory().getChestplate().getId() == ItemID.ELYTRA || player.getInventory().getItemInHand().getId() == ItemID.TRIDENT))
                {
                    this.getOwner().flag("Speed", "Speed: " + Math.round(dist/3) + " blocks in sec, max: " + Math.round(maxSpeed/3) , player);

                    if (checks.containsKey(player.getName())){
                        checks.put(player.getName(), checks.get(player.getName()) + 1);
                    }else{
                        checks.put(player.getName(), 1);
                    }

                    if (checks.get(player.getName()) > 2){
                        Revolutionarity.banPlayer(player, "Speed");
                        checks.remove(player.getName());
                    }
                }
            }

            newPositions.put(player.getName(), player.getPosition());
        }

        positions = newPositions;
    }

    public boolean isRider(Player player){
        for (Entity entity : player.getChunk().getEntities().values()) {
            if (entity instanceof EntityRideable) {
                if (entity.getPassenger() instanceof Player){
                    if (entity.getPassenger().getName().equalsIgnoreCase(player.getName())){
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
