package nl.dantevg.dynmapexport;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ExportCache {
	private static final String FILENAME = "last-export.yml";
	
	private final DynmapExport plugin;
	private final File file;
	private final Map<TileGroupCoords, Instant> lastExport = new HashMap<>();
	
	public ExportCache(DynmapExport plugin) {
		this.plugin = plugin;
		file = new File(plugin.getDataFolder(), FILENAME);
		load();
	}
	
	/**
	 * Returns whether this tile group has been exported at least once before.
	 *
	 * @param tileGroupCoords the tile group to check
	 * @return whether the tile group at <code>tileGroupCoords</code> has been
	 * exported before
	 */
	public boolean isCached(TileGroupCoords tileGroupCoords) {
		return lastExport.containsKey(tileGroupCoords);
	}
	
	/**
	 * Get the latest date/time at which this tile group was exported, or
	 * <code>null</code> if it was never exported before.
	 *
	 * @param tileGroupCoords the tile group to check
	 * @return the latest date/time at which the tile group at <code>tileGroupCoords</code>
	 * was exported
	 */
	public Instant getLastExport(TileGroupCoords tileGroupCoords) {
		return lastExport.get(tileGroupCoords);
	}
	
	/**
	 * Set the time at which this tile group was last exported.
	 *
	 * @param tileGroupCoords the tile group to set the time for
	 * @param instant         the time at which the tile group at <code>tileGroupCoords</code>
	 *                        was exported
	 */
	public void setLastExport(TileGroupCoords tileGroupCoords, Instant instant) {
		lastExport.put(tileGroupCoords, instant);
	}
	
	/**
	 * Load the latest export times from a file, if it exists yet.
	 * @see ExportCache#FILENAME
	 */
	private void load() {
		YamlConfiguration lastExportYaml = new YamlConfiguration();
		try {
			lastExportYaml.load(file);
		} catch (FileNotFoundException e) {
			// Ignore, the file did not exist yet but will be created on server close
		} catch (IOException | InvalidConfigurationException e) {
			plugin.logger.log(Level.WARNING, "Could not read " + FILENAME, e);
			return;
		}
		
		for (String tileGroupCoords : lastExportYaml.getKeys(false)) {
			lastExport.put(
					TileGroupCoords.parse(tileGroupCoords),
					Instant.parse((String) lastExportYaml.get(tileGroupCoords)));
		}
	}
	
	/**
	 * Save the latest export times to a file.
	 * @see ExportCache#FILENAME
	 */
	public void save() {
		YamlConfiguration lastExportYaml = new YamlConfiguration();
		lastExport.forEach((tileGroupCoords, instant) ->
				lastExportYaml.set(tileGroupCoords.toString(), instant.toString()));
		
		try {
			lastExportYaml.save(file);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not store " + FILENAME, e);
		}
	}
	
}
