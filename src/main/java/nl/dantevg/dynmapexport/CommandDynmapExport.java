package nl.dantevg.dynmapexport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandDynmapExport implements CommandExecutor, TabCompleter {
	private final DynmapExport dynmapExport;
	
	public CommandDynmapExport(DynmapExport dynmapExport) {
		this.dynmapExport = dynmapExport;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			// Export all from config.yml
			int nSkipped = 0;
			for (ExportConfig exportConfig : dynmapExport.exportConfigs) {
				if (dynmapExport.downloader.downloadTiles(exportConfig) == -1) nSkipped++;
			}
			sender.sendMessage(String.format("Exported %d configs, skipped %d",
					dynmapExport.exportConfigs.size() - nSkipped, nSkipped));
			return true;
		} else if (args.length == 5) {
			// Export single
			String world = args[0];
			String map = args[1];
			int x = Integer.parseInt(args[2]);
			int z = Integer.parseInt(args[3]);
			int zoom = Integer.parseInt(args[4]);
			String path = dynmapExport.downloader.downloadTile(world, map, x, z, zoom);
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
		// TODO: implement
		return null;
	}
}
