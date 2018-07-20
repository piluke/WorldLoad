package net.agazed.worldload;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
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

		GameMode gm = this.wl.getGameMode(player.getWorld().getName());
		if ((player.getGameMode() == GameMode.CREATIVE)&&(gm != GameMode.CREATIVE)) {
			player.getInventory().clear(); // TODO: restore inventory
		}
		player.setGameMode(gm);
	}
}
