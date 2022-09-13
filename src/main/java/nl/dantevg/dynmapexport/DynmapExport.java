package nl.dantevg.dynmapexport;

import com.google.gson.Gson;
import nl.dantevg.dynmapexport.location.TileCoords;
import nl.dantevg.dynmapexport.location.WorldCoords;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
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
	
	protected @Nullable DynmapWebAPI.Configuration worldConfiguration;
	protected ImageTresholdCache imageTresholdCache;
	protected ExportScheduler exportScheduler;
	protected Downloader downloader;
	protected TileCombiner tileCombiner;
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
		tileCombiner = new TileCombiner(this);
		
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
	
	/**
	 * Export all configurations. <b>Should be run as an async task to prevent
	 * server lag!</b>
	 *
	 * @param commandSender where to send update and completion logs to, if any
	 * @return the number of configs that are exported (i.e. they had enough changes)
	 */
	public int export(@Nullable CommandSender commandSender) {
		int nExported = 0;
		Instant now = Instant.now();
		for (ExportConfig exportConfig : exportConfigs) {
			if (commandSender != null) {
				commandSender.sendMessage(String.format("Exporting map %s:%s",
						exportConfig.world.name, exportConfig.map.name));
			}
			
			Map<TileCoords, File> downloadedTiles = downloader.downloadTiles(exportConfig, now);
			if (downloadedTiles != null && downloadedTiles.size() > 0) {
				nExported++;
				if (config.getBoolean("auto-combine")
						&& tileCombiner.combineAndSave(exportConfig, now)) {
					downloader.removeOldExportDirs(exportConfig);
				}
			}
		}
		
		logger.log(Level.INFO, String.format("Exported %d configs, skipped %d",
				nExported, exportConfigs.size() - nExported));
		if (commandSender != null) commandSender.sendMessage(String.format("Exported %d configs, skipped %d",
				nExported, exportConfigs.size() - nExported));
		return nExported;
	}
	
	/**
	 * Export all configurations. <b>Should be run as an async task to prevent
	 * server lag!</b>
	 *
	 * @return the number of configs that are exported (i.e. they had enough changes)
	 */
	public int export() {
		return export(null);
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
		
		if (!fromMap.containsKey("x") || !fromMap.containsKey("z")) {
			logger.log(Level.WARNING, "export field 'from' needs to have at least fields 'x' and 'z', ignoring this export");
			return null;
		}
		if (!toMap.containsKey("x") || !toMap.containsKey("z")) {
			logger.log(Level.WARNING, "export field 'to' needs to have at least fields 'x' and 'z', ignoring this export");
			return null;
		}
		WorldCoords from = new WorldCoords(
				fromMap.get("x"),
				fromMap.getOrDefault("y", Y_LEVEL),
				fromMap.get("z"));
		WorldCoords to = new WorldCoords(
				toMap.get("x"),
				toMap.getOrDefault("y", Y_LEVEL),
				toMap.get("z"));
		
		DynmapWebAPI.World world = worldConfiguration.getWorldByName(worldName);
		if (world == null) {
			logger.log(Level.SEVERE, worldName + " is not a valid world, ignoring this export");
			return null;
		}
		
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) {
			logger.log(Level.SEVERE, mapName + " is not a valid map for world " + worldName + ", ignoring this export");
			return null;
		}
		
		return new ExportConfig(world, map, zoom, from, to);
	}
	
	protected @NotNull String debug() {
		return "Dynmap world configuration:\n" + worldConfiguration.toString();
	}
	
}
