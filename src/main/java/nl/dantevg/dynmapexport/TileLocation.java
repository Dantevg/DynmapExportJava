package nl.dantevg.dynmapexport;

import org.jetbrains.annotations.NotNull;

public class TileLocation {
	public static final String SEPARATOR = ",";
	
	public final int x, y;
	
	public TileLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public @NotNull TileGroupCoords getTileGroupCoords() {
		return new TileGroupCoords(x >> 5, y >> 5);
	}
	
	@Override
	public @NotNull String toString() {
		return this.x + SEPARATOR + this.y;
	}
	
	public static @NotNull TileLocation parse(@NotNull String str) {
		int separator = str.indexOf(SEPARATOR);
		int x = Integer.parseInt(str.substring(0, separator - 1));
		int y = Integer.parseInt(str.substring(separator + 1));
		return new TileLocation(x, y);
	}
	
}
