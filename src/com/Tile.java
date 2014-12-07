package com;

public class Tile {
	
	private final String key;
    public final int x, y, z;
    
    public Tile(String tileServer, int x, int y, int z) {
        this.key = tileServer;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
	public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }
    
    @Override
	public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tile other = (Tile) obj;
        if (key == null) {
            if (other.key != null) 
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

}
