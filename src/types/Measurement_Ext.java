package types;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public class Measurement_Ext implements Externalizable {
	private Integer id = 0;
	private Integer measurementSetID;
	private List<Integer> channel = new ArrayList<Integer>(10);
	private List<Double> ber = new ArrayList<Double>(10);
	private List<Double> dBm = new ArrayList<Double>(10);
	private List<Double> sinad = new ArrayList<Double>(10);
	private List<Double> frequency = new ArrayList<Double>(10);

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

	public final List<Integer> getChannel() {
		return channel;
	}

	public final void setChannel(List<Integer> channel) {
		this.channel = channel;
	}

	public final List<Double> getBer() {
		return ber;
	}

	public final void setBer(List<Double> ber) {
		this.ber = ber;
	}

	public final List<Double> getdBm() {
		return dBm;
	}

	public final void setdBm(List<Double> dBm) {
		this.dBm = dBm;
	}

	public final List<Double> getSinad() {
		return sinad;
	}

	public final void setSinad(List<Double> sinad) {
		this.sinad = sinad;
	}

	public final List<Double> getFrequency() {
		return frequency;
	}

	public final void setFrequency(List<Double> frequency) {
		this.frequency = frequency;
	}
	
	public final int getChannel(int index) {
		return channel.get(index);
	}

	public final void setChannel(int index, int channel) {
		this.channel.set(index, channel);
	}

	public final double getBer(int index) {
		return ber.get(index);
	}

	public final void setBer(int index, double ber) {
		this.ber.set(index, ber);
	}

	public final double getdBm(int index) {
		return dBm.get(index);
	}

	public final void setdBm(int index, double dBm) {
		this.dBm.set(index, dBm);
	}

	public final double getSinad(int index) {
		return sinad.get(index);
	}

	public final void setSinad(int index, double sinad) {
		this.sinad.set(index, sinad);
	}

	public final double getFrequency(int index) {
		return frequency.get(index);
	}

	public final void setFrequency(int index, double frequency) {
		this.frequency.set(index, frequency);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.id = in.readInt();
		this.measurementSetID = in.readInt();
		this.channel = (List<Integer>) in.readObject();
		this.ber = (List<Double>) in.readObject();
		this.dBm = (List<Double>) in.readObject();
		this.sinad = (List<Double>) in.readObject();
		this.frequency = (List<Double>) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(id);
		out.writeInt(measurementSetID);
		out.writeObject(channel);
		out.writeObject(ber);
		out.writeObject(dBm);
		out.writeObject(sinad);
		out.writeObject(frequency);
	}

}
