package nl.dantevg.dynmapexport;

public class ExportConfig {
	public final DynmapWebAPI.World world;
	public final DynmapWebAPI.Map map;
	public final int zoom;
	
	public ExportConfig(DynmapWebAPI.World world, DynmapWebAPI.Map map, int zoom) {
		this.world = world;
		this.map = map;
		this.zoom = zoom;
	}
	
	public ExportConfig(DynmapWebAPI.Configuration worldConfiguration, String worldName, String mapName, int zoom) {
		DynmapWebAPI.World world = worldConfiguration.getWorldByName(worldName);
		if (world == null) throw new IllegalArgumentException("not a valid world");
		
		DynmapWebAPI.Map map = world.getMapByName(mapName);
		if (map == null) throw new IllegalArgumentException("not a valid map");
		
		this.world = world;
		this.map = map;
		this.zoom = zoom;
	}
	
}
