package interfaces;

import types.Tile;

public interface TileLoader {

    TileJob createTileLoaderJob(Tile tile);

    void cancelOutstandingTasks();
}
