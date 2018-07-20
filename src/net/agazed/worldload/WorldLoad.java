package net.agazed.worldload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WorldLoad extends JavaPlugin {
    private static Logger logger = Logger.getLogger("minecraft");

    List<String> worldlist = new ArrayList<String>();
    List<String> worldlistloaded = new ArrayList<String>();
    List<String> worldlistunloaded = new ArrayList<String>();
    Map<String,GameMode> world_gamemodes = new HashMap<String,GameMode>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        load();
    }
    public void onDisable() {
        this.worldlist.clear();
        this.worldlistloaded.clear();
        this.worldlistunloaded.clear();
        this.world_gamemodes.clear();
    }

    public static void log(String s) {
        WorldLoad.logger.log(Level.INFO, s);
    }
    public static void warn(String s) {
        WorldLoad.logger.log(Level.WARNING, String.format("%s%s", ChatColor.RED, s));
    }
    public static void senderLog(CommandSender sender, Level level, String s) {
        if (level == Level.INFO) {
            s = String.format("%s%s", ChatColor.GREEN, s);
        } else if (level == Level.WARNING) {
            s = String.format("%s%s", ChatColor.RED, s);
        }
        sender.sendMessage(s);
    }

    public void load() {
        if (getConfig().getConfigurationSection("worlds") != null) {
            for (String world_name : getConfig().getConfigurationSection("worlds").getKeys(false)) {
                getLogger().info(String.format("Preparing level \"%s\"", world_name));
                String w = "worlds." + world_name;

                String type = getConfig().getString(w + ".type");
                String environment = getConfig().getString(w + ".environment");
                Boolean pvp = getConfig().getBoolean(w + ".pvp");
                String difficulty = getConfig().getString(w + ".difficulty");
                String gamemode = getConfig().getString(w + ".gamemode");
                Long seed = getConfig().getLong(w + ".seed");
                Boolean structures = getConfig().getBoolean(w + ".generate-structures");
                Boolean animals = getConfig().getBoolean(w + ".spawn-animals");
                Boolean monsters = getConfig().getBoolean(w + ".spawn-monsters");
                
                WorldCreator wc = (new WorldCreator(world_name))
                    .type(WorldType.valueOf(type))
                    .environment(Environment.valueOf(environment))
                    .seed(seed)
                    .generateStructures(structures);
                World world = wc.createWorld();
                if (world == null) {
                    continue;
                }
                
                world.setPVP(pvp);
                world.setDifficulty(Difficulty.valueOf(difficulty));
                world.setSpawnFlags(monsters, animals);
                
                if (!worldlist.contains(world_name)) {
                    world_gamemodes.put(world_name, GameMode.valueOf(gamemode));
                    worldlist.add(world_name);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // WorldLoad Help

        if (cmd.getName().equalsIgnoreCase("worldload")) {
            if ((args.length == 0)||(args[0].equalsIgnoreCase("help"))) {
                if (!sender.hasPermission("worldload.help")) {
                    senderLog(sender, Level.WARNING, "No permission!");
                    return true;
                }

                ChatColor aq = ChatColor.DARK_AQUA;
                ChatColor bold = ChatColor.BOLD;
                ChatColor gr = ChatColor.GRAY;
                ChatColor wh = ChatColor.WHITE;

                sender.sendMessage(String.format("-----%s%s WorldLoad Help %s-----", aq, bold, wh));
                sender.sendMessage(String.format("%s/worldload %shelp %s- Displays this page", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %stp <world> %s- Teleport to a world", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %screate <world> [-flat] %s- Create a world", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %sremove <world> %s- Remove a world from the config", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %sload <world> %s- Load a world one time", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %sunload <world> %s- Unload a world one time", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %slist %s- List your worlds", aq, gr, wh));
                sender.sendMessage(String.format("%s/worldload %sreload %s- Reload the config", aq, gr, wh));

                return true;
            }
        }

        // WorldLoad TP

        if (args[0].equalsIgnoreCase("tp")) {
            if (!(sender instanceof Player)) {
                //getServer().getConsoleSender().sendMessage(ChatColor.RED + "Command can only be run as a player!");
                warn("Command can only be run as a player!");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("worldload.tp")) {
                senderLog(player, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                senderLog(player, Level.WARNING, "Correct usage: /worldload tp <world>");
                return true;
            }

            String world_name = args[1];
            if (getServer().getWorld(world_name) == null) {
                if (worldlistunloaded.contains(world_name)) {
                    senderLog(player, Level.WARNING, "World is unloaded!");
                    return true;
                }
                senderLog(player, Level.WARNING, "World does not exist!");
                return true;
            }
            player.teleport(getServer().getWorld(world_name).getSpawnLocation());

            GameMode gm = world_gamemodes.get(world_name);
            if (gm == null) {
            	gm = GameMode.SURVIVAL;
            }
            if ((player.getGameMode() == GameMode.CREATIVE)&&(gm != GameMode.CREATIVE)) {
            	player.getInventory().clear();
            }
            player.setGameMode(gm);

            senderLog(player, Level.INFO, String.format("Teleported to world \"%s\"", world_name));
            return true;
        }

        // WorldLoad Create

        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("worldload.create")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                senderLog(sender, Level.WARNING, "Correct usage: /worldload create <world>");
                return true;
            }

            String world_name = args[1];
            if (
                (worldlist.contains(world_name))
                ||(getServer().getWorld(world_name) == getServer().getWorlds().get(0))
            ) {
                senderLog(sender, Level.WARNING, "World already exists!");
                return true;
            }

            boolean is_flat = false;
            if ((args.length > 2)&&(args[2].equalsIgnoreCase("-flat"))) {
                is_flat = true;
            }

            if (worldlistloaded.contains(world_name)) {
                worldlistloaded.remove(world_name);
            }
            if (worldlistunloaded.contains(world_name)) {
                worldlistunloaded.remove(world_name);
            }

            senderLog(sender, Level.INFO, String.format("Preparing level \"%s\"", world_name));
            if (!worldlist.contains(world_name)) {
                worldlist.add(world_name);
            }

            String w = "worlds." + world_name;
            if (is_flat) {
                getConfig().set(w + ".type", "FLAT");
            } else {
                getConfig().set(w + ".type", "NORMAL");
            }
            getConfig().set(w + ".environment", "NORMAL");
            getConfig().set(w + ".pvp", "true");
            getConfig().set(w + ".difficulty", "NORMAL");
            getConfig().set(w + ".seed", "");
            getConfig().set(w + ".generate-structures", "true");
            getConfig().set(w + ".spawn-animals", "true");
            getConfig().set(w + ".spawn-monsters", "true");
            saveConfig();

            WorldCreator wc = new WorldCreator(world_name);
            if (is_flat) {
                wc.type(WorldType.FLAT);
            }
            wc.createWorld();
            senderLog(sender, Level.INFO, String.format("Successfully created world \"%s\"", world_name));
            return true;
        }

        // WorldLoad Remove

        if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("worldload.remove")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                senderLog(sender, Level.WARNING, "Correct usage: /worldload remove <world>");
                return true;
            }

            String world_name = args[1];
            if (!worldlist.contains(world_name)) {
                senderLog(sender, Level.WARNING, "World does not exist!");
                return true;
            }

            worldlist.remove(world_name);
            getConfig().set("worlds." + world_name, null);
            saveConfig();
            if (getServer().getWorld(world_name) == null) {
                senderLog(sender, Level.INFO, String.format("Successfully removed unloaded world \"%s\" from the world list.", world_name));
                return true;
            }
            worldlistloaded.add(world_name);
            senderLog(sender, Level.INFO, String.format("Successfully removed world \"%s\" from the world list.", world_name));
            return true;
        }

        // WorldLoad Load

        if (args[0].equalsIgnoreCase("load")) {
            if (!sender.hasPermission("worldload.load")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                senderLog(sender, Level.WARNING, "Correct usage: /worldload load <world>");
                return true;
            }

            String world_name = args[1];
            if (
                (worldlist.contains(world_name))
                ||(worldlistloaded.contains(world_name))
                ||(getServer().getWorld(world_name) == getServer().getWorlds().get(0))
            ) {
                senderLog(sender, Level.WARNING, "World already exists!");
                return true;
            }

            if (worldlistunloaded.contains(world_name)) {
                worldlistunloaded.remove(world_name);
            }

            worldlistloaded.add(world_name);
            senderLog(sender, Level.INFO, String.format("Loading level \"%s\"", world_name));
            new WorldCreator(args[1]).createWorld();
            senderLog(sender, Level.INFO, String.format("Successfully loaded world \"%s\"", world_name));
            return true;
        }

        // WorldLoad Unload

        if (args[0].equalsIgnoreCase("unload")) {
            if (!sender.hasPermission("worldload.unload")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                senderLog(sender, Level.WARNING, "Correct usage: /worldload unload <world>");
                return true;
            }

            String world_name = args[1];
            if (worldlistunloaded.contains(world_name)) {
                senderLog(sender, Level.WARNING, "World is already unloaded!");
                return true;
            }
            if (getServer().getWorld(world_name) == null) {
                senderLog(sender, Level.WARNING, "World does not exist!");
                return true;
            }
            if (getServer().getWorld(world_name) == getServer().getWorlds().get(0)) {
                senderLog(sender, Level.WARNING, "Cannot unload the main world!");
                return true;
            }
            if (getServer().getWorld(world_name).getPlayers().size() > 0) {
                senderLog(sender, Level.WARNING, "Cannot unload a world with players inside!");
                return true;
            }

            if (worldlist.contains(world_name)) {
                worldlist.remove(world_name);
            }
            if (worldlistloaded.contains(world_name)) {
                worldlistloaded.remove(world_name);
            }

            if (!worldlistunloaded.contains(world_name)) {
                worldlistunloaded.add(world_name);
            }
            getServer().unloadWorld(world_name, true);
            senderLog(sender, Level.INFO, String.format("Successfully unloaded world \"%s\"", world_name));
            return true;
        }

        // WorldLoad List

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("worldload.list")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                ChatColor aq = ChatColor.DARK_AQUA;
                ChatColor bold = ChatColor.BOLD;
                ChatColor gr = ChatColor.GRAY;
                ChatColor wh = ChatColor.WHITE;

                sender.sendMessage(String.format("----- %s%sWorld List %s-----", aq, bold, wh));
                sender.sendMessage(String.format("%sCreated: %s%s", aq, gr, worldlist));
                sender.sendMessage(String.format("%sLoaded: %s%s", aq, gr, worldlistloaded));
                sender.sendMessage(String.format("%sUnloaded: %s%s", aq, gr, worldlistunloaded));
                return true;
            }
        }

        // WorldLoad Reload

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("worldload.reload")) {
                senderLog(sender, Level.WARNING, "No permission!");
                return true;
            }

            if (args.length == 1) {
                reloadConfig();
                load();
                senderLog(sender, Level.INFO, "Successfully reloaded config!");
                return true;
            }
        }

        senderLog(sender, Level.WARNING, "Unknown argument!");
        return true;
    }
}
