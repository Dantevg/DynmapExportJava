# DynmapExport
This is a Spigot plugin that can automatically export dynmap tiles at a set
interval. It will only export when at least one tile in an export configuration
has changed since the last export.

## Config file
### `dynmap-port`
The port on which dynmap is set to run. `8123` by default.

### `schedule`
A simplified ISO-8601 formatted interval at which to automatically export all
configurations.
> For example: `10m`, `1h30m`, `1d`

### `exports`
A list of export configurations. Each configuration has the following structure:
- `world`: the name of the world
- `map`: the name of the map
- `zoom`: the zoom-out level, 0 is fully zoomed in.
- `from` and `to`: the Dynmap tile coordinates that specify the (inclusive)
  range of tiles to export.
  - `x`: the x-coordinate of the tile
  - `y`: the y-coordinate of the tile

## Command
- `/dynmapexport now`: export all configurations immediately, separate from the
  scheduler.
- `/dynmapexport export <world> <map> <x> <z> <zoom>`: export a single tile at
  the given **world coordinates** (not tile coordinates), at y-level 64.
