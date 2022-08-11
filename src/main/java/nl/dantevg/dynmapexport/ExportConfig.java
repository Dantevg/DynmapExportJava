package nl.dantevg.dynmapexport;

public class ExportConfig {
	public final DynmapWebAPI.World world;
	public final DynmapWebAPI.Map map;
	public final int zoom;
	
	public final TileLocation from;
	public final TileLocation to;
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, TileLocation from, TileLocation to) {
		this.world = world;
		this.map = map;
		this.zoom = zoom;
		this.from = from;
		this.to = to;
	}
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom, TileLocation tile) {
		this(world, map, zoom, tile, tile);
	}
	
}
