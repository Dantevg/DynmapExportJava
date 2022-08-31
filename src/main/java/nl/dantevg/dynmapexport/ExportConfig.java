package nl.dantevg.dynmapexport;

import nl.dantevg.dynmapexport.location.TileCoords;

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
		this.from = from;
		this.to = to;
	}
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, TileCoords tile) {
		this(world, map, zoom, tile, tile);
	}
	
}
