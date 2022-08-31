package nl.dantevg.dynmapexport.location;

import nl.dantevg.dynmapexport.DynmapWebAPI;
import org.jetbrains.annotations.NotNull;

public class WorldCoords {
	public static final int SCALE_FACTOR = 128;
	
	public final int x, y, z;
	
	public WorldCoords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public @NotNull TileCoords toTileCoords(DynmapWebAPI.@NotNull Map map, int zoom) {
		double unscaledX = (int) map.worldtomap[0] * x
				+ (int) map.worldtomap[1] * y
				+ (int) map.worldtomap[2] * z;
		int tileX = (int) (unscaledX / SCALE_FACTOR / (1 << zoom)) * (1 << zoom);
		
		double unscaledY = (int) map.worldtomap[3] * x
				+ (int) map.worldtomap[4] * y
				+ (int) map.worldtomap[5] * z;
		int tileY = (int) Math.ceil(((unscaledY - SCALE_FACTOR) / SCALE_FACTOR) / (1 << zoom)) * (1 << zoom);
		
		return new TileCoords(tileX, tileY);
	}
	
}
