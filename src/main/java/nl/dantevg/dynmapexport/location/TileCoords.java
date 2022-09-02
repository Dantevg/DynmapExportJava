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
	
}
