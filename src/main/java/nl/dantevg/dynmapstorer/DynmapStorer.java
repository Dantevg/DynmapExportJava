package nl.dantevg.dynmapstorer;

import org.bukkit.plugin.java.JavaPlugin;

public class DynmapStorer extends JavaPlugin {
	@Override
	public void onEnable() {
		// Register commands
		CommandDynmapStorer command = new CommandDynmapStorer(this);
		getCommand("dynmapstorer").setExecutor(command);
		getCommand("dynmapstorer").setTabCompleter(command);
	}
	
}
