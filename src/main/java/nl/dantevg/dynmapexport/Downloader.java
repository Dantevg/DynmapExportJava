package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import com.google.gson.Gson;
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

public class Downloader {
	private final DynmapExport plugin;
	private final DynmapWebAPI.Configuration worldConfiguration;
	
	public Downloader(DynmapExport plugin) {
		this.plugin = plugin;
		worldConfiguration = getDynmapConfiguration();
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param world the name of the world (e.g. <code>"world"</code>)
	 * @param map   the name of the map, or "prefix" (e.g. <code>"flat"</code>)
	 * @param x     the in-game block x-coordinate
	 * @param z     the in-game block z-coordinate
	 * @param zoom  the zoom-out level, 0 is fully zoomed in.
	 * @return the path to the downloaded file
	 */
	public @Nullable String downloadTile(String world, String map, int x, int z, int zoom) {
		return downloadTile(new DynmapLocation(worldConfiguration, world, map, new Vector(x, DynmapExport.Y_LEVEL, z), zoom));
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param location the location of the tile
	 * @return the path to the downloaded file
	 */
	public @Nullable String downloadTile(DynmapLocation location) {
		File dest = getDestFile(Instant.now(), location);
		String tilePath = getPath(location);
		return download(tilePath, dest) ? dest.getPath() : null;
	}
	
	/**
	 * Download the world configuration from Dynmap, which is used to determine
	 * the tile coordinates from world coordinates.
	 *
	 * @return the world configuration
	 */
	private @Nullable DynmapWebAPI.Configuration getDynmapConfiguration() {
		int port = plugin.config.getInt("dynmap-port");
		try {
			URL url = new URL(String.format("http://localhost:%d/up/configuration", port));
			InputStreamReader reader = new InputStreamReader(url.openStream());
			return new Gson().fromJson(reader, DynmapWebAPI.Configuration.class);
		} catch (MalformedURLException e) {
			plugin.logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			plugin.logger.log(Level.SEVERE, "Could not download dynmap worlds configuration", e);
		}
		return null;
	}
	
	/**
	 * Download the Dynmap tile at <code>path</code> to <code>dest</code>.
	 *
	 * @param path the Dynmap path to the tile
	 * @param dest the destination file to download to.
	 * @return whether the download succeeded
	 */
	private boolean download(String path, @NotNull File dest) {
		int port = plugin.config.getInt("dynmap-port");
		
		try {
			URL url = new URL(String.format("http://localhost:%d/%s", port, path));
			InputStream inputStream = url.openStream();
			dest.getParentFile().mkdirs(); // Make all directories on path to file
			long bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			plugin.logger.log(Level.INFO, "Downloaded tile " + path);
			if (bytesWritten == 0) plugin.logger.log(Level.WARNING, "Tile was 0 bytes!");
			return bytesWritten > 0;
		} catch (MalformedURLException e) {
			plugin.logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			plugin.logger.log(Level.SEVERE, "Could not download tile", e);
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
		File directory = new File(plugin.getDataFolder(), "exports/latest");
		return new File(directory, String.format("%d_%d.png", location.getTileX(), location.getTileY()));
	}
	
}
