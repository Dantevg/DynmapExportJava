package nl.dantevg.dynmapexport;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import nl.dantevg.dynmapexport.location.TileCoords;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Paths {
	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
					.withZone(ZoneId.from(ZoneOffset.UTC));
	
	/**
	 * Basic instant format without separators (which are problematic in filenames)
	 * https://stackoverflow.com/a/39820917
	 *
	 * @return DateTimeFormatter of the basic ISO 8601 format
	 */
	public static @NotNull DateTimeFormatter getInstantFormat() {
		return dateTimeFormatter;
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
	public static @NotNull String getDynmapTilePath(@NotNull ExportConfig config, @NotNull TileCoords tile) {
		return String.format("tiles/%s/%s/%s/%s%d_%d.png",
				config.world.name,
				config.map.prefix,
				tile.getTileGroupCoords(),
				getZoomString(config.zoom), tile.x, tile.y);
	}
	
	/**
	 * Get the local map directory.
	 *
	 * @param plugin the DynmapExport plugin
	 * @param config the export configuration
	 * @return the local map directory at <code>plugins/DynmapExport/exports/{world}/{map}/</code>
	 */
	public static @NotNull File getLocalMapDir(@NotNull DynmapExport plugin, @NotNull ExportConfig config) {
		return new File(plugin.getDataFolder(),
				String.format("exports/%s/%s", config.world.name, config.map.name));
	}
	
	/**
	 * Get the local directory of a single export.
	 *
	 * @param plugin  the DynmapExport plugin
	 * @param config  the export configuration
	 * @param instant the time of the export
	 * @return the local export directory at <code>plugins/DynmapExport/exports/{world}/{map}/{instant}/</code>
	 */
	public static @NotNull File getLocalExportDir(@NotNull DynmapExport plugin,
	                                              @NotNull ExportConfig config,
	                                              @NotNull Instant instant) {
		return new File(getLocalMapDir(plugin, config), getInstantFormat().format(instant));
	}
	
	/**
	 * Get the local file for the image in a single export at the given location.
	 * The instant gets formatted in ISO 8601 basic format, truncated to seconds
	 * (for example, <code>20220804T213215Z</code>).
	 *
	 * @param plugin  the DynmapExport plugin
	 * @param config  the export configuration
	 * @param instant the time of the export
	 * @param tile    the Dynmap tile coordinates
	 * @return the local file of the tile at location
	 * <code>plugins/DynmapExport/exports/{world}/{map}/{instant}/{zoom}_{tileX}_{tileY}.png</code>
	 */
	public static @NotNull File getLocalTileFile(@NotNull DynmapExport plugin,
	                                             @NotNull ExportConfig config,
	                                             @NotNull Instant instant,
	                                             @NotNull TileCoords tile) {
		return new File(getLocalExportDir(plugin, config, instant),
				String.format("%s%d_%d.png", getZoomString(config.zoom), tile.x, tile.y));
	}
	
	/**
	 * Get the local file for the combined image of a single export.
	 *
	 * @param plugin  the DynmapExport plugin
	 * @param config  the export configuration
	 * @param instant the time of the export
	 * @return the local file of the combined image at location
	 * <code>plugins/DynmapExport/exports/{world}/{map}/{instant}.png</code>
	 */
	public static @NotNull File getLocalCombinedFile(@NotNull DynmapExport plugin,
	                                                 @NotNull ExportConfig config,
	                                                 @NotNull Instant instant) {
		return new File(getLocalMapDir(plugin, config), getInstantFormat().format(instant) + ".png");
	}
	
	public static @NotNull String getZoomString(int zoom) {
		return (zoom > 0) ? Strings.repeat("z", zoom) + "_" : "";
	}
	
	public static @NotNull Instant getInstantFromFile(File file) {
		return Instant.from(getInstantFormat().parse(
				Files.getNameWithoutExtension(file.getName())));
	}
	
}
