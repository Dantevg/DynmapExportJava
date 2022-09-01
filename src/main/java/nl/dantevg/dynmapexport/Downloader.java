package nl.dantevg.dynmapexport;

import nl.dantevg.dynmapexport.location.TileCoords;
import nl.dantevg.dynmapexport.location.WorldCoords;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
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
		
		TileCoords tile = new WorldCoords(x, DynmapExport.Y_LEVEL, z).toTileCoords(map, zoom);
		ExportConfig config = new ExportConfig(world, map, zoom, tile);
		return downloadTile(config, tile);
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param config     the export configuration
	 * @param tileCoords the tile coordinates
	 * @return the path to the downloaded file
	 */
	public @Nullable String downloadTile(@NotNull ExportConfig config, @NotNull TileCoords tileCoords) {
		String tilePath = Paths.getDynmapTilePath(config, tileCoords);
		File dest = Paths.getLocalTileFile(plugin, config, Instant.now(), tileCoords);
		return download(tilePath, dest) ? dest.getPath() : null;
	}
	
	/**
	 * Download multiple tiles in the rectangle between <code>from</code> and <code>to</code> (inclusive)
	 *
	 * @param config the export configuration
	 * @return the amount of tiles downloaded, or -1 if nothing changed in the Dynmap
	 */
	public Map<TileCoords, File> downloadTiles(@NotNull ExportConfig config, Instant now) {
		Instant cached = plugin.imageTresholdCache.getCachedInstant(config);
		List<TileCoords> tiles = configToTileLocations(config);
		
		Map<TileCoords, File> downloadedFiles = new HashMap<>();
		for (TileCoords tile : tiles) {
			String tilePath = Paths.getDynmapTilePath(config, tile);
			File dest = Paths.getLocalTileFile(plugin, config, now, tile);
			downloadedFiles.put(tile, dest);
			download(tilePath, dest);
		}
		
		// Not enough changes, remove tile files and directory again
		if (downloadedFiles.size() > 0
				&& !plugin.imageTresholdCache.anyChangedSince(cached, config, downloadedFiles.values())) {
			removeExportedTiles(downloadedFiles.values());
			return null;
		}
		
		return downloadedFiles;
	}
	
	public static void removeExportedTiles(Collection<File> files) {
		File dir = files.stream().findAny().get().getParentFile();
		
		// Delete downloaded tile files
		for (File file : files) file.delete();
		
		// Delete parent directory
		dir.delete();
	}
	
	/**
	 * Get all tile locations from an export config.
	 *
	 * @param config the export config to get the tile locations of
	 * @return a list of tiles that are within the range from the config
	 */
	public static @NotNull List<TileCoords> configToTileLocations(@NotNull ExportConfig config) {
		List<TileCoords> tiles = new ArrayList<>();
		
		for (int x = config.from.x; x <= config.to.x; x += 1 << config.zoom) {
			for (int y = config.from.y; y <= config.to.y; y += 1 << config.zoom) {
				tiles.add(new TileCoords(x, y));
			}
		}
		
		return tiles;
	}
	
	/**
	 * Download the Dynmap tile at <code>path</code> to <code>dest</code>.
	 *
	 * @param path the Dynmap path to the tile
	 * @param dest the destination file to download to.
	 * @return whether the download succeeded
	 */
	private boolean download(String path, @NotNull File dest) {
		try {
			URL url = new URL(String.format("http://%s/%s", plugin.dynmapHost, path));
			InputStream inputStream = url.openStream();
			dest.getParentFile().mkdirs(); // Make all directories on path to file
			long bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			plugin.logger.log(Level.CONFIG, "Downloaded tile " + path);
			if (bytesWritten == 0) plugin.logger.log(Level.WARNING, "Tile was 0 bytes!");
			return bytesWritten > 0;
		} catch (MalformedURLException e) {
			plugin.logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			plugin.logger.log(Level.SEVERE, "Could not download tile", e);
		}
		return false;
	}
	
}
