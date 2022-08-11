package nl.dantevg.dynmapexport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DynmapExport extends JavaPlugin {
	public static final int Y_LEVEL = 64;
	
	FileConfiguration config;
	Logger logger;
	
	Downloader downloader;
	
	@Override
	public void onEnable() {
		// Config
		config = getConfig();
		saveDefaultConfig();
		
		logger = getLogger();
		
		// Register commands
		CommandDynmapExport command = new CommandDynmapExport(this);
		getCommand("dynmapexport").setExecutor(command);
		getCommand("dynmapexport").setTabCompleter(command);
		
		downloader = new Downloader(this);
	}
	
}
