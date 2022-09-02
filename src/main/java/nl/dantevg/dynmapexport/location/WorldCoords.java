package nl.dantevg.dynmapexport.location;

import nl.dantevg.dynmapexport.DynmapWebAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorldCoords {
	public static final int SCALE_FACTOR = 128;
	public static final String SEPARATOR = ",";
	
	public final int x, y, z;
	
	public WorldCoords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public @NotNull TileCoords toTileCoords(@NotNull DynmapWebAPI.Map map, int zoom) {
		double unscaledX = (int) map.worldtomap[0] * x
				+ (int) map.worldtomap[1] * y
				+ (int) map.worldtomap[2] * z;
		int tileX = (int) (unscaledX / SCALE_FACTOR / (1 << zoom)) * (1 << zoom);
		
		double unscaledY = (int) map.worldtomap[3] * x
				+ (int) map.worldtomap[4] * y
				+ (int) map.worldtomap[5] * z;
		int tileY = (int) Math.ceil((unscaledY / SCALE_FACTOR - 1) / (1 << zoom)) * (1 << zoom);
		
		return new TileCoords(tileX, tileY);
	}
	
	@Override
	public String toString() {
		return x + SEPARATOR + y + SEPARATOR + z;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WorldCoords that = (WorldCoords) o;
		return x == that.x && y == that.y && z == that.z;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
