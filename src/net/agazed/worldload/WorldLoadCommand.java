package net.agazed.worldload;

import java.util.logging.Level;

import org.bukkit.WorldCreator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldLoadCommand implements CommandExecutor {
	private WorldLoad wl = null;

	public WorldLoadCommand(WorldLoad _wl) {
		super();
		wl = _wl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((args.length == 0)||(args[0].equalsIgnoreCase("help"))) {
			if (!sender.hasPermission("worldload.help")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			ChatColor aq = ChatColor.DARK_AQUA;
			ChatColor bold = ChatColor.BOLD;
			ChatColor gr = ChatColor.GRAY;
			ChatColor wh = ChatColor.WHITE;

			sender.sendMessage(String.format("-----%s%s WorldLoad Help %s-----", aq, bold, wh));
			sender.sendMessage(String.format("%s/worldload %shelp %s- Display this info", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %stp <world> %s- Teleport to a world", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %screate <world> %s %s- Create a world", aq, gr, WorldConfig.option_info, wh));
			sender.sendMessage(String.format("%s/worldload %sremove <world> %s- Remove a world from the config", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %sload <world> %s- Load a world", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %sunload <world> %s- Unload a world", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %slist %s- List the configured worlds", aq, gr, wh));
			sender.sendMessage(String.format("%s/worldload %sreload %s- Reload the config", aq, gr, wh));

			return true;
		} else if (args[0].equalsIgnoreCase("tp")) {
			if (!(sender instanceof Player)) {
				WorldLoad.senderLog(sender, Level.WARNING, "Command can only be run as a player!");
				return true;
			}

			Player player = (Player) sender;
			if (!player.hasPermission("worldload.tp")) {
				WorldLoad.senderLog(player, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				WorldLoad.senderLog(player, Level.WARNING, "Correct usage: /worldload tp <world>");
				return true;
			}

			String world_name = args[1];
			if (this.wl.getServer().getWorld(world_name) == null) {
				WorldLoad.senderLog(player, Level.WARNING, "World is unloaded!");
				return true;
			}

			String p = String.format("players.%s.", player.getUniqueId());
			this.wl.config.set(p + player.getWorld().getUID(), player.getLocation());
			this.wl.saveConfig();

			Location loc = this.wl.config.getSerializable(p + this.wl.getServer().getWorld(world_name).getUID(), Location.class);
			Location l = this.wl.getServer().getWorld(world_name).getSpawnLocation();
			if (loc != null) {
				l = loc;
			} else {
				Location bed = player.getBedSpawnLocation();
				if ((bed != null)&&(bed.getWorld().equals(l.getWorld()))) {
					l = bed;
				}
			}
			player.teleport(l, PlayerTeleportEvent.TeleportCause.COMMAND);

			WorldLoad.senderLog(player, Level.INFO, String.format("Teleported to world \"%s\"", world_name));
			return true;
		} else if (args[0].equalsIgnoreCase("create")) {
			if (!sender.hasPermission("worldload.create")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				WorldLoad.senderLog(sender, Level.WARNING, String.format("Correct usage: /worldload create <world> %s", WorldConfig.option_info));
				return true;
			}

			String world_name = args[1];
			if (
				(this.wl.config.contains("worlds." + world_name))
				||(this.wl.getServer().getWorld(world_name) != null)
			) {
				WorldLoad.senderLog(sender, Level.WARNING, "World already exists, some options will be ignored!");
			}

			WorldConfig wconf = new WorldConfig();
			if (args.length > 2) {
				if (wconf.configure(sender, args) != 0) {
					return true;
				}
			}

			WorldLoad.senderLog(sender, Level.INFO, String.format("Preparing level \"%s\"", world_name));

			String w = "worlds." + world_name;
			this.wl.config.set(w + ".difficulty", wconf.difficulty.toString());
			this.wl.config.set(w + ".gamemode", wconf.gamemode.toString());
			this.wl.config.set(w + ".pvp", wconf.pvp);
			this.wl.config.set(w + ".spawn-animals", wconf.spawn_animals);
			this.wl.config.set(w + ".spawn-monsters", wconf.spawn_monsters);
			this.wl.saveConfig();

			WorldCreator wc = new WorldCreator(world_name);
			wconf.apply(wc);
			World world = wc.createWorld();

			world.setPVP(wconf.pvp);
			world.setDifficulty(wconf.difficulty);
			world.setSpawnFlags(wconf.spawn_monsters, wconf.spawn_animals);

			for (Player p : this.wl.getServer().getWorld(world_name).getPlayers()) {
				p.setGameMode(wconf.gamemode);
			}

			WorldLoad.senderLog(sender, Level.INFO, String.format("Successfully created world \"%s\"", world_name));
			return true;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (!sender.hasPermission("worldload.remove")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				WorldLoad.senderLog(sender, Level.WARNING, "Correct usage: /worldload remove <world>");
				return true;
			}

			String world_name = args[1];
			if (!this.wl.config.contains("worlds." + world_name)) {
				WorldLoad.senderLog(sender, Level.WARNING, "World does not exist!");
				return true;
			}

			this.wl.config.set("worlds." + world_name, null);
			this.wl.saveConfig();

			WorldLoad.senderLog(sender, Level.INFO, String.format("Successfully removed world \"%s\" from the world list.", world_name));
			return true;
		} else if (args[0].equalsIgnoreCase("load")) {
			if (!sender.hasPermission("worldload.load")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				WorldLoad.senderLog(sender, Level.WARNING, "Correct usage: /worldload load <world>");
				return true;
			}

			String world_name = args[1];
			if (this.wl.getServer().getWorld(world_name) != null) {
				WorldLoad.senderLog(sender, Level.WARNING, "World already loaded!");
				return true;
			}

			WorldLoad.senderLog(sender, Level.INFO, String.format("Loading world \"%s\"", world_name));
			new WorldCreator(world_name).createWorld();

			WorldLoad.senderLog(sender, Level.INFO, String.format("Successfully loaded world \"%s\"", world_name));
			return true;
		} else if (args[0].equalsIgnoreCase("unload")) {
			if (!sender.hasPermission("worldload.unload")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				WorldLoad.senderLog(sender, Level.WARNING, "Correct usage: /worldload unload <world>");
				return true;
			}

			String world_name = args[1];
			if (this.wl.getServer().getWorld(world_name) == null) {
				WorldLoad.senderLog(sender, Level.WARNING, "World is not loaded!");
				return true;
			}
			if (this.wl.getServer().getWorld(world_name) == this.wl.getServer().getWorlds().get(0)) {
				WorldLoad.senderLog(sender, Level.WARNING, "Cannot unload the main world!");
				return true;
			}
			if (this.wl.getServer().getWorld(world_name).getPlayers().size() > 0) {
				WorldLoad.senderLog(sender, Level.WARNING, "Cannot unload a world with players inside!");
				return true;
			}

			this.wl.getServer().unloadWorld(world_name, true);

			WorldLoad.senderLog(sender, Level.INFO, String.format("Successfully unloaded world \"%s\"", world_name));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			if (!sender.hasPermission("worldload.list")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				ChatColor aq = ChatColor.DARK_AQUA;
				ChatColor gr = ChatColor.GRAY;

				sender.sendMessage(String.format("%sWorlds: %s%s", aq, gr, this.wl.config.getConfigurationSection("worlds").getKeys(false)));
				return true;
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("worldload.reload")) {
				WorldLoad.senderLog(sender, Level.WARNING, "No permission!");
				return true;
			}

			if (args.length == 1) {
				this.wl.reloadConfig();
				this.wl.load();
				WorldLoad.senderLog(sender, Level.INFO, "Successfully reloaded config!");
				return true;
			}
		}

		WorldLoad.senderLog(sender, Level.WARNING, String.format("Unknown subcommand: \"%s\"", args[0]));
		return true;
	}
}
