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
		if (args.length == 5) {
			String world = args[0];
			String map = args[1];
			int chunkX = Integer.parseInt(args[2]);
			int chunkZ = Integer.parseInt(args[3]);
			int zoom = Integer.parseInt(args[4]);
			String path = dynmapExport.downloadTile(world, map, chunkX, chunkZ, zoom);
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
