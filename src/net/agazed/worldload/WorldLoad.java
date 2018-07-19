package net.agazed.worldload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WorldLoad extends JavaPlugin {
    List<String> worldlist = new ArrayList<String>();
    List<String> worldlistloaded = new ArrayList<String>();
    List<String> worldlistunloaded = new ArrayList<String>();
    Map<String,GameMode> world_gamemodes = new HashMap<String,GameMode>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (getConfig().getConfigurationSection("worlds") != null) {
            for (String world : getConfig().getConfigurationSection("worlds").getKeys(false)) {
                getLogger().info("Preparing level \"" + world + "\"");
                
                String type = getConfig().getString("worlds." + world + ".type");
                String environment = getConfig().getString("worlds." + world + ".environment");
                Boolean pvp = getConfig().getBoolean("worlds." + world + ".pvp");
                String difficulty = getConfig().getString("worlds." + world + ".difficulty");
                String gamemode = getConfig().getString("worlds." + world + ".gamemode");
                Long seed = getConfig().getLong("worlds." + world + ".seed");
                Boolean structures = getConfig().getBoolean("worlds." + world + ".generate-structures");
                Boolean animals = getConfig().getBoolean("worlds." + world + ".spawn-animals");
                Boolean monsters = getConfig().getBoolean("worlds." + world + ".spawn-monsters");
                
                new WorldCreator(world).type(WorldType.valueOf(type)).environment(Environment.valueOf(environment))
                        .seed(seed).generateStructures(structures).createWorld();
                getServer().getWorld(world).setPVP(pvp);
                getServer().getWorld(world).setDifficulty(Difficulty.valueOf(difficulty));
                getServer().getWorld(world).setSpawnFlags(monsters, animals);
                
                if (!worldlist.contains(world)) {
                	world_gamemodes.put(world, GameMode.valueOf(gamemode));
                	worldlist.add(world);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // WorldLoad Help

        if (cmd.getName().equalsIgnoreCase("worldload")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                if (!sender.hasPermission("worldload.help")) {
                    sender.sendMessage(ChatColor.RED + "No permission!");
                    return true;
                }
                sender.sendMessage("----- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "WorldLoad Help " + ChatColor.WHITE
                        + "-----");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "help " + ChatColor.WHITE
                        + "- Displays this page");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "tp <world> "
                        + ChatColor.WHITE + "- Teleport to a world");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "create <world> [-flat] "
                        + ChatColor.WHITE + "- Create a world");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "remove <world> "
                        + ChatColor.WHITE + "- Remove a world from the config");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "delete <world> "
                        + ChatColor.WHITE + "- Permanently delete a world's files");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "load <world> "
                        + ChatColor.WHITE + "- Load a world one time");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "unload <world> "
                        + ChatColor.WHITE + "- Unload a world one time");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "stats [world] "
                        + ChatColor.WHITE + "- Get world statistics");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "list " + ChatColor.WHITE
                        + "- List your worlds");
                sender.sendMessage(ChatColor.DARK_AQUA + "/worldload " + ChatColor.GRAY + "reload " + ChatColor.WHITE
                        + "- Reload the config");
                return true;
            }
        }

        // WorldLoad TP

        if (args[0].equalsIgnoreCase("tp")) {
            if (!(sender instanceof Player)) {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "Command can only be run as a player!");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("worldload.tp")) {
                player.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                player.sendMessage(ChatColor.RED + "Correct usage: /worldload tp <world>");
                return true;
            }
            if (getServer().getWorld(args[1]) == null) {
                if (worldlistunloaded.contains(args[1])) {
                    player.sendMessage(ChatColor.RED + "World is unloaded!");
                    return true;
                }
                player.sendMessage(ChatColor.RED + "World does not exist!");
                return true;
            }
            player.teleport(getServer().getWorld(args[1]).getSpawnLocation());
            
            GameMode gm = world_gamemodes.get(args[1]);
            if (gm == null) {
            	gm = GameMode.SURVIVAL;
            }
            if ((player.getGameMode() == GameMode.CREATIVE)&&(gm != GameMode.CREATIVE)) {
            	player.getInventory().clear();
            }
            player.setGameMode(gm);
            
            player.sendMessage(ChatColor.GREEN + "Teleported to world \"" + args[1] + "\"");
            return true;
        }

        // WorldLoad Create

        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("worldload.create")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /worldload create <world>");
                return true;
            }
            if (args.length == 2) {
                if (worldlist.contains(args[1]) || getServer().getWorld(args[1]) == getServer().getWorlds().get(0)) {
                    sender.sendMessage(ChatColor.RED + "World already exists!");
                    return true;
                }
                if (worldlistloaded.contains(args[1]))
                    worldlistloaded.remove(args[1]);
                if (worldlistunloaded.contains(args[1]))
                    worldlistunloaded.remove(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Preparing level \"" + args[1] + "\"");
                if (!worldlist.contains(args[1]))
                    worldlist.add(args[1]);
                getConfig().set("worlds." + args[1] + ".type", "NORMAL");
                getConfig().set("worlds." + args[1] + ".environment", "NORMAL");
                getConfig().set("worlds." + args[1] + ".pvp", "true");
                getConfig().set("worlds." + args[1] + ".difficulty", "NORMAL");
                getConfig().set("worlds." + args[1] + ".seed", "");
                getConfig().set("worlds." + args[1] + ".generate-structures", "true");
                getConfig().set("worlds." + args[1] + ".spawn-animals", "true");
                getConfig().set("worlds." + args[1] + ".spawn-monsters", "true");
                saveConfig();
                new WorldCreator(args[1]).createWorld();
                sender.sendMessage(ChatColor.GREEN + "Successfully created world \"" + args[1] + "\"");
                return true;
            }
            if (args[2].equalsIgnoreCase("-flat")) {
                if (worldlist.contains(args[1]) || getServer().getWorld(args[1]) == getServer().getWorlds().get(0)) {
                    sender.sendMessage(ChatColor.RED + "World already exists!");
                    return true;
                }
                if (worldlistloaded.contains(args[1]))
                    worldlistloaded.remove(args[1]);
                if (worldlistunloaded.contains(args[1]))
                    worldlistunloaded.remove(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Preparing flat level \"" + args[1] + "\"");
                if (!worldlist.contains(args[1]))
                    worldlist.add(args[1]);
                getConfig().set("worlds." + args[1] + ".type", "FLAT");
                getConfig().set("worlds." + args[1] + ".environment", "NORMAL");
                getConfig().set("worlds." + args[1] + ".pvp", "true");
                getConfig().set("worlds." + args[1] + ".difficulty", "NORMAL");
                getConfig().set("worlds." + args[1] + ".seed", "");
                getConfig().set("worlds." + args[1] + ".generate-structures", "true");
                getConfig().set("worlds." + args[1] + ".spawn-animals", "true");
                getConfig().set("worlds." + args[1] + ".spawn-monsters", "true");
                saveConfig();
                new WorldCreator(args[1]).type(WorldType.FLAT).createWorld();
                sender.sendMessage(ChatColor.GREEN + "Successfully created flat world \"" + args[1] + "\"");
                return true;
            }
        }

        // WorldLoad Remove

        if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("worldload.remove")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /worldload remove <world>");
                return true;
            }
            if (!worldlist.contains(args[1])) {
                sender.sendMessage(ChatColor.RED + "World does not exist!");
                return true;
            }
            if (worldlist.contains(args[1])) {
                worldlist.remove(args[1]);
                getConfig().set("worlds." + args[1], null);
                saveConfig();
                if (getServer().getWorld(args[1]) == null) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully removed unloaded world \"" + args[1]
                            + "\" from the world list.");
                    return true;
                }
                worldlistloaded.add(args[1]);
                sender.sendMessage(
                        ChatColor.GREEN + "Successfully removed world \"" + args[1] + "\" from the world list.");
                return true;
            }
        }

        // WorldLoad Delete

        if (args[0].equalsIgnoreCase("delete")) {
            if (!sender.hasPermission("worldload.delete")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /worldload delete <world>");
                return true;
            }
            if (getServer().getWorld(args[1]) == null) {
                File unloaded = new File(args[1]);
                if (!unloaded.exists()) {
                    sender.sendMessage(ChatColor.RED + "World does not exist!");
                    return true;
                }
                if (worldlist.contains(args[1]))
                    worldlist.remove(args[1]);
                if (worldlistloaded.contains(args[1]))
                    worldlistloaded.remove(args[1]);
                if (worldlistunloaded.contains(args[1]))
                    worldlistunloaded.remove(args[1]);
                delete(unloaded);
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted unloaded world \"" + args[1] + "\"");
                return true;
            }
            if (getServer().getWorld(args[1]) == getServer().getWorlds().get(0)) {
                sender.sendMessage(ChatColor.RED + "Cannot delete the main world!");
                return true;
            }
            if (getServer().getWorld(args[1]).getPlayers().size() > 0) {
                sender.sendMessage(ChatColor.RED + "Cannot delete a world with players inside!");
                return true;
            }
            if (worldlist.contains(args[1]))
                worldlist.remove(args[1]);
            if (worldlistloaded.contains(args[1]))
                worldlistloaded.remove(args[1]);
            if (worldlistunloaded.contains(args[1]))
                worldlistunloaded.remove(args[1]);
            getConfig().set("worlds." + args[1], null);
            saveConfig();
            File world = getServer().getWorld(args[1]).getWorldFolder();
            getServer().unloadWorld(args[1], true);
            delete(world);
            sender.sendMessage(ChatColor.GREEN + "Successfully deleted world \"" + args[1] + "\"");
            return true;
        }

        // WorldLoad Load

        if (args[0].equalsIgnoreCase("load")) {
            if (!sender.hasPermission("worldload.load")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /worldload load <world>");
                return true;
            }
            if (worldlist.contains(args[1]) || worldlistloaded.contains(args[1])
                    || getServer().getWorld(args[1]) == getServer().getWorlds().get(0)) {
                sender.sendMessage(ChatColor.RED + "World already exists!");
                return true;
            }
            if (worldlistunloaded.contains(args[1]))
                worldlistunloaded.remove(args[1]);
            worldlistloaded.add(args[1]);
            sender.sendMessage(ChatColor.GREEN + "Loading level \"" + args[1] + "\"");
            new WorldCreator(args[1]).createWorld();
            sender.sendMessage(ChatColor.GREEN + "Successfully loaded world \"" + args[1] + "\"");
            return true;
        }

        // WorldLoad Unload

        if (args[0].equalsIgnoreCase("unload")) {
            if (!sender.hasPermission("worldload.unload")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "Correct usage: /worldload unload <world>");
                return true;
            }
            if (worldlistunloaded.contains(args[1])) {
                sender.sendMessage(ChatColor.RED + "World is already unloaded!");
                return true;
            }
            if (getServer().getWorld(args[1]) == null) {
                sender.sendMessage(ChatColor.RED + "World does not exist!");
                return true;
            }
            if (getServer().getWorld(args[1]) == getServer().getWorlds().get(0)) {
                sender.sendMessage(ChatColor.RED + "Cannot unload the main world!");
                return true;
            }
            if (getServer().getWorld(args[1]).getPlayers().size() > 0) {
                sender.sendMessage(ChatColor.RED + "Cannot unload a world with players inside!");
                return true;
            }
            if (worldlist.contains(args[1]))
                worldlist.remove(args[1]);
            if (worldlistloaded.contains(args[1]))
                worldlistloaded.remove(args[1]);
            if (!worldlistunloaded.contains(args[1]))
                worldlistunloaded.add(args[1]);
            getServer().unloadWorld(args[1], true);
            sender.sendMessage(ChatColor.GREEN + "Successfully unloaded world \"" + args[1] + "\"");
            return true;
        }

        // WorldLoad Stats

        if (args[0].equalsIgnoreCase("stats")) {
            if (!sender.hasPermission("worldload.stats")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    getServer().getConsoleSender()
                            .sendMessage(ChatColor.RED + "Correct usage: /worldload stats [world]");
                    return true;
                }
                Player player = (Player) sender;
                int entities = player.getWorld().getEntities().size() - player.getWorld().getPlayers().size();
                player.sendMessage("----- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Current World Statistics "
                        + ChatColor.WHITE + "-----");
                player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.DARK_AQUA + player.getWorld().getName());
                player.sendMessage(
                        ChatColor.GRAY + "Type: " + ChatColor.DARK_AQUA + player.getWorld().getWorldType().getName());
                player.sendMessage(ChatColor.GRAY + "Environment: " + ChatColor.DARK_AQUA
                        + player.getWorld().getEnvironment().toString());
                player.sendMessage(ChatColor.GRAY + "Seed: " + ChatColor.DARK_AQUA + player.getWorld().getSeed());
                player.sendMessage(ChatColor.GRAY + "Difficulty: " + ChatColor.DARK_AQUA
                        + player.getWorld().getDifficulty().toString());
                player.sendMessage(ChatColor.GRAY + "Entities: " + ChatColor.DARK_AQUA + entities);
                player.sendMessage(
                        ChatColor.GRAY + "Players: " + ChatColor.DARK_AQUA + player.getWorld().getPlayers().size());
                player.sendMessage(ChatColor.GRAY + "Time: " + ChatColor.DARK_AQUA + player.getWorld().getTime());
                player.sendMessage(
                        ChatColor.GRAY + "Height: " + ChatColor.DARK_AQUA + player.getWorld().getMaxHeight());
                player.sendMessage(ChatColor.GRAY + "Structures: " + ChatColor.DARK_AQUA
                        + player.getWorld().canGenerateStructures());
                player.sendMessage(ChatColor.GRAY + "Border: " + ChatColor.DARK_AQUA
                        + player.getWorld().getWorldBorder().getSize());
                player.sendMessage(ChatColor.GRAY + "Spawn: " + ChatColor.DARK_AQUA
                        + player.getWorld().getSpawnLocation().getBlockX() + " / "
                        + player.getWorld().getSpawnLocation().getBlockY() + " / "
                        + player.getWorld().getSpawnLocation().getBlockZ());
                return true;
            }
            if (args.length == 2) {
                if (getServer().getWorld(args[1]) == null) {
                    sender.sendMessage(ChatColor.RED + "World does not exist!");
                    return true;
                }
                int entities = getServer().getWorld(args[1]).getEntities().size()
                        - getServer().getWorld(args[1]).getPlayers().size();
                sender.sendMessage("----- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "World \"" + args[1]
                        + "\" Statistics " + ChatColor.WHITE + "-----");
                sender.sendMessage(
                        ChatColor.GRAY + "World: " + ChatColor.DARK_AQUA + getServer().getWorld(args[1]).getName());
                sender.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getWorldType().getName());
                sender.sendMessage(ChatColor.GRAY + "Environment: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getEnvironment().toString());
                sender.sendMessage(
                        ChatColor.GRAY + "Seed: " + ChatColor.DARK_AQUA + getServer().getWorld(args[1]).getSeed());
                sender.sendMessage(ChatColor.GRAY + "Difficulty: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getDifficulty().toString());
                sender.sendMessage(ChatColor.GRAY + "Entities: " + ChatColor.DARK_AQUA + entities);
                sender.sendMessage(ChatColor.GRAY + "Players: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getPlayers().size());
                sender.sendMessage(
                        ChatColor.GRAY + "Time: " + ChatColor.DARK_AQUA + getServer().getWorld(args[1]).getTime());
                sender.sendMessage(ChatColor.GRAY + "Height: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getMaxHeight());
                sender.sendMessage(ChatColor.GRAY + "Structures: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).canGenerateStructures());
                sender.sendMessage(ChatColor.GRAY + "Border: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getWorldBorder().getSize());
                sender.sendMessage(ChatColor.GRAY + "Spawn: " + ChatColor.DARK_AQUA
                        + getServer().getWorld(args[1]).getSpawnLocation().getBlockX() + " / "
                        + getServer().getWorld(args[1]).getSpawnLocation().getBlockY() + " / "
                        + getServer().getWorld(args[1]).getSpawnLocation().getBlockZ());
                return true;
            }
        }

        // WorldLoad List

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("worldload.list")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(
                        "----- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "World List " + ChatColor.WHITE + "-----");
                sender.sendMessage(ChatColor.DARK_AQUA + "Created: " + ChatColor.GRAY + worldlist.toString());
                sender.sendMessage(ChatColor.DARK_AQUA + "Loaded: " + ChatColor.GRAY + worldlistloaded.toString());
                sender.sendMessage(ChatColor.DARK_AQUA + "Unloaded: " + ChatColor.GRAY + worldlistunloaded.toString());
                return true;
            }
        }

        // WorldLoad Reload

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("worldload.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            if (args.length == 1) {
                reloadConfig();
                if (getConfig().getConfigurationSection("worlds") != null) {
                    for (String world : getConfig().getConfigurationSection("worlds").getKeys(false)) {
                        getLogger().info("Preparing level \"" + world + "\"");
                        String type = getConfig().getString("worlds." + world + ".type");
                        String environment = getConfig().getString("worlds." + world + ".environment");
                        Boolean pvp = getConfig().getBoolean("worlds." + world + ".pvp");
                        String difficulty = getConfig().getString("worlds." + world + ".difficulty");
                        Long seed = getConfig().getLong("worlds." + world + ".seed");
                        Boolean structures = getConfig().getBoolean("worlds." + world + ".generate-structures");
                        Boolean animals = getConfig().getBoolean("worlds." + world + ".spawn-animals");
                        Boolean monsters = getConfig().getBoolean("worlds." + world + ".spawn-monsters");
                        new WorldCreator(world).type(WorldType.valueOf(type))
                                .environment(Environment.valueOf(environment)).seed(seed).generateStructures(structures)
                                .createWorld();
                        getServer().getWorld(world).setPVP(pvp);
                        getServer().getWorld(world).setDifficulty(Difficulty.valueOf(difficulty));
                        getServer().getWorld(world).setSpawnFlags(monsters, animals);
                        if (!worldlist.contains(world))
                            worldlist.add(world);
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Successfully reloaded config!");
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Unknown argument!");
        return true;
    }

    public boolean delete(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    delete(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
