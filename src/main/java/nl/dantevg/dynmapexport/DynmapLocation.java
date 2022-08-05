package nl.dantevg.dynmapexport;

import org.bukkit.util.Vector;

import java.util.logging.Level;

public class DynmapLocation {
	public static final int SCALE_FACTOR = 128;
	
	public final DynmapWebAPI.World world;
	public final DynmapWebAPI.Map map;
	public final Vector worldCoords;
	public final int zoom;
	
	/**
	 * @param world       the name of the world (e.g. <code>"world"</code>)
	 * @param map         the name of the map, or "prefix" (e.g. <code>"flat"</code>)
	 * @param worldCoords the in-game block coordinates
	 * @param zoom        the zoom-out level, 0 is fully zoomed in.
	 */
	public DynmapLocation(DynmapWebAPI.World world, DynmapWebAPI.Map map, Vector worldCoords, int zoom) {
		this.world = world;
		this.map = map;
		this.worldCoords = worldCoords;
		this.zoom = zoom;
	}
	
	public int getWorldX() {
		return worldCoords.getBlockX();
	}
	
	public int getWorldY() {
		return worldCoords.getBlockY();
	}
	
	public int getWorldZ() {
		return worldCoords.getBlockZ();
	}
	
	public int getTileX() {
		double unscaled = (int) map.worldtomap[0] * getWorldX()
				+ (int) map.worldtomap[1] * getWorldY()
				+ (int) map.worldtomap[2] * getWorldZ();
		return (int) (unscaled / SCALE_FACTOR / (1 << zoom)) * (1 << zoom);
	}
	
	public int getTileY() {
		double unscaled = (int) map.worldtomap[3] * getWorldX()
				+ (int) map.worldtomap[4] * getWorldY()
				+ (int) map.worldtomap[5] * getWorldZ();
		return (int) Math.ceil(((unscaled - SCALE_FACTOR) / SCALE_FACTOR) / (1 << zoom)) * (1 << zoom);
	}
	
}
