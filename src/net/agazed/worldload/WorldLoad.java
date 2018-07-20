package net.agazed.worldload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.WorldCreator;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

public class WorldLoad extends JavaPlugin {
	public FileConfiguration config = null;
	public static String version = "WorldLoad v4.1";

	private File configurationFile = null;
	private static Logger logger = Logger.getLogger("minecraft");

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new WorldLoadPlayerListener(this), this);

		this.getCommand("worldload").setExecutor(new WorldLoadCommand(this));

		createDefaultConfig(getDataFolder(), "config.yml");
		reloadConfig();
		load();

		log("Enabled!");
	}
	public void onDisable() {
		this.config = null;
		this.configurationFile = null;

		log("Disabled!");
	}

	public static void log(String s) {
		WorldLoad.logger.log(Level.INFO, String.format("[WorldLoad] %s", s));
	}
	public static void senderLog(CommandSender sender, Level level, String s) {
		if (level == Level.INFO) {
			s = String.format("%s%s", ChatColor.GREEN, s);
		} else if (level == Level.WARNING) {
			s = String.format("%s%s", ChatColor.RED, s);
		}
		sender.sendMessage(s);
	}

	public void reloadConfig() {
		if (this.configurationFile == null) {
			this.configurationFile = new File(getDataFolder(), "config.yml");
		}

		this.config = YamlConfiguration.loadConfiguration(this.configurationFile);
		InputStream defConfigStream = getResource("config.yml");
		if (defConfigStream != null) {
			InputStreamReader defConfigReader = new InputStreamReader(defConfigStream);
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigReader);
			this.config.setDefaults(defConfig);
		}
	}
	private void createDefaultConfig(File dir, String name) {
		if (!dir.exists()) {
			dir.mkdir();
		}

		File file = new File(dir, name);
		if (file.exists()) {
			return;
		}

		try {
			InputStream in = WorldLoad.class.getResourceAsStream(String.format("/res/%s", name));
			if (in == null) {
				log("Failed to get default config file");
				return;
			}

			FileOutputStream out = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;

			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}

			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void saveConfig() {
		if ((this.config == null)||(this.configurationFile == null)) {
			log(String.format("Could not save config"));
			return;
		}

		try {
			this.config.save(this.configurationFile);
		} catch (IOException ex) {
			log(String.format("Could not save config to %s: %s", this.configurationFile, ex.getMessage()));
		}
	}    

	public void load() {
		if (this.config.getConfigurationSection("worlds") != null) {
			for (String world_name : this.config.getConfigurationSection("worlds").getKeys(false)) {
				log(String.format("Preparing level \"%s\"", world_name));
				ConfigurationSection wconf = this.config.getConfigurationSection("worlds." + world_name);

				String difficulty = wconf.getString("difficulty");
				//String gamemode = wconf.getString("gamemode");
				Boolean pvp = wconf.getBoolean("pvp");
				Boolean animals = wconf.getBoolean("spawn-animals");
				Boolean monsters = wconf.getBoolean("spawn-monsters");

				World world = (new WorldCreator(world_name)).createWorld();
				if (world == null) {
					continue;
				}

				world.setPVP(pvp);
				world.setDifficulty(Difficulty.valueOf(difficulty));
				world.setSpawnFlags(monsters, animals);
			}
		}
	}

	public GameMode getGameMode(String world_name) {
		return GameMode.valueOf(this.config.getString(String.format("worlds.%s.gamemode", world_name), "SURVIVAL"));
	}
}
