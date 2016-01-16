package tilesources;

import tilesources.TileSourceInfo;

public class TMSTileSource extends AbstractTMSTileSource {

    protected int maxZoom;
    protected int minZoom = 0;

    public TMSTileSource(TileSourceInfo info) {
        super(info);
        minZoom = info.getMinZoom();
        maxZoom = info.getMaxZoom();
    }

    @Override
    public int getMinZoom() {
        return (minZoom == 0) ? super.getMinZoom() : minZoom;
    }

    @Override
    public int getMaxZoom() {
        return (maxZoom == 0) ? super.getMaxZoom() : maxZoom;
    }
}
