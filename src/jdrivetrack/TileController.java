package jdrivetrack;

import interfaces.TileLoader;
import interfaces.TileLoaderListener;
import interfaces.TileSource;

public class TileController {
    private TileLoader tileLoader;
    private TileCache tileCache;
    private TileSource tileSource;

    public TileController(TileSource tileSource, TileCache tileCache, TileLoaderListener listener) {
        this.tileSource = tileSource;
        this.tileLoader = new OsmTileLoader(listener);
        this.tileCache = tileCache;
    }

    public Tile getTile(int tilex, int tiley, int zoom) {
        int max = 1 << zoom;
        if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
            return null;
        Tile tile = tileCache.getTile(tileSource, tilex, tiley, zoom);
        if (tile == null) {
            tile = new Tile(tileSource, tilex, tiley, zoom);
            tileCache.addTile(tile);
            tile.loadPlaceholderFromCache(tileCache);
        }
        if (tile.error) {
            tile.loadPlaceholderFromCache(tileCache);
        }
        if (!tile.isLoaded()) {
            tileLoader.createTileLoaderJob(tile).submit();
        }
        return tile;
    }
    
    public TileCache getTileCache() {
        return tileCache;
    }

    public void setTileCache(TileCache tileCache) {
        this.tileCache = tileCache;
    }

    public TileLoader getTileLoader() {
        return tileLoader;
    }

    public void setTileLoader(TileLoader tileLoader) {
        this.tileLoader = tileLoader;
    }

    public TileSource getTileLayerSource() {
        return tileSource;
    }

    public TileSource getTileSource() {
        return tileSource;
    }

    public void setTileSource(TileSource tileSource) {
        this.tileSource = tileSource;
    }

    public void cancelOutstandingJobs() {
        tileLoader.cancelOutstandingTasks();
    }
}
