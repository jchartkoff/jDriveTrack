package types;

import java.awt.Point;
import java.io.Serializable;

public class TestSettings implements Serializable, Cloneable {
	private static final long serialVersionUID = -2766133162469149171L;
	
	private Integer id = 0;
	private String testName;
	private Integer minimumTimePerTile;
	private Integer sampleTimingMode;
	private Point.Double tileSize;
	private Point.Double gridReference;
	private Point.Double gridSize;
	private Integer maximumSamplesPerTile;
	private Integer minimumSamplesPerTile;
	private Integer dotsPerTile;

	public TestSettings() {}
	
	public TestSettings(TestSettings data) {
		this.id = data.id;
		this.testName = data.testName;
		this.minimumTimePerTile = data.minimumTimePerTile;
		this.sampleTimingMode = data.sampleTimingMode;
		this.tileSize = data.tileSize;
		this.maximumSamplesPerTile = data.maximumSamplesPerTile;
		this.minimumSamplesPerTile = data.minimumSamplesPerTile;
		this.gridReference = data.gridReference;
		this.gridSize = data.gridSize;
		this.dotsPerTile = data.dotsPerTile;
	}

	public Object[] toObjectArray(final TestSettings data) {
		Object[] obj = new Object[13];
		obj[0] = data.id;
		obj[1] = data.testName;
		obj[2] = data.minimumTimePerTile;
		obj[3] = data.sampleTimingMode;
		obj[4] = data.tileSize.x;
		obj[5] = data.tileSize.y;
		obj[6] = data.maximumSamplesPerTile;
		obj[7] = data.minimumSamplesPerTile;
		obj[8] = data.gridReference.x;
		obj[9] = data.gridReference.y;
		obj[10] = data.gridSize.x;
		obj[11] = data.gridSize.y;
		obj[12] = data.dotsPerTile;
		return obj;
	}

	public static TestSettings toTestSettings(final Object[] obj) {
		TestSettings cts = new TestSettings();
		cts.id = (Integer) obj[0];
		cts.testName = (String) obj[1];
		cts.minimumTimePerTile = (Integer) obj[2];
		cts.sampleTimingMode = (Integer) obj[3];  
		cts.tileSize = new Point.Double((Double) obj[4], (Double) obj[5]);
		cts.maximumSamplesPerTile = (Integer) obj[6];
		cts.minimumSamplesPerTile = (Integer) obj[7];
		cts.gridReference = new Point.Double((Double) obj[8], (Double) obj[9]);
		cts.gridSize = new Point.Double((Double) obj[10], (Double) obj[11]);
		cts.dotsPerTile = (Integer) obj[12];
		return 	cts;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public String getTestName() {
		return testName;
	}
	
	public Integer getMinimumTimePerTile() {
		return minimumTimePerTile;
	}

	public void setMinimumTimePerTile(Integer minimumTimePerTile) {
		this.minimumTimePerTile = minimumTimePerTile;
	}

	public Integer getSampleTimingMode() {
		return sampleTimingMode;
	}

	public void setSampleTimingMode(Integer sampleTimingMode) {
		this.sampleTimingMode = sampleTimingMode;
	}
	
	public Point.Double getGridSize() {
		return gridSize;
	}

	public void setGridSize(Point.Double gridSize) {
		this.gridSize = gridSize;
	}
	
	public Point.Double getGridReference() {
		return gridReference;
	}

	public void setGridReference(Point.Double gridReference) {
		this.gridReference = gridReference;
	}
	
	public Point.Double getTileSize() {
		return tileSize;
	}

	public void setTileSize(Point.Double tileSize) {
		this.tileSize = tileSize;
	}

	public Integer getMaximumSamplesPerTile() {
		return maximumSamplesPerTile;
	}

	public void setMaximumSamplesPerTile(Integer maximumSamplesPerTile) {
		this.maximumSamplesPerTile = maximumSamplesPerTile;
	}

	public Integer getMinimumSamplesPerTile() {
		return minimumSamplesPerTile;
	}

	public void setMinimumSamplesPerTile(Integer minimumSamplesPerTile) {
		this.minimumSamplesPerTile = minimumSamplesPerTile;
	}
	
	public void setDotsPerTile(Integer dotsPerTile) {
		this.dotsPerTile = dotsPerTile;
	}
	
	public Integer getDotsPerTile() {
		return dotsPerTile;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {		 
	    TestSettings clone = (TestSettings) super.clone();
	    return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dotsPerTile == null) ? 0 : dotsPerTile.hashCode());
		result = prime * result + ((gridReference == null) ? 0 : gridReference.hashCode());
		result = prime * result + ((gridSize == null) ? 0 : gridSize.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((maximumSamplesPerTile == null) ? 0 : maximumSamplesPerTile.hashCode());
		result = prime * result + ((minimumSamplesPerTile == null) ? 0 : minimumSamplesPerTile.hashCode());
		result = prime * result + ((minimumTimePerTile == null) ? 0 : minimumTimePerTile.hashCode());
		result = prime * result + ((sampleTimingMode == null) ? 0 : sampleTimingMode.hashCode());
		result = prime * result + ((testName == null) ? 0 : testName.hashCode());
		result = prime * result + ((tileSize == null) ? 0 : tileSize.hashCode());
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
		TestSettings other = (TestSettings) obj;
		if (dotsPerTile == null) {
			if (other.dotsPerTile != null)
				return false;
		} else if (!dotsPerTile.equals(other.dotsPerTile))
			return false;
		if (gridReference == null) {
			if (other.gridReference != null)
				return false;
		} else if (!gridReference.equals(other.gridReference))
			return false;
		if (gridSize == null) {
			if (other.gridSize != null)
				return false;
		} else if (!gridSize.equals(other.gridSize))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (maximumSamplesPerTile == null) {
			if (other.maximumSamplesPerTile != null)
				return false;
		} else if (!maximumSamplesPerTile.equals(other.maximumSamplesPerTile))
			return false;
		if (minimumSamplesPerTile == null) {
			if (other.minimumSamplesPerTile != null)
				return false;
		} else if (!minimumSamplesPerTile.equals(other.minimumSamplesPerTile))
			return false;
		if (minimumTimePerTile == null) {
			if (other.minimumTimePerTile != null)
				return false;
		} else if (!minimumTimePerTile.equals(other.minimumTimePerTile))
			return false;
		if (sampleTimingMode == null) {
			if (other.sampleTimingMode != null)
				return false;
		} else if (!sampleTimingMode.equals(other.sampleTimingMode))
			return false;
		if (testName == null) {
			if (other.testName != null)
				return false;
		} else if (!testName.equals(other.testName))
			return false;
		if (tileSize == null) {
			if (other.tileSize != null)
				return false;
		} else if (!tileSize.equals(other.tileSize))
			return false;
		return true;
	}

}

