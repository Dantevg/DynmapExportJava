package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapExport extends JavaPlugin {
	static FileConfiguration config;
	static Logger logger;
	
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
	}
	
	public @Nullable String downloadTile(String world, String map, int chunkX, int chunkZ, int zoom) {
		File dest = getDestFile(Instant.now(), chunkX, chunkZ);
		return download(getPath(world, map, chunkX, chunkZ, zoom), dest)
				? dest.getPath() : null;
	}
	
	/**
	 * Download the Dynmap tile at <code>path</code> to <code>dest</code>.
	 *
	 * @param path the Dynmap path to the tile
	 * @param dest the destination file to download to.
	 * @return whether the download succeeded
	 */
	private static boolean download(String path, @NotNull File dest) {
		int port = config.getInt("dynmap-port");
		
		try {
			URL url = new URL(String.format("http://localhost:%d/%s", port, path));
			InputStream inputStream = url.openStream();
			long bytesWritten = Files.copy(inputStream, dest.toPath());
			if (bytesWritten == 0) logger.log(Level.WARNING, "Downloaded 0 bytes from tile " + path);
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
	 * @param world  the name of the world (e.g. <code>"Survival"</code>)
	 * @param map    the name of the map, or "prefix" (e.g. <code>"flat"</code>)
	 * @param chunkX the X-coordinate of the chunk
	 * @param chunkZ the Z-coordinate of the chunk
	 * @param zoom   the zoom-out level, 0 is fully zoomed in.
	 * @return the path to the Dynmap tile image at
	 * <code>{world}/{map}/{regionX}_{regionZ}/{zoom}_{chunkX}_{chunkZ}.png</code>
	 */
	private @NotNull String getPath(String world, String map, int chunkX, int chunkZ, int zoom) {
		final int regionX = chunkX / 32;
		final int regionZ = chunkZ / 32;
		final String zoomStr = (zoom > 0) ? Strings.repeat("z", zoom) + "_" : "";
		String extension = config.getString("extension", "png");
		
		return String.format("tiles/%s/%s/%d_%d/%s%d_%d.%s",
				world, map, regionX, regionZ, zoomStr, chunkX, chunkZ, extension);
	}
	
	/**
	 * Get the file where the image at the given location is to be stored.
	 * The instant gets formatted in ISO 8601 basic format, truncated to minutes
	 * (for example, <code>20220804T213200Z</code>).
	 *
	 * @param now    the current time
	 * @param chunkX the chunk X coordinate
	 * @param chunkZ the chunk Y coordinate
	 * @return the file at location <code>plugins/DynmapExport/exports/{now}/{x}_{y}.png</code>
	 */
	private @NotNull File getDestFile(@NotNull Instant now, int chunkX, int chunkZ) {
		// Convert extended format to basic format without separators (which are problematic in filenames)
		// https://stackoverflow.com/a/39820917
		String datetime = now.truncatedTo(ChronoUnit.MINUTES).toString()
				.replace("-", "")
				.replace(":", "");
		File directory = new File(getDataFolder(), "exports/" + datetime);
		return new File(directory, String.format("%d_%d.png", chunkX, chunkZ));
	}
	
}
