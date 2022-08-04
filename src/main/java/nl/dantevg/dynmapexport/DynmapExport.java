package nl.dantevg.dynmapexport;

import org.bukkit.plugin.java.JavaPlugin;

public class DynmapExport extends JavaPlugin {
	@Override
	public void onEnable() {
		// Register commands
		CommandDynmapExport command = new CommandDynmapExport(this);
		getCommand("dynmapexport").setExecutor(command);
		getCommand("dynmapexport").setTabCompleter(command);
	}
	
}
