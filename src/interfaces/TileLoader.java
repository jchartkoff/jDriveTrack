package interfaces;

import jdrivetrack.Tile;

public interface TileLoader {

    TileJob createTileLoaderJob(Tile tile);

    void cancelOutstandingTasks();
}
