package nl.dantevg.dynmapexport.location;

import nl.dantevg.dynmapexport.DynmapWebAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldCoordsTest {
	static final DynmapWebAPI.Map flat = new DynmapWebAPI.Map();
	static final DynmapWebAPI.Map surface = new DynmapWebAPI.Map();
	
	static {
		flat.worldtomap = new double[] {
				4, 0, -2.4492935982947064e-16,
				-2.4492935982947064e-16, 0, -4,
				0, 1, 0
		};
		surface.worldtomap = new double[] {
				11.31370849898476, 0, -11.313708498984761,
				-5.6568542494923815, 13.856406460551018, -5.656854249492381,
				5.551115123125782e-17, 0.9999999999999997, 5.551115123125782e-17
		};
	}
	
	@DisplayName("World coordinates to projection 'flat' tile coordinates")
	@ParameterizedTest(name = "zoom {2} @ {0} -> {1}")
	@MethodSource("flatCoordinateProvider")
	void toTileCoordsFlat(WorldCoords worldCoords, TileCoords tileCoords, int zoom) {
		assertEquals(tileCoords, worldCoords.toTileCoords(flat, zoom));
	}
	
	@DisplayName("World coordinates to projection 'surface' tile coordinates")
	@ParameterizedTest(name = "zoom {2} @ {0} -> {1}")
	@MethodSource("surfaceCoordinateProvider")
	void toTileCoordsSurface(WorldCoords worldCoords, TileCoords tileCoords, int zoom) {
		assertEquals(tileCoords, worldCoords.toTileCoords(surface, zoom));
	}
	
	static Stream<Arguments> flatCoordinateProvider() {
		return Stream.of(
				// Zoom 0
				Arguments.of(new WorldCoords(0, 0, 0), new TileCoords(0, -1), 0),
				Arguments.of(new WorldCoords(31, 0, 0), new TileCoords(0, -1), 0),
				Arguments.of(new WorldCoords(0, 0, 31), new TileCoords(0, -1), 0),
				Arguments.of(new WorldCoords(31, 0, 31), new TileCoords(0, -1), 0),
				
				Arguments.of(new WorldCoords(0, 0, -32), new TileCoords(0, 0), 0),
				Arguments.of(new WorldCoords(31, 0, -32), new TileCoords(0, 0), 0),
				Arguments.of(new WorldCoords(0, 0, -1), new TileCoords(0, 0), 0),
				Arguments.of(new WorldCoords(31, 0, -1), new TileCoords(0, 0), 0),
				
				// Zoom 1
				Arguments.of(new WorldCoords(0, 0, -32), new TileCoords(0, 0), 1),
				Arguments.of(new WorldCoords(63, 0, -32), new TileCoords(0, 0), 1),
				Arguments.of(new WorldCoords(0, 0, 31), new TileCoords(0, 0), 1),
				Arguments.of(new WorldCoords(63, 0, 31), new TileCoords(0, 0), 1),
				
				Arguments.of(new WorldCoords(0, 0, -96), new TileCoords(0, 2), 1),
				Arguments.of(new WorldCoords(63, 0, -96), new TileCoords(0, 2), 1),
				Arguments.of(new WorldCoords(0, 0, -33), new TileCoords(0, 2), 1),
				Arguments.of(new WorldCoords(63, 0, -33), new TileCoords(0, 2), 1),
				
				Arguments.of(new WorldCoords(0, 0, 32), new TileCoords(0, -2), 1),
				Arguments.of(new WorldCoords(63, 0, 32), new TileCoords(0, -2), 1),
				Arguments.of(new WorldCoords(0, 0, 95), new TileCoords(0, -2), 1),
				Arguments.of(new WorldCoords(63, 0, 95), new TileCoords(0, -2), 1),
				
				Arguments.of(new WorldCoords(0, 0, 96), new TileCoords(0, -4), 1),
				Arguments.of(new WorldCoords(63, 0, 96), new TileCoords(0, -4), 1),
				Arguments.of(new WorldCoords(0, 0, 159), new TileCoords(0, -4), 1),
				Arguments.of(new WorldCoords(63, 0, 159), new TileCoords(0, -4), 1),
				
				Arguments.of(new WorldCoords(64, 0, -32), new TileCoords(2, 0), 1),
				Arguments.of(new WorldCoords(127, 0, -32), new TileCoords(2, 0), 1),
				Arguments.of(new WorldCoords(64, 0, 31), new TileCoords(2, 0), 1),
				Arguments.of(new WorldCoords(127, 0, 31), new TileCoords(2, 0), 1),
				
				// Zoom 2
				Arguments.of(new WorldCoords(0, 0, -32), new TileCoords(0, 0), 2),
				Arguments.of(new WorldCoords(127, 0, -32), new TileCoords(0, 0), 2),
				Arguments.of(new WorldCoords(0, 0, 95), new TileCoords(0, 0), 2),
				Arguments.of(new WorldCoords(127, 0, 95), new TileCoords(0, 0), 2)
		);
	}
	
	static Stream<Arguments> surfaceCoordinateProvider() {
		return Stream.of(
				// Zoom 0
				Arguments.of(new WorldCoords(0, 97, -75), new TileCoords(6, 13), 0)
		);
	}
	
}
