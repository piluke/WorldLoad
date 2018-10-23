package net.agazed.worldload;

import org.bukkit.World.Environment;

import java.util.logging.Level;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;

public class WorldConfig {
	WorldType type = WorldType.NORMAL;
	Environment environment = Environment.NORMAL;
	Long seed = null;
	boolean generate_structures = true;

	Difficulty difficulty = Difficulty.NORMAL;
	GameMode gamemode = GameMode.SURVIVAL;
	boolean pvp = true;
	boolean spawn_animals = true;
	boolean spawn_monsters = true;

	public static String option_info = "[type:TYPE] [env:ENVIRONMENT] [seed:SEED] [gen:GEN_STRUCTURES] [diff:DIFFICULTY] [mode:GAMEMODE] [pvp:PVP] [animals:SPAWN_ANIMALS] [monsters:SPAWN_MONSTERS]";

	public int configure(CommandSender sender, String[] args) {
		for (int i=2; i<args.length; ++i) {
			if (args[i].startsWith("type:")) {
				String t = args[i].substring("type:".length());
				try {
					this.type = WorldType.valueOf(t.toUpperCase());
				} catch (IllegalArgumentException ex) {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid world type \"%s\"!", t));
					return 1;
				}
			} else if (args[i].startsWith("env:")) {
				String e = args[i].substring("env:".length());
				try {
					this.environment = Environment.valueOf(e.toUpperCase());
				} catch (IllegalArgumentException ex) {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid environment \"%s\"!", e));
					return 1;
				}
			} else if (args[i].startsWith("seed:")) {
				String s = args[i].substring("seed:".length());
				try {
					this.seed = Long.parseLong(s);
				} catch (NumberFormatException ex) {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid seed \"%s\"!", s));
					return 1;
				}
			} else if (args[i].startsWith("gen:")) {
				String b = args[i].substring("gen:".length());
				if ((b.equalsIgnoreCase("true"))||(b.equalsIgnoreCase("false"))) {
					this.generate_structures = Boolean.parseBoolean(b);
				} else {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid structure generation value \"%s\"!", b));
					return 1;
				}
			} else if (args[i].startsWith("diff:")) {
				String d = args[i].substring("diff:".length());
				try {
					this.difficulty = Difficulty.valueOf(d.toUpperCase());
				} catch (IllegalArgumentException ex) {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid difficulty \"%s\"!", d));
					return 1;
				}
			} else if (args[i].startsWith("mode:")) {
				String m = args[i].substring("mode:".length());
				try {
					this.gamemode = GameMode.valueOf(m.toUpperCase());
				} catch (IllegalArgumentException ex) {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid game mode \"%s\"!", m));
					return 1;
				}
			} else if (args[i].startsWith("pvp:")) {
				String p = args[i].substring("pvp:".length());
				if ((p.equalsIgnoreCase("true"))||(p.equalsIgnoreCase("false"))) {
					this.pvp = Boolean.parseBoolean(p);
				} else {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid pvp value \"%s\"!", p));
					return 1;
				}
			} else if (args[i].startsWith("animals:")) {
				String a = args[i].substring("animals:".length());
				if ((a.equalsIgnoreCase("true"))||(a.equalsIgnoreCase("false"))) {
					this.spawn_animals = Boolean.parseBoolean(a);
				} else {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid animal spawn value \"%s\"!", a));
					return 1;
				}
			} else if (args[i].startsWith("monsters:")) {
				String m = args[i].substring("monsters:".length());
				if ((m.equalsIgnoreCase("true"))||(m.equalsIgnoreCase("false"))) {
					this.spawn_monsters = Boolean.parseBoolean(m);
				} else {
					WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid monster spawn value \"%s\"!", m));
					return 1;
				}
			} else {
				WorldLoad.senderLog(sender, Level.WARNING, String.format("Invalid argument \"%s\"!", args[i]));
				return 2;
			}
		}

		return 0;
	}
	public void apply(WorldCreator wc) {
		wc
		  .type(this.type)
		  .environment(this.environment)
		  .generateStructures(this.generate_structures);
		if (this.seed != null) {
			wc.seed(this.seed);
		}
	}
}
