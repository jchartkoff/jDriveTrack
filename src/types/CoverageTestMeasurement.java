package types;

import java.awt.Point;   import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CoverageTestMeasurement implements Serializable, Cloneable {
	private static final long serialVersionUID = 242009630100094270L;
	
	private Integer id;
	private String sentence; 
	private Long millis;
	private List<Double> ber = new ArrayList<Double>(10);
	private List<Double> dBm = new ArrayList<Double>(10);
	private List<Double> sinad = new ArrayList<Double>(10);
	private List<Double> freq = new ArrayList<Double>(10);
	private List<Boolean> select = new ArrayList<Boolean>(10);
	private Integer testTileID;
	private Point.Double position;
	private Double dopplerDirection = 0d;
	private Integer dopplerQuality = 0;
	private Integer marker = 0;
	
	public CoverageTestMeasurement() {}
	
	public CoverageTestMeasurement(CoverageTestMeasurement data) {
		this.id = data.id;
		this.sentence = data.sentence;
		this.millis = data.millis;
		this.ber = data.ber;
		this.dBm = data.dBm;
		this.sinad = data.sinad;
		this.freq = data.freq;
		this.select = data.select;
		this.testTileID = data.testTileID;
		this.position = data.position;
		this.dopplerDirection = data.dopplerDirection;
		this.dopplerQuality = data.dopplerQuality;
		this.marker = data.marker;
	}

	public Object[] toObjectArray() {
		Object[] obj = new Object[59];
		obj[0] = id;
		obj[1] = sentence;
		obj[2] = millis;
		
		for (Integer i = 0; i < 10; i++) {
	    	obj[(i*5)+3] = ber.get(i);
	    	obj[(i*5)+4] = dBm.get(i);
	    	obj[(i*5)+5] = sinad.get(i);
	    	obj[(i*5)+6] = freq.get(i);
	    	obj[(i*5)+7] = select.get(i);
	    }
		
		obj[53] = testTileID;
		obj[54] = position.x;
		obj[55] = position.y;
		obj[56] = dopplerDirection;
		obj[57] = dopplerQuality;
		obj[58] = marker;
		
		return obj;
	}

	public static CoverageTestMeasurement fromObjectArray(final Object[] obj) {
		CoverageTestMeasurement ct = new CoverageTestMeasurement();
		ct.id = (Integer) obj[0];
		ct.sentence = (String) obj[1];
	    ct.millis = (Long) obj[2];
	    
	    for (Integer i = 0; i < 10; i++) { 	
	    	ct.ber.add((Double) obj[(i*5)+3]);
	    	ct.dBm.add((Double) obj[(i*5)+4]);
	    	ct.sinad.add((Double) obj[(i*5)+5]);
	    	ct.freq.add((Double) obj[(i*5)+6]);
	    	ct.select.add((Boolean) obj[(i*5)+7]);
	    }

	    ct.testTileID = (Integer) obj[53];  
		ct.position = new Point.Double((Double) obj[54], (Double) obj[55]);
		ct.dopplerDirection = (Double) obj[56];
		ct.dopplerQuality = (Integer) obj[57];
		ct.marker = (Integer) obj[58];
		
		return 	ct;
	}

    @Override
    public boolean equals(Object other) {
        return (other instanceof CoverageTestMeasurement) && (id != null)
             ? id.equals(((CoverageTestMeasurement) other).id)
             : (other == this);
    }

    @Override
    public int hashCode() {
        return (id != null) 
             ? (this.getClass().hashCode() + id.hashCode()) 
             : super.hashCode();
    }
    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public Long getMillis() {
		return millis;
	}

	public void setMillis(Long millis) {
		this.millis = millis;
	}

	public List<Double> getBer() {
		return ber;
	}

	public void setBer(List<Double> ber) {
		this.ber = ber;
	}
	
	public void setBer(int index, Double ber) {
		this.ber.add(index, ber);
	}
	
	public void setdBm(int index, Double dBm) {
		this.dBm.add(index, dBm);
	}
	
	public void setSinad(int index, Double sinad) {
		this.sinad.add(index, sinad);
	}
	
	public void setSelect(List<Boolean> select) {
		this.select = select;
	}
	
	public void setSelect(int index, Boolean select) {
		this.select.add(index, select);
	}
	
	public Boolean getSelect(int index) {
		return select.get(index);
	}
	
	public Double getFreq(int index) {
		return freq.get(index);
	}
	
	public Double getBer(int index) {
		return ber.get(index);
	}
	
	public Double getSinad(int index) {
		return sinad.get(index);
	}
	
	public Double getdBm(int index) {
		return dBm.get(index);
	}
	
	public List<Double> getdBm() {
		return dBm;
	}

	public void setdBm(List<Double> dBm) {
		this.dBm = dBm;
	}

	public List<Double> getSinad() {
		return sinad;
	}

	public void setSinad(List<Double> sinad) {
		this.sinad = sinad;
	}
	
	public void setFreq(int index, Double freq) {
		this.freq.add(index, freq);
	}
	
	public List<Double> getFreq() {
		return freq;
	}

	public void setFreq(List<Double> freq) {
		this.freq = freq;
	}

	public Integer getTestTileID() {
		return testTileID;
	}

	public void setTestTileID(Integer testTileID) {
		this.testTileID = testTileID;
	}

	public Point.Double getPosition() {
		return position;
	}

	public void setPosition(Point.Double position) {
		this.position = position;
	}

	public Double getDopplerDirection() {
		return dopplerDirection;
	}

	public void setDopplerDirection(Double dopplerDirection) {
		this.dopplerDirection = dopplerDirection;
	}

	public Integer getDopplerQuality() {
		return dopplerQuality;
	}

	public void setDopplerQuality(Integer dopplerQuality) {
		this.dopplerQuality = dopplerQuality;
	}

	public Integer getMarker() {
		return marker;
	}

	public void setMarker(Integer marker) {
		this.marker = marker;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {		 
	    CoverageTestMeasurement clone = (CoverageTestMeasurement) super.clone();
	    return clone;
	}
}

