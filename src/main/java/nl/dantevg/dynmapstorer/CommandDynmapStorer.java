package nl.dantevg.dynmapstorer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandDynmapStorer implements CommandExecutor, TabCompleter {
	private final DynmapStorer plugin;
	
	public CommandDynmapStorer(DynmapStorer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO: implement
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		// TODO: implement
		return null;
	}
}
