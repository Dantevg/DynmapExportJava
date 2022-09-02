package nl.dantevg.dynmapexport;

import nl.dantevg.dynmapexport.location.TileCoords;
import nl.dantevg.dynmapexport.location.WorldCoords;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandDynmapExport implements CommandExecutor, TabCompleter {
	private final DynmapExport plugin;
	
	public CommandDynmapExport(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, String label, String @NotNull [] args) {
		if (args.length == 1 && args[0].equals("now")) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				int nExported = plugin.export();
				if (!(sender instanceof ConsoleCommandSender)) {
					plugin.logger.log(Level.INFO, String.format("Exported %d configs, skipped %d",
							nExported, plugin.exportConfigs.size() - nExported));
				}
			});
			return true;
		} else if (args.length == 1 && args[0].equals("reload")) {
			plugin.reload();
			if (!(sender instanceof ConsoleCommandSender)) sender.sendMessage("Reload complete");
			return true;
		} else if (args.length == 1 && args[0].equals("debug")) {
			sender.sendMessage(plugin.debug());
			return true;
		} else if (args.length == 6 && args[0].equals("export")) {
			// Export single
			String world = args[1];
			String map = args[2];
			int x, z, zoom;
			try {
				x = Integer.parseInt(args[3]);
				z = Integer.parseInt(args[4]);
				zoom = Integer.parseInt(args[5]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid number");
				return false;
			}
			
			String path;
			try {
				path = plugin.downloader.downloadTile(world, map, x, z, zoom);
			} catch (IllegalArgumentException e) {
				sender.sendMessage("Could not save tile: " + e.getMessage());
				return false;
			}
			if (path != null) {
				sender.sendMessage("Saved tile at " + path);
			} else {
				sender.sendMessage("Could not save tile (see console)");
			}
			return true;
		} else if ((args.length == 6 || args.length == 7) && args[0].equals("worldtomap")) {
			String worldName = args[1];
			String mapName = args[2];
			int x, y, z, zoom = 0;
			try {
				x = Integer.parseInt(args[3]);
				y = Integer.parseInt(args[4]);
				z = Integer.parseInt(args[5]);
				if (args.length == 7) zoom = Integer.parseInt(args[6]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid number");
				return false;
			}
			WorldCoords worldCoords = new WorldCoords(x, y, z);
			DynmapWebAPI.Map map = getMapFromWorldMapNames(sender, worldName, mapName);
			if (map == null) return false;
			TileCoords tileCoords = worldCoords.toTileCoords(map, zoom);
			sender.sendMessage(String.format("%s is in tile %s", worldCoords, tileCoords));
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String @NotNull [] args) {
		if (args.length == 1) {
			return Arrays.asList("now", "export", "reload", "debug", "worldtomap");
		} else if (args.length == 2 && (args[0].equals("export") || args[0].equals("worldtomap"))) {
			// Suggest world
			return plugin.worldConfiguration.worlds.stream()
					.map(world -> world.name)
					.collect(Collectors.toList());
		} else if (args.length == 3 && (args[0].equals("export") || args[0].equals("worldtomap"))) {
			// Suggest map
			DynmapWebAPI.World world = plugin.worldConfiguration.getWorldByName(args[1]);
			if (world != null) {
				return world.maps.stream().map(map -> map.name).collect(Collectors.toList());
			} else {
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}
	
	private @Nullable DynmapWebAPI.Map getMapFromWorldMapNames(@NotNull CommandSender sender, String worldName, String mapName) {
		assert plugin.worldConfiguration != null;
		DynmapWebAPI.World world = plugin.worldConfiguration.getWorldByName(worldName);
		if (world == null) {
			sender.sendMessage("no world with name " + worldName);
			return null;
		}
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) {
			sender.sendMessage("world " + worldName + " has no map with name " + mapName);
			return null;
		}
		return map;
	}
	
}
