package nl.dantevg.dynmapexport;

import nl.dantevg.dynmapexport.location.TileCoords;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Level;

public class TileCombiner {
	private static final int PIXELS_PER_TILE = 128;
	
	private final DynmapExport plugin;
	
	public TileCombiner(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	public @Nullable BufferedImage combine(ExportConfig config, Instant instant) {
		int width = tileCoordsToPixelX(config, config.to) + PIXELS_PER_TILE;
		int height = tileCoordsToPixelY(config, config.from) + PIXELS_PER_TILE;
		
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = output.createGraphics();
		
		for (TileCoords tile : Downloader.configToTileLocations(config)) {
			if (!drawTile(config, instant, graphics, tile)) return null;
		}
		
		return output;
	}
	
	public boolean combineAndSave(ExportConfig config, Instant instant) {
		BufferedImage result = combine(config, instant);
		if (result == null) return false;
		File file = Paths.getLocalCombinedFile(plugin, config, instant);
		try {
			ImageIO.write(result, "png", file);
			return true;
		} catch (IOException e) {
			plugin.logger.log(Level.SEVERE, "Cannot save combined image to " + file, e);
			return false;
		}
	}
	
	private boolean drawTile(ExportConfig config, Instant instant, Graphics2D graphics, TileCoords tile) {
		File tileFile = Paths.getLocalTileFile(plugin, config, instant, tile);
		BufferedImage tileImage;
		try {
			tileImage = ImageIO.read(tileFile);
		} catch (IOException e) {
			plugin.logger.log(Level.SEVERE, "Cannot read image from file " + tileFile, e);
			return false;
		}
		int x = tileCoordsToPixelX(config, tile);
		int y = tileCoordsToPixelY(config, tile);
		graphics.drawImage(tileImage, x, y, null);
		return true;
	}
	
	private static int tileCoordsToPixelX(ExportConfig config, TileCoords tile) {
		return (tile.x - config.from.x) / (1 << config.zoom) * PIXELS_PER_TILE;
	}
	
	private static int tileCoordsToPixelY(ExportConfig config, TileCoords tile) {
		return (config.to.y - tile.y) / (1 << config.zoom) * PIXELS_PER_TILE;
	}
	
}
