package nl.dantevg.dynmapexport.cache;

import nl.dantevg.dynmapexport.DynmapExport;
import nl.dantevg.dynmapexport.ExportConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

public class ImageTresholdCache {
	private final DynmapExport plugin;
	private final double treshold = 0.1;
	
	public ImageTresholdCache(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	public boolean anyChanged(Set<File> files) {
		// TODO: implement
		return true;
	}
	
	public boolean hasChanged(ExportConfig config, File file) {
		try {
			return hasChanged(config, ImageIO.read(file));
		} catch (IOException e) {
			// TODO: log error
			return true;
		}
	}
	
	private boolean hasChanged(ExportConfig config, BufferedImage image) throws IOException {
		File cachedImageFile = getCachedImageFile(config);
		if (!cachedImageFile.exists()) return true;
		
		BufferedImage from = ImageIO.read(cachedImageFile);
		return getFractionPixelsChanged(from, image) >= treshold;
	}
	
	private double getFractionPixelsChanged(BufferedImage from, BufferedImage to) {
		int pixelsChanged = getNPixelsChanged(from, to);
		int totalPixels = to.getWidth() * to.getHeight();
		return (double) pixelsChanged / totalPixels;
	}
	
	private int getNPixelsChanged(BufferedImage from, BufferedImage to) {
		assert from.getWidth() == to.getWidth();
		assert from.getHeight() == to.getHeight();
		
		// TODO: implement correctly?
		int changed = 0;
		for (int x = 0; x < from.getWidth(); x++) {
			for (int y = 0; y < from.getHeight(); y++) {
				if (from.getRGB(x, y) != to.getRGB(x, y)) changed++;
			}
		}
		
		return changed;
	}
	
	private File getCachedImageFile(ExportConfig config) {
		File mapDir = new File(plugin.getDataFolder(),
				String.format("exports/%s/%s", config.world.name, config.map.prefix));
		return Arrays.stream(mapDir.listFiles())
				.map(File::getName) // file -> filename
				.map(Instant::parse) // filename -> instant
				.sorted()
				.reduce((a, b) -> b) // get latest instant
				.map(instant -> new File(mapDir, instant.toString()))
				.orElse(null);
	}
	
}
