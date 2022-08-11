package nl.dantevg.dynmapexport;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
	
	protected DynmapWebAPI.Configuration worldConfiguration;
	protected Downloader downloader;
	protected List<ExportConfig> exportConfigs;
	
	@Override
	public void onEnable() {
		// Config
		config = getConfig();
		saveDefaultConfig();
		
		logger = getLogger();
		
		worldConfiguration = getDynmapConfiguration();
		
		exportConfigs = config.getMapList("exports").stream()
				.map(exportMap -> {
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
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		// Register commands
		CommandDynmapExport command = new CommandDynmapExport(this);
		getCommand("dynmapexport").setExecutor(command);
		getCommand("dynmapexport").setTabCompleter(command);
		
		downloader = new Downloader(this);
	}
	
	/**
	 * Download the world configuration from Dynmap, which is used to determine
	 * the tile coordinates from world coordinates.
	 *
	 * @return the world configuration
	 */
	private @Nullable DynmapWebAPI.Configuration getDynmapConfiguration() {
		int port = config.getInt("dynmap-port");
		try {
			URL url = new URL(String.format("http://localhost:%d/up/configuration", port));
			InputStreamReader reader = new InputStreamReader(url.openStream());
			return new Gson().fromJson(reader, DynmapWebAPI.Configuration.class);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not download dynmap worlds configuration", e);
		}
		return null;
	}
	
}
