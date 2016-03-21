// License: GPL. For details, see Readme.txt file.
package interfaces;

import types.Tile;

/**
 * Interface for implementing a tile loading job. Tiles are usually loaded via HTTP
 * or from a file.
 *
 * @author Dirk Stöcker
 */
public interface TileJob extends Runnable {

    /**
     * Function to return the tile associated with the job
     *
     * @return {@link Tile} to be handled
     */
    Tile getTile();

    /**
     * submits download job to backend.
     */
    void submit();

    /**
     * submits download job to backend.
     * @param force true if the load should skip all the caches (local &amp; remote)
     */
    void submit(boolean force);
}
