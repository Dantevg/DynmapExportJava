package nl.dantevg.dynmapexport.location;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TileCoords {
	public static final String SEPARATOR = ",";
	
	public final int x, y;
	
	public TileCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public @NotNull TileGroupCoords getTileGroupCoords() {
		return new TileGroupCoords(x >> 5, y >> 5);
	}
	
	public @NotNull TileCoords floorToZoom(int zoom) {
		return new TileCoords(zoomedFloor(x, zoom), zoomedFloor(y, zoom));
	}
	
	public @NotNull TileCoords ceilToZoom(int zoom) {
		return new TileCoords(zoomedCeil(x, zoom), zoomedCeil(y, zoom));
	}
	
	@Override
	public @NotNull String toString() {
		return x + SEPARATOR + y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TileCoords that = (TileCoords) o;
		return x == that.x && y == that.y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	public static @NotNull TileCoords parse(@NotNull String str) {
		int separator = str.indexOf(SEPARATOR);
		int x = Integer.parseInt(str.substring(0, separator - 1));
		int y = Integer.parseInt(str.substring(separator + 1));
		return new TileCoords(x, y);
	}
	
	public static int zoomedFloor(double value, int zoom) {
		return (int) Math.floor(value / (1 << zoom)) * (1 << zoom);
	}
	
	public static int zoomedCeil(double value, int zoom) {
		return (int) Math.ceil(value / (1 << zoom)) * (1 << zoom);
	}
	
}
