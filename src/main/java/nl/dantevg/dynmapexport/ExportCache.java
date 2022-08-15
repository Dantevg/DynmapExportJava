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
import java.util.logging.Level;

public class ExportCache {
	private final DynmapExport plugin;
	
	public ExportCache(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Check whether the tile has changed since the last hash.
	 *
	 * @param config          the config for the export
	 * @param tileGroupCoords the tile group to check
	 * @return whether the hash has changed since the last export of this hash
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
		File file = new File(plugin.getDataFolder(), String.format("exports/%s/%s_%s.hash",
				config.world.name,
				config.map.prefix, tileGroupCoords));
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
			return CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		} catch (MalformedURLException e) {
			plugin.logger.log(Level.SEVERE, e.getMessage());
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not download hash", e);
		}
		return null;
	}
	
}
