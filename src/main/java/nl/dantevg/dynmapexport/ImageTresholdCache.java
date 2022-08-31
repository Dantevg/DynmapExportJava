package nl.dantevg.dynmapexport;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

public class ImageTresholdCache {
	private final DynmapExport plugin;
	private final double treshold = 0.2;
	
	public ImageTresholdCache(DynmapExport plugin) {
		this.plugin = plugin;
	}
	
	public boolean anyChangedSince(Instant since, ExportConfig config, Set<File> files) {
		return files.stream().anyMatch(file -> hasChangedSince(since, config, file));
	}
	
	public Instant getCachedInstant(ExportConfig config) {
		File mapDir = getMapDir(config);
		if (!mapDir.isDirectory()) return null;
		DateTimeFormatter formatter = plugin.downloader.getInstantFormat();
		return Arrays.stream(mapDir.listFiles())
				.map(File::getName) // file -> filename
				.map(formatter::parse).map(Instant::from) // filename -> instant
				.sorted()
				.reduce((a, b) -> b) // get latest instant
				.orElse(null);
	}
	
	private boolean hasChangedSince(Instant since, ExportConfig config, File file) {
		if (since == null) return true;
		
		BufferedImage image;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not read image from " + file);
			return true;
		}
		
		File cachedImageFile = new File(
				new File(
						getMapDir(config),
						plugin.downloader.getInstantFormat().format(since)),
				file.getName());
		if (!cachedImageFile.exists()) return true;
		
		BufferedImage from;
		try {
			from = ImageIO.read(cachedImageFile);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not read image from " + cachedImageFile);
			return true;
		}
		double fraction = getFractionPixelsChanged(from, image);
		plugin.logger.log(Level.INFO, String.format("%f%% of %s changed", fraction, file.getName()));
		return fraction >= treshold;
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
	
	private File getMapDir(ExportConfig config) {
		return new File(plugin.getDataFolder(),
				String.format("exports/%s/%s", config.world.name, config.map.prefix));
	}
	
}
