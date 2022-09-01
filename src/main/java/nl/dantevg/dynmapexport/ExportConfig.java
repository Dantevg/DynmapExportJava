package nl.dantevg.dynmapexport;

import nl.dantevg.dynmapexport.location.TileCoords;
import nl.dantevg.dynmapexport.location.WorldCoords;
import org.jetbrains.annotations.NotNull;

public class ExportConfig {
	public final DynmapWebAPI.World world;
	public final DynmapWebAPI.Map map;
	public final int zoom;
	
	public final TileCoords from;
	public final TileCoords to;
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, TileCoords from, TileCoords to) {
		this.world = world;
		this.map = map;
		this.zoom = zoom;
		this.from = new TileCoords(
				zoomedFloor(Math.min(from.x, to.x), zoom),
				zoomedFloor(Math.min(from.y, to.y), zoom));
		this.to = new TileCoords(
				zoomedCeil(Math.max(from.x, to.x), zoom),
				zoomedCeil(Math.max(from.y, to.y), zoom));
	}
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, TileCoords tile) {
		this(world, map, zoom, tile, tile);
	}
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, @NotNull WorldCoords from, @NotNull WorldCoords to) {
		this(world, map, zoom, from.toTileCoords(map, zoom), to.toTileCoords(map, zoom));
	}
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, @NotNull WorldCoords coords) {
		this(world, map, zoom, coords.toTileCoords(map, zoom));
	}
	
	private static int zoomedFloor(int value, int zoom) {
		return value / (1 << zoom) * (1 << zoom);
	}
	
	private static int zoomedCeil(int value, int zoom) {
		return (int) Math.ceil((double) value / (1 << zoom)) * (1 << zoom);
	}
	
}
