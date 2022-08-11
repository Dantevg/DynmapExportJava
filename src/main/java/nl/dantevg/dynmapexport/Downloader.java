package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import com.google.gson.Gson;
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
	 * @param worldName the name of the world (e.g. <code>"world"</code>)
	 * @param mapName   the name of the map, or "prefix" (e.g. <code>"flat"</code>)
	 * @param x         the in-game block x-coordinate
	 * @param z         the in-game block z-coordinate
	 * @param zoom      the zoom-out level, 0 is fully zoomed in.
	 * @return the path to the downloaded file
	 */
	public @Nullable String downloadTile(String worldName, String mapName, int x, int z, int zoom) {
		DynmapWebAPI.World world = worldConfiguration.getWorldByName(worldName);
		if (world == null) throw new IllegalArgumentException("not a valid world");
		
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) throw new IllegalArgumentException("not a valid map");
		
		TileLocation tile = new WorldLocation(x, DynmapExport.Y_LEVEL, z).toTileLocation(map, zoom);
		ExportConfig config = new ExportConfig(world, map, zoom, tile);
		return downloadTile(config, tile);
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param config       the export configuration
	 * @param tileLocation the tile coordinates
	 * @return the path to the downloaded file
	 */
	public @Nullable String downloadTile(ExportConfig config, TileLocation tileLocation) {
		File dest = getDestFile(Instant.now(), tileLocation);
		String tilePath = getPath(config, tileLocation);
		return download(tilePath, dest) ? dest.getPath() : null;
	}
	
	/**
	 * Download multiple tiles in the rectangle between <code>from</code> and <code>to</code> (inclusive)
	 *
	 * @param config the export configuration
	 * @param from   the first tile corner
	 * @param to     the second tile corner, diagonally opposing <code>from</code>
	 * @return the amount of tiles downloaded
	 */
	public int downloadTiles(ExportConfig config, TileLocation from, TileLocation to) {
		int nDownloaded = 0;
		Instant now = Instant.now();
		
		for (int x = Math.min(from.x, to.x); x < Math.max(from.x, to.x); x++) {
			for (int y = Math.min(from.y, to.y); y < Math.max(from.y, to.y); y++) {
				TileLocation tile = new TileLocation(x, y);
				File dest = getDestFile(now, tile);
				if (download(getPath(config, tile), dest)) nDownloaded++;
			}
		}
		
		return nDownloaded;
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
	 * See <a href="https://github.com/webbukkit/dynmap/blob/f89777a0dd1ac9e17f595ef0361a030f53eff92a/DynmapCore/src/main/java/org/dynmap/storage/filetree/FileTreeMapStorage.java#L46-L53">
	 * https://github.com/webbukkit/dynmap/blob/f89777a0dd1ac9e17f595ef0361a030f53eff92a/DynmapCore/src/main/java/org/dynmap/storage/filetree/FileTreeMapStorage.java#L46-L53</a>
	 *
	 * @param config the export configuration
	 * @param tile   the Dynmap tile coordinates
	 * @return the path to the Dynmap tile image at
	 * <code>{world}/{map}/{regionX}_{regionZ}/{zoom}_{tileX}_{tileY}.png</code>
	 */
	private @NotNull String getPath(ExportConfig config, TileLocation tile) {
		String zoomStr = (config.zoom > 0) ? Strings.repeat("z", config.zoom) + "_" : "";
		
		return String.format("tiles/%s/%s/%d_%d/%s%d_%d.png",
				config.world.name,
				config.map.prefix,
				tile.x >> 5, tile.y >> 5,
				zoomStr, tile.x, tile.y);
	}
	
	/**
	 * Get the file where the image at the given location is to be stored.
	 * The instant gets formatted in ISO 8601 basic format, truncated to minutes
	 * (for example, <code>20220804T213200Z</code>).
	 *
	 * @param now  the current time
	 * @param tile the Dynmap tile coordinates
	 * @return the file at location <code>plugins/DynmapExport/exports/{now}/{tileX}_{tileY}.png</code>
	 */
	private @NotNull File getDestFile(@NotNull Instant now, TileLocation tile) {
		// Convert extended format to basic format without separators (which are problematic in filenames)
		// https://stackoverflow.com/a/39820917
		String datetime = now.truncatedTo(ChronoUnit.MINUTES).toString()
				.replace("-", "")
				.replace(":", "");
		File directory = new File(plugin.getDataFolder(), "exports/latest");
		return new File(directory, String.format("%d_%d.png", tile.x, tile.y));
	}
	
}
