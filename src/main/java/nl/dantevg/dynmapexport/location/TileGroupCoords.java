package nl.dantevg.dynmapexport.location;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TileGroupCoords {
	public static final String SEPARATOR = "_";
	
	public final int x, y;
	
	public TileGroupCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public @NotNull String toString() {
		return x + SEPARATOR + y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TileGroupCoords that = (TileGroupCoords) o;
		return x == that.x && y == that.y;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	public static @NotNull TileGroupCoords parse(@NotNull String str) {
		int separator = str.indexOf(SEPARATOR);
		int x = Integer.parseInt(str.substring(0, separator - 1));
		int y = Integer.parseInt(str.substring(separator + 1));
		return new TileGroupCoords(x, y);
	}
}
