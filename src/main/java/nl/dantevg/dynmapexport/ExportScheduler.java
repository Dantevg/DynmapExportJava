package nl.dantevg.dynmapexport;

import com.google.common.io.Files;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

public class ExportScheduler {
	private static final String FILENAME = "last-export.txt";
	
	private final @NotNull DynmapExport plugin;
	private final @NotNull File lastExportFile;
	private Instant lastExport;
	
	public ExportScheduler(@NotNull DynmapExport plugin) {
		this.plugin = plugin;
		lastExportFile = new File(plugin.getDataFolder(), FILENAME);
		loadLastExport();
		
		if (plugin.config.contains("schedule")) {
			String schedule = plugin.config.getString("schedule");
			Duration duration;
			try {
				duration = Duration.parse("PT" + schedule);
			} catch (DateTimeParseException e) {
				plugin.logger.log(Level.WARNING, "Invalid schedule format (only seconds, minutes, hours allowed!)");
				return;
			}
			startScheduledTask(duration);
		}
	}
	
	private void loadLastExport() {
		try {
			lastExport = Instant.parse(Files.toString(lastExportFile, StandardCharsets.UTF_8));
		} catch (IOException e) {
			// Most likely the file did not exist yet, will be created later
			lastExport = Instant.EPOCH;
		}
	}
	
	private void saveLastExport() {
		try {
			Files.write(lastExport.toString(), lastExportFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			plugin.logger.log(Level.WARNING, "Could not save last export time", e);
		}
	}
	
	private void startScheduledTask(@NotNull Duration duration) {
		Duration delay = Duration.between(Instant.now(), lastExport.plus(duration));
		if (delay.isNegative()) delay = Duration.ZERO;
		
		new ExportTask().runTaskTimerAsynchronously(
				plugin,
				delay.getSeconds() * 20,
				duration.getSeconds() * 20);
		
		String durationStr = DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true);
		String delayStr = DurationFormatUtils.formatDurationWords(delay.toMillis(), true, true);
		plugin.logger.log(Level.INFO, "Scheduled export every " + durationStr + " starts in " + delayStr);
	}
	
	private class ExportTask extends BukkitRunnable {
		@Override
		public void run() {
			plugin.export();
			lastExport = Instant.now();
			saveLastExport();
		}
		
	}
}
