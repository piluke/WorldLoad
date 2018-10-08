package net.agazed.worldload;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class WorldLoadPlayerListener implements Listener {
	private WorldLoad wl = null;

	public WorldLoadPlayerListener(WorldLoad _wl) {
		wl = _wl;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		//GameMode pgm = player.getGameMode();
		GameMode pgm = this.wl.getGameMode(event.getFrom().getName());
		GameMode gm = this.wl.getGameMode(player.getWorld().getName());
		if (pgm != gm) {
			// Save inventory
			String key = "";
			switch (pgm) {
				case SURVIVAL:
				case CREATIVE:
				case ADVENTURE: {
					key = pgm.toString().toLowerCase();
					break;
				}

				// Ignore other inventories
				case SPECTATOR:
				default: {}
			}

			if (!key.isEmpty()) {
				this.wl.players.set(String.format("players.%s.inventories.%s", player.getUniqueId(), key), player.getInventory().getContents());
				this.wl.saveConfig();
			}
			key = "";

			player.getInventory().clear();

			// Load inventory
			switch (gm) {
				case SURVIVAL:
				case CREATIVE:
				case ADVENTURE: {
					key = gm.toString().toLowerCase();
					break;
				}

				// Ignore other inventories
				case SPECTATOR:
				default: {}
			}

			if (!key.isEmpty()) {
				Object it = this.wl.players.get(String.format("players.%s.inventories.%s", player.getUniqueId(), key));
				if (it != null) {
					ItemStack[] items = null;

					if (it instanceof ItemStack[]) {
						items = (ItemStack[]) it;
					} else if (it instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<ItemStack> l = (List<ItemStack>) it;
						items = l.toArray(new ItemStack[l.size()]);
					} else {
						WorldLoad.log("no inventory");
					}

					player.getInventory().setContents(items);
				}
			}
		}

		player.setGameMode(gm);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerPortal(PlayerPortalEvent event) {
		if ((event.getCause() == TeleportCause.NETHER_PORTAL)||(event.getCause() == TeleportCause.END_PORTAL)) {
			event.useTravelAgent(true);
			if (event.getTo() == null) {
				Location loc = event.getPlayer().getLocation();
				String w = loc.getWorld().getName();
				if (event.getCause() == TeleportCause.NETHER_PORTAL) {
					if (loc.getWorld().getEnvironment() != Environment.NETHER) {
						w += "_nether";
					} else {
						w = w.substring(0, w.length() - "_nether".length());
					}
				} else {
					if (loc.getWorld().getEnvironment() != Environment.THE_END) {
						w += "_the_end";
					} else {
						w = w.substring(0, w.length() - "_the_end".length());
					}
				}

				World world = this.wl.getServer().getWorld(w);
				if (world == null) {
					Environment env = Environment.NETHER;
					if (event.getCause() == TeleportCause.END_PORTAL) {
						env = Environment.THE_END;
					}
					WorldLoad.senderLog(event.getPlayer(), Level.INFO, "Preparing teleport...");
					world = new WorldCreator(w).environment(env).createWorld();
				}

				loc.setWorld(world);
				event.setTo(loc);
			}
		}
	}
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		World w = event.getPlayer().getWorld();
		if ((w.getEnvironment() == Environment.NETHER)||(w.getEnvironment() == Environment.THE_END)) {
			String spawn_world = w.getName();
			if (w.getEnvironment() == Environment.NETHER) {
				spawn_world = spawn_world.substring(0, spawn_world.length() - "_nether".length());
			} else {
				spawn_world = spawn_world.substring(0, spawn_world.length() - "_the_end".length());
			}

			World sw = this.wl.getServer().getWorld(spawn_world);
			event.setRespawnLocation(sw.getSpawnLocation());
		} else {
			event.setRespawnLocation(w.getSpawnLocation());
		}
	}
}
