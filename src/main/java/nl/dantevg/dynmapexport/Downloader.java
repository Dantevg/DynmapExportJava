package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

public class Downloader {
	private final DynmapExport plugin;
	
	public Downloader(DynmapExport plugin) {
		this.plugin = plugin;
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
		DynmapWebAPI.World world = plugin.worldConfiguration.getWorldByName(worldName);
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
		String tilePath = getPath(config, tileLocation);
		File dest = getDestFile(Instant.now(), tilePath);
		return download(tilePath, dest) ? dest.getPath() : null;
	}
	
	/**
	 * Download multiple tiles in the rectangle between <code>from</code> and <code>to</code> (inclusive)
	 *
	 * @param config the export configuration
	 * @return the amount of tiles downloaded
	 */
	public int downloadTiles(ExportConfig config) {
		int nDownloaded = 0;
		Instant now = Instant.now();
		
		int minX = zoomedFloor(Math.min(config.from.x, config.to.x), config.zoom);
		int maxX = zoomedCeil(Math.max(config.from.x, config.to.x), config.zoom);
		int minY = zoomedFloor(Math.min(config.from.y, config.to.y), config.zoom);
		int maxY = zoomedCeil(Math.max(config.from.y, config.to.y), config.zoom);
		
		for (int x = minX; x <= maxX; x += 1 << config.zoom) {
			for (int y = minY; y <= maxY; y += 1 << config.zoom) {
				TileLocation tile = new TileLocation(x, y);
				String tilePath = getPath(config, tile);
				File dest = getDestFile(now, tilePath);
				if (download(tilePath, dest)) nDownloaded++;
			}
		}
		
		return nDownloaded;
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
		return String.format("tiles/%s/%s/%d_%d/%s%d_%d.png",
				config.world.name,
				config.map.prefix,
				tile.x >> 5, tile.y >> 5,
				getZoomString(config.zoom), tile.x, tile.y);
	}
	
	/**
	 * Get the file where the image at the given location is to be stored.
	 * The instant gets formatted in ISO 8601 basic format, truncated to seconds
	 * (for example, <code>20220804T213215Z</code>).
	 *
	 * @param now  the current time
	 * @param tilePath the path to the source tile
	 * @return the file at location <code>plugins/DynmapExport/exports/{now}/{tileX}_{tileY}.png</code>
	 */
	private @NotNull File getDestFile(@NotNull Instant now, String tilePath) {
		// Convert extended format to basic format without separators (which are problematic in filenames)
		// https://stackoverflow.com/a/39820917
		String datetime = now.truncatedTo(ChronoUnit.SECONDS).toString()
				.replace("-", "")
				.replace(":", "");
		File directory = new File(plugin.getDataFolder(), "exports/" + datetime);
		return new File(directory, new File(tilePath).getName());
	}
	
	private static String getZoomString(int zoom) {
		return (zoom > 0) ? Strings.repeat("z", zoom) + "_" : "";
	}
	
	private static int zoomedFloor(int value, int zoom) {
		return value / (1 << zoom) * (1 << zoom);
	}
	
	private static int zoomedCeil(int value, int zoom) {
		return (int) Math.ceil((double) value / (1 << zoom)) * (1 << zoom);
	}
	
}
