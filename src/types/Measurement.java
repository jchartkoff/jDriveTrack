package types;

import java.io.Serializable;

public class Measurement implements Serializable, Cloneable {
	private static final long serialVersionUID = 242009630100094270L;
	
	private Integer id = 0;
	private Integer measurementSetID;
	private Integer channelNumber;
	private Double ber;
	private Double dBm;
	private Double sinad;
	private Double frequency;
	private Boolean selected;

	public Measurement() {}
	
	public Measurement(Measurement data) {
		this.id = data.id;
		this.measurementSetID = data.measurementSetID;
		this.channelNumber = data.channelNumber;
		this.ber = data.ber;
		this.dBm = data.dBm;
		this.sinad = data.sinad;
		this.frequency = data.frequency;
		this.selected = data.selected;
	}

	public Object[] toObjectArray() {
		Object[] obj = new Object[8];
		obj[0] = id;
		obj[1] = measurementSetID;
		obj[2] = channelNumber;
	    obj[3] = ber;
	    obj[4] = dBm;
	    obj[5] = sinad;
	    obj[6] = frequency;
	    obj[7] = selected;
	    return obj;
	}

	public static Measurement toMeasurement(final Object[] obj) {
		Measurement measurement = new Measurement();
		measurement.id = (Integer) obj[0];
		measurement.measurementSetID = (Integer) obj[1];
	    measurement.channelNumber = (Integer) obj[2];	
    	measurement.ber = (Double) obj[3];
    	measurement.dBm = (Double) obj[4];
    	measurement.sinad = (Double) obj[5];
    	measurement.frequency = (Double) obj[6];
    	measurement.selected = (Boolean) obj[7];
		return 	measurement;
	}
	
	public void fromObjectArray(final Object[] obj) {
		id = (Integer) obj[0];
		measurementSetID = (Integer) obj[1];
	    channelNumber = (Integer) obj[2];	
    	ber = (Double) obj[3];
    	dBm = (Double) obj[4];
    	sinad = (Double) obj[5];
    	frequency = (Double) obj[6];
    	selected = (Boolean) obj[7];
	}
	
	public final Integer getId() {
		return id;
	}

	public final void setId(Integer id) {
		this.id = id;
	}
	
	public final Integer getMeasurementSetID() {
		return measurementSetID;
	}

	public final void setMeasurementSetID(Integer measurementSetID) {
		this.measurementSetID = measurementSetID;
	}

	public final Integer getChannelNumber() {
		return channelNumber;
	}

	public final void setChannelNumber(Integer channelNumber) {
		this.channelNumber = channelNumber;
	}

	public final Double getBer() {
		return ber;
	}

	public final void setBer(Double ber) {
		this.ber = ber;
	}

	public final Double getdBm() {
		return dBm;
	}

	public final void setdBm(Double dBm) {
		this.dBm = dBm;
	}

	public final Double getSinad() {
		return sinad;
	}

	public final void setSinad(Double sinad) {
		this.sinad = sinad;
	}

	public final Double getFrequency() {
		return frequency;
	}

	public final void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public final Boolean getSelected() {
		return selected;
	}

	public final void setSelected(Boolean selected) {
		this.selected = selected;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {		 
	    Measurement clone = (Measurement) super.clone();
	    return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ber == null) ? 0 : ber.hashCode());
		result = prime * result + ((channelNumber == null) ? 0 : channelNumber.hashCode());
		result = prime * result + ((dBm == null) ? 0 : dBm.hashCode());
		result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((measurementSetID == null) ? 0 : measurementSetID.hashCode());
		result = prime * result + ((selected == null) ? 0 : selected.hashCode());
		result = prime * result + ((sinad == null) ? 0 : sinad.hashCode());
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
		Measurement other = (Measurement) obj;
		if (ber == null) {
			if (other.ber != null)
				return false;
		} else if (!ber.equals(other.ber))
			return false;
		if (channelNumber == null) {
			if (other.channelNumber != null)
				return false;
		} else if (!channelNumber.equals(other.channelNumber))
			return false;
		if (dBm == null) {
			if (other.dBm != null)
				return false;
		} else if (!dBm.equals(other.dBm))
			return false;
		if (frequency == null) {
			if (other.frequency != null)
				return false;
		} else if (!frequency.equals(other.frequency))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (measurementSetID == null) {
			if (other.measurementSetID != null)
				return false;
		} else if (!measurementSetID.equals(other.measurementSetID))
			return false;
		if (selected == null) {
			if (other.selected != null)
				return false;
		} else if (!selected.equals(other.selected))
			return false;
		if (sinad == null) {
			if (other.sinad != null)
				return false;
		} else if (!sinad.equals(other.sinad))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Measurement [id=" + id + ", measurementSetID=" + measurementSetID + ", channelNumber=" + channelNumber
				+ ", ber=" + ber + ", dBm=" + dBm + ", sinad=" + sinad + ", frequency=" + frequency + ", selected="
				+ selected + "]";
	}

}

