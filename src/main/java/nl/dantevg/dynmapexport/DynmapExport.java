package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapExport extends JavaPlugin {
	public static final int Y_LEVEL = 64;
	
	FileConfiguration config;
	Logger logger;
	
	DynmapWebAPI.Configuration worldConfiguration;
	
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
		
		worldConfiguration = getDynmapConfiguration();
	}
	
	public @Nullable String downloadTile(String world, String map, int x, int z, int zoom) {
		DynmapLocation location = getLocation(world, map, x, z, zoom);
		return downloadTile(location);
	}
	
	public @Nullable String downloadTile(DynmapLocation location) {
		File dest = getDestFile(Instant.now(), location);
		String tilePath = getPath(location);
		return download(tilePath, dest) ? dest.getPath() : null;
	}
	
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
	
	private @Nullable DynmapLocation getLocation(String worldName, String mapName, int x, int z, int zoom) {
		DynmapWebAPI.World world = worldConfiguration.getWorldByName(worldName);
		if (world == null) return null;
		
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) return null;
		
		return new DynmapLocation(world, map, new Vector(x, Y_LEVEL, z), zoom);
	}
	
	/**
	 * Download the Dynmap tile at <code>path</code> to <code>dest</code>.
	 *
	 * @param path the Dynmap path to the tile
	 * @param dest the destination file to download to.
	 * @return whether the download succeeded
	 */
	private boolean download(String path, @NotNull File dest) {
		int port = config.getInt("dynmap-port");
		
		try {
			URL url = new URL(String.format("http://localhost:%d/%s", port, path));
			InputStream inputStream = url.openStream();
			dest.getParentFile().mkdirs(); // Make all directories on path to file
			long bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			logger.log(Level.INFO, "Downloaded tile " + path);
			if (bytesWritten == 0) logger.log(Level.WARNING, "Tile was 0 bytes!");
			return bytesWritten > 0;
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not download tile", e);
		}
		return false;
	}
	
	/**
	 * Get the Dynmap path to the tile specified.
	 *
	 * @param location the location in the world
	 * @return the path to the Dynmap tile image at
	 * <code>{world}/{map}/{regionX}_{regionZ}/{zoom}_{tileX}_{tileY}.png</code>
	 */
	private @NotNull String getPath(DynmapLocation location) {
		final int regionX = location.getWorldX() / 16 / 32;
		final int regionZ = location.getWorldZ() / 16 / 32;
		final String zoomStr = (location.zoom > 0) ? Strings.repeat("z", location.zoom) + "_" : "";
		
		return String.format("tiles/%s/%s/%d_%d/%s%d_%d.png",
				location.world.name, location.map.prefix, regionX, regionZ, zoomStr,
				location.getTileX(), location.getTileY());
	}
	
	/**
	 * Get the file where the image at the given location is to be stored.
	 * The instant gets formatted in ISO 8601 basic format, truncated to minutes
	 * (for example, <code>20220804T213200Z</code>).
	 *
	 * @param now      the current time
	 * @param location the location in the world
	 * @return the file at location <code>plugins/DynmapExport/exports/{now}/{tileX}_{tileY}.png</code>
	 */
	private @NotNull File getDestFile(@NotNull Instant now, DynmapLocation location) {
		// Convert extended format to basic format without separators (which are problematic in filenames)
		// https://stackoverflow.com/a/39820917
		String datetime = now.truncatedTo(ChronoUnit.MINUTES).toString()
				.replace("-", "")
				.replace(":", "");
		File directory = new File(getDataFolder(), "exports/latest");
		return new File(directory, String.format("%d_%d.png", location.getTileX(), location.getTileY()));
	}
	
}
