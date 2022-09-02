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
		this.from = new TileCoords(Math.min(from.x, to.x), Math.min(from.y, to.y)).floorToZoom(zoom);
		this.to = new TileCoords(Math.max(from.x, to.x), Math.max(from.y, to.y)).ceilToZoom(zoom);
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
	
}
