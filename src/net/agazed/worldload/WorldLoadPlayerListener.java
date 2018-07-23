package net.agazed.worldload;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class WorldLoadPlayerListener implements Listener {
	private WorldLoad wl = null;

	public WorldLoadPlayerListener(WorldLoad _wl) {
		wl = _wl;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		GameMode pgm = player.getGameMode();
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
}
