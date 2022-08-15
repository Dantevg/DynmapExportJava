package nl.dantevg.dynmapexport;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Level;

public class ExportCache {
	private final DynmapExport plugin;
	
	public ExportCache(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Check whether any of the tile groups have changed since the last hash.
	 *
	 * @param config     the config for the export
	 * @param tileGroups the set of tile groups to check
	 * @return whether any of the tile groups have changed
	 */
	public boolean anyChanged(ExportConfig config, Set<TileGroupCoords> tileGroups) {
		return tileGroups.stream().anyMatch(tileGroupCoords -> hasChanged(config, tileGroupCoords));
	}
	
	/**
	 * Check whether the tile has changed since the last hash.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group to check
	 * @return whether the hash has changed since the last export of this tile group
	 */
	public boolean hasChanged(ExportConfig config, TileGroupCoords tileGroupCoords) {
		String cached = getCachedHash(config, tileGroupCoords);
		String latest = getLatestHash(config, tileGroupCoords);
		return cached == null || !cached.equals(latest);
	}
	
	/**
	 * Get the last stored hash.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group to get the hash for
	 * @return the last stored hash of the tile group
	 */
	private @Nullable String getCachedHash(ExportConfig config, TileGroupCoords tileGroupCoords) {
		File file = getCachedHashFile(config, tileGroupCoords);
		if (!file.exists()) return null;
		try {
			return Files.toString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not read stored hash", e);
		}
		return null;
	}
	
	/**
	 * Get the newest hash from Dynmap.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group to get the hash for
	 * @return the newest hash of the tile group
	 */
	private @Nullable String getLatestHash(ExportConfig config, TileGroupCoords tileGroupCoords) {
		try {
			URL url = new URL(String.format("http://localhost:%d/tiles/%s/%s_%s.hash",
					plugin.dynmapPort,
					config.world.name,
					config.map.prefix, tileGroupCoords));
			InputStream inputStream = url.openStream();
			String hash = CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			saveHash(config, tileGroupCoords, hash);
			return hash;
		} catch (MalformedURLException e) {
			plugin.logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not download hash", e);
		}
		return null;
	}
	
	/**
	 * Write the hash to the correct location.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group this hash is for
	 * @param hash            the hash of the tile group
	 */
	private void saveHash(ExportConfig config, TileGroupCoords tileGroupCoords, String hash) {
		File file = getCachedHashFile(config, tileGroupCoords);
		file.getParentFile().mkdirs();
		try {
			Files.write(hash, file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not save hash", e);
		}
	}
	
	/**
	 * Get the file where the cached hash is stored.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group the hash is for
	 * @return the file where the cached hash is stored
	 */
	private File getCachedHashFile(ExportConfig config, TileGroupCoords tileGroupCoords) {
		return new File(plugin.getDataFolder(), String.format("exports/%s/%s_%s.hash",
				config.world.name,
				config.map.prefix, tileGroupCoords));
	}
	
}
