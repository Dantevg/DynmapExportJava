package nl.dantevg.dynmapexport;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynmapWebAPI {
	public static class Configuration {
		public List<World> worlds;
		
		public @Nullable World getWorldByName(String name) {
			return worlds.stream()
					.filter(world -> world.name.equalsIgnoreCase(name))
					.findAny()
					.orElse(null);
		}
	}
	
	public static class World {
		public String name;
		public List<Map> maps;
		
		public @Nullable Map getMapByName(String name) {
			return maps.stream()
					.filter(map -> map.name.equalsIgnoreCase(name))
					.findAny()
					.orElse(null);
		}
	}
	
	public static class Map {
		public String name;
		public String prefix;
		public int scale;
		public double[] worldtomap;
	}
	
}
