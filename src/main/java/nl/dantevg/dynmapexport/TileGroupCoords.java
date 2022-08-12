package nl.dantevg.dynmapexport;

public class TileGroupCoords {
	public static final String SEPARATOR = "_";
	
	public final int x, y;
	
	public TileGroupCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return this.x + SEPARATOR + this.y;
	}
	
	public static TileGroupCoords parse(String str) {
		int separator = str.indexOf(SEPARATOR);
		int x = Integer.parseInt(str.substring(0, separator - 1));
		int y = Integer.parseInt(str.substring(separator + 1));
		return new TileGroupCoords(x, y);
	}
}
