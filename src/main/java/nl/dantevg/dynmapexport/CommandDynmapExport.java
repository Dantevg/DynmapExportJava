package nl.dantevg.dynmapexport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandDynmapExport implements CommandExecutor, TabCompleter {
	private final DynmapExport plugin;
	
	public CommandDynmapExport(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equals("now")) {
			// Export all from config.yml
			int nSkipped = 0;
			for (ExportConfig exportConfig : plugin.exportConfigs) {
				if (plugin.downloader.downloadTiles(exportConfig) == -1) nSkipped++;
			}
			sender.sendMessage(String.format("Exported %d configs, skipped %d",
					plugin.exportConfigs.size() - nSkipped, nSkipped));
			return true;
		} else if (args.length == 1 && args[0].equals("reload")) {
			plugin.reload();
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
			String path = plugin.downloader.downloadTile(world, map, x, z, zoom);
			if (path != null) {
				sender.sendMessage("Saved tile at " + path);
			} else {
				sender.sendMessage("Could not save tile (see console)");
			}
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			return Arrays.asList("now", "export");
		} else if (args.length == 2 && args[0].equals("export")) {
			// Suggest world
			return plugin.worldConfiguration.worlds.stream()
					.map(world -> world.name)
					.collect(Collectors.toList());
		} else if (args.length == 3 && args[0].equals("export")) {
			// Suggest map
			DynmapWebAPI.World world = plugin.worldConfiguration.getWorldByName(args[0]);
			if (world != null) {
				return world.maps.stream().map(map -> map.name).collect(Collectors.toList());
			} else {
				return null;
			}
		}
		return null;
	}
	
}
