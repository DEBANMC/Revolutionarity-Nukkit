package de.mariocst.revolutionarity;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandMap;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import de.mariocst.revolutionarity.checks.*;
import de.mariocst.revolutionarity.commands.*;
import de.mariocst.revolutionarity.config.*;
import de.mariocst.revolutionarity.forms.ReportForm;
import de.mariocst.revolutionarity.listener.FreezeEventListener;
import de.mariocst.revolutionarity.listener.JoinListener;
import de.mariocst.revolutionarity.listener.PacketListener;
import de.mariocst.revolutionarity.listener.PlayerTasks;
import de.mariocst.revolutionarity.logging.Logger;
import de.mariocst.revolutionarity.tasks.BanTask;
import de.mariocst.revolutionarity.tasks.ClearChecks;
import de.mariocst.revolutionarity.tasks.KickTask;
import de.mariocst.revolutionarity.utils.FakePlayer;
import de.mariocst.revolutionarity.utils.Util;
import de.mariocst.revolutionarity.webhook.DiscordWebhook;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Revolutionarity extends PluginBase {
    @Getter
    @Setter
    private String prefix;

    @Getter
    private PluginSettings pluginSettings;

    @Getter
    private Settings settings;

    @Getter
    private ReportForm reportForm;

    public final HashMap<Player, Location> frozen = new HashMap<>();

    private Logger acLogger;

    public static ArrayList<HashMap<String, Integer>> checks = new ArrayList<>();

    private static Revolutionarity instance;

    public static Revolutionarity getInstance(){
        return instance;
    }

    public static ArrayList<String> banned = new ArrayList<>();

    @Override
    public void onEnable() {
        this.loadConfigs();
        this.register();

        instance = this;

        this.acLogger = new Logger(this);

        if (this.pluginSettings.getDiscordWebhookLink().equals("")) this.warning("No discord webhook link entered!");

        this.log("Revolutionarity AntiCheat loaded on version " + this.getDescription().getVersion() + "!");

        checks.add(Flight.checks);
        checks.add(Glide.checks);
        checks.add(AirJump.checks);
        checks.add(KillAuraBot.checks);
        checks.add(AntiSpeed.checks);
    }

    public static ArrayList <HashMap<String, Integer>> getChecks(){
        return checks;
    }

    @Override
    public void onDisable() {
        this.saveConfigs();

        this.log("Revolutionarity AntiCheat unloaded!");

        for (FakePlayer fpl : KillAuraBot.bots.values()){
            fpl.despawnFromAll();
            fpl.close();
        }
    }

    public void log(String msg) {
        this.getLogger().info(this.prefix + msg);
    }

    public void warning(String msg) {
        this.getLogger().warning(this.prefix + msg);
    }

    public void loadConfigs() {
        Config pS = new Config(this.getDataFolder() + "/pluginSettings.yml", Config.YAML);
        this.pluginSettings = new PluginSettings(this, pS.getRootSection());

        Config s = new Config(this.getDataFolder() + "/settings.yml", Config.YAML);
        this.settings = new Settings(this, s.getRootSection());
    }

    public void saveConfigs() {
        this.pluginSettings.save();
        this.settings.save();
    }

    private void register() {
        CommandMap map = this.getServer().getCommandMap();

        map.register("freeze", new FreezeCommand(this));
        map.register("report", new ReportCommand(this));
        map.register("revolutionarity", new RevolutionaryCommand(this));

        PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new AirJump(this), this);
        manager.registerEvents(new BlockReach(this), this);
        manager.registerEvents(new Flight(this), this);
        manager.registerEvents(new Glide(this), this);
        manager.registerEvents(new KillAuraBot(this), this);
        manager.registerEvents(new Reach(this), this);
        manager.registerEvents(new SelfHit(this), this);

        //manager.registerEvents(new AntiImmobile(this), this);
        //manager.registerEvents(new Step(this), this);
        //manager.registerEvents(new KillAura(this), this);
        //manager.registerEvents(new NoSwing(this), this);

        manager.registerEvents(new JoinListener(this), this);
        //manager.registerEvents(new PacketListener(), this);

        ServerScheduler scheduler = this.getServer().getScheduler();

        //scheduler.scheduleRepeatingTask(this, new FreezeEventListener(this), 2);
        scheduler.scheduleRepeatingTask(this, new PlayerTasks(this), 10);
        //scheduler.scheduleRepeatingTask(this, new Speed(this), 4);

        scheduler.scheduleRepeatingTask(this, new AntiSpeed(this), 60);
        manager.registerEvents(new AntiSpeedListener(), this);

        scheduler.scheduleRepeatingTask(this, new ClearChecks(this), 900);

        this.reportForm = new ReportForm();

        Util.setupSkinStream();
    }

    public void sendReport(Player player, IPlayer reported, String reason) throws IOException {
        if (this.pluginSettings.getDiscordWebhookLink().equals("")) return;

        DiscordWebhook webhook = new DiscordWebhook(this.pluginSettings.getDiscordWebhookLink());

        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Report")
                .setDescription("Someone got reported")
                .addField("Player", player.getName(), false)
                .addField("Reported", reported.getName(), false)
                .addField("Reason", reason, false)
                .setColor(Color.RED));

        try {
            webhook.execute();
        }
        catch (IOException e) {
            this.getLogger().error(e.getLocalizedMessage());
        }
    }

    public void flag(String check, Player flagged) {
        this.flag(check, "", flagged);
    }

    public void flag(String check, String details, Player flagged) {
        String dtls = details.equals("") ? "" : " Details: " + details;

        this.warning("The player " + flagged.getName() + " got flagged for " + check + "!" + dtls);
        this.acLogger.log(flagged, check, details);

        double velo = 0.0;

        if (this.settings.velo.containsKey(flagged)) {
            velo = this.settings.velo.get(flagged);
            this.settings.velo.remove(flagged);
        }

        velo += 1.0;

        this.settings.velo.put(flagged, velo);

        for (Player player : this.getServer().getOnlinePlayers().values()) {
            if (player.hasPermission("revolutionarity.staff") || player.hasPermission("revolutionarity.*") || player.hasPermission("*") || player.isOp()) {
                player.sendMessage(this.getPrefix() + "The player " + flagged.getName() + " got flagged for " + check + "!" + dtls);
            }
        }

        /*if (velo >= this.settings.getMaxVelo()) {
            this.settings.velo.remove(flagged);
            banPlayer(flagged);
            //flagged.kick(this.pluginSettings.getKickMessage().replaceAll("%newline%", "\n"), false);
            this.acLogger.log("Player " + flagged.getName() + " got kicked");

            for (Player player : this.getServer().getOnlinePlayers().values()) {
                if (player.hasPermission("revolutionarity.staff") || player.hasPermission("revolutionarity.*") || player.hasPermission("*") || player.isOp()) {
                    player.sendMessage(this.getPrefix() + "The player " + flagged.getName() + " got kicked for cheating!");
                }
            }
        }*/

        if (this.pluginSettings.getDiscordWebhookLink().equals("")) return;

        DiscordWebhook webhook = new DiscordWebhook(this.pluginSettings.getDiscordWebhookLink());

        if (details.equals("")) {
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("AntiCheat")
                    .setDescription("A player got flagged!")
                    .addField("Player", flagged.getName(), false)
                    .addField("Check", check, false)
                    .addField("DeviceOS", this.getDeviceName(flagged), false)
                    .setColor(Color.RED));
        }
        else {
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("AntiCheat")
                    .setDescription("A player got flagged!")
                    .addField("Player", flagged.getName(), false)
                    .addField("Check", check, false)
                    .addField("Details", details, false)
                    .addField("DeviceOS", this.getDeviceName(flagged), false)
                    .setColor(Color.RED));
        }

        try {
            webhook.execute();
        }
        catch (IOException e) {
            this.getLogger().error(e.getLocalizedMessage());
        }

        if (velo >= this.settings.getMaxVelo()) {
            DiscordWebhook webhook2 = new DiscordWebhook(this.pluginSettings.getDiscordWebhookLink());

            webhook2.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("AntiCheat")
                    .setDescription("A player got kicked for cheating!")
                    .addField("Player", flagged.getName(), false)
                    .setColor(Color.RED));

            try {
                webhook2.execute();
            }
            catch (IOException e) {
                this.getLogger().error(e.getLocalizedMessage());
            }
        }
    }

    public static void banPlayer(Player player, String reason){
        if (!banned.contains(player.getName())){
            //Server.getInstance().getScheduler().scheduleDelayedTask(new BanTask(Revolutionarity.getInstance(), player), 5);
            Server.getInstance().getScheduler().scheduleDelayedTask(new KickTask(Revolutionarity.getInstance(), player, reason), 5);
            Server.getInstance().getLogger().info("Player §b" + player.getName() + " §rwas banned, reason: " + reason);
            banned.add(player.getName());
        }
    }

    private String getDeviceName(Player p) {
        return String.valueOf(p.getLoginChainData().getDeviceOS())
                .replace("10", "TV OS")
                .replace("11", "PlayStation")
                .replace("12", "Switch")
                .replace("13", "XBOX")
                .replace("0", "Unknown")
                .replace("1", "Android")
                .replace("2", "iOS")
                .replace("3", "MacOS")
                .replace("4", "FireOS")
                .replace("5", "GearVR")
                .replace("6", "HoloLens")
                .replace("7", "Windows")
                .replace("8", "Windows 86x")
                .replace("9", "Dedicated");
    }
}
