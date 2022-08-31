package nl.dantevg.dynmapexport;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DynmapExport extends JavaPlugin {
	public static final int Y_LEVEL = 64;
	
	FileConfiguration config;
	Logger logger;
	
	protected DynmapWebAPI.@Nullable Configuration worldConfiguration;
	protected ImageTresholdCache imageTresholdCache;
	protected ExportScheduler exportScheduler;
	protected Downloader downloader;
	protected List<ExportConfig> exportConfigs;
	
	protected String dynmapHost;
	
	@Override
	public void onEnable() {
		logger = getLogger();
		
		getDataFolder().mkdirs(); // Create plugin folder
		
		// Config
		config = getConfig();
		saveDefaultConfig();
		
		dynmapHost = config.getString("dynmap-host");
		
		// Register commands
		CommandDynmapExport command = new CommandDynmapExport(this);
		getCommand("dynmapexport").setExecutor(command);
		getCommand("dynmapexport").setTabCompleter(command);
		
		imageTresholdCache = new ImageTresholdCache(this);
		exportScheduler = new ExportScheduler(this);
		downloader = new Downloader(this);
		
		worldConfiguration = getDynmapConfiguration();
		if (worldConfiguration == null) {
			exportConfigs = new ArrayList<>();
			return;
		}
		
		exportConfigs = config.getMapList("exports").stream()
				.map(this::getExportConfig)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	public void export() {
		int nSkipped = 0;
		for (ExportConfig exportConfig : exportConfigs) {
			if (downloader.downloadTiles(exportConfig) == -1) nSkipped++;
		}
		logger.log(Level.INFO, String.format("Exported %d configs, skipped %d",
				exportConfigs.size() - nSkipped, nSkipped));
	}
	
	public void reload() {
		logger.log(Level.INFO, "Reload: disabling plugin");
		setEnabled(false);
		Bukkit.getScheduler().cancelTasks(this);
		logger.log(Level.INFO, "Reload: re-enabling plugin");
		reloadConfig();
		setEnabled(true);
		logger.log(Level.INFO, "Reload complete");
	}
	
	/**
	 * Download the world configuration from Dynmap, which is used to determine
	 * the tile coordinates from world coordinates.
	 *
	 * @return the world configuration
	 */
	private @Nullable DynmapWebAPI.Configuration getDynmapConfiguration() {
		try {
			URL url = new URL(String.format("http://%s/up/configuration", dynmapHost));
			InputStreamReader reader = new InputStreamReader(url.openStream());
			return new Gson().fromJson(reader, DynmapWebAPI.Configuration.class);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (ConnectException e) {
			logger.log(Level.SEVERE, "Could not connect to Dynmap, check the port in config.yml");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not download Dynmap worlds configuration", e);
		}
		return null;
	}
	
	private @Nullable ExportConfig getExportConfig(@NotNull Map<?, ?> exportMap) {
		String worldName = (String) exportMap.get("world");
		String mapName = (String) exportMap.get("map");
		int zoom = (int) exportMap.get("zoom");
		Map<String, Integer> fromMap = (Map<String, Integer>) exportMap.get("from");
		Map<String, Integer> toMap = (Map<String, Integer>) exportMap.get("to");
		TileLocation from = new TileLocation(fromMap.get("x"), fromMap.get("y"));
		TileLocation to = new TileLocation(toMap.get("x"), toMap.get("y"));
		
		DynmapWebAPI.World world = worldConfiguration.getWorldByName(worldName);
		if (world == null) {
			logger.log(Level.SEVERE, worldName + " is not a valid world, ignoring");
			return null;
		}
		
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) {
			logger.log(Level.SEVERE, mapName + " is not a valid map for world " + worldName + ", ignoring");
			return null;
		}
		
		return new ExportConfig(world, map, zoom, from, to);
	}
	
	protected @NotNull String debug() {
		return "Dynmap world configuration:\n" + worldConfiguration.toString();
	}
	
}
