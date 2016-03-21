package types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class EmissionDesignator {
	public enum BandwidthUnits {H,K,M,G}
	public enum ModulationTypes {N,A,H,R,J,B,C,F,G,D,P,K,L,M,Q,V,W,X}
	public enum SignalTypes {S0,S1,S2,S3,S7,S8,S9,SX,NULL}
	public enum InformationTypes {N,A,B,C,D,E,F,W,X,NULL}
	public enum InformationDetails {A,B,C,D,E,F,G,H,J,K,L,M,N,W,X,Y,Z,NULL}
	public enum MultiplexingTypes {N,C,F,T,W,X,NULL}
	public enum ModeNames {WFM,FM,NFM,AM,USB,LSB,CW,DSTAR,P25,NULL}
	
	private BandwidthUnits bandwidthUnit;
	private String bandwidthString;
	private ModulationTypes modulationType;
	private SignalTypes signalType;
	private InformationTypes informationType;
	private InformationDetails informationDetail;
	private MultiplexingTypes multiplexingType;
	
	private HashMap<BandwidthUnits,String> bandwidthUnits = new HashMap<BandwidthUnits,String>(4);
	private HashMap<ModulationTypes,String> modulationTypes = new HashMap<ModulationTypes,String>(18);
	private HashMap<SignalTypes,String> signalTypes = new HashMap<SignalTypes,String>(9);
	private HashMap<InformationTypes,String> informationTypes = new HashMap<InformationTypes,String>(10);
	private HashMap<InformationDetails,String> informationDetails = new HashMap<InformationDetails,String>(18);
	private HashMap<MultiplexingTypes,String> multiplexingTypes = new HashMap<MultiplexingTypes,String>(7);
	private HashMap<String,ModeNames> modeNames = new HashMap<String,ModeNames>(6);
	private static TreeMap<String,String> modes = new TreeMap<String,String>();
	
	public EmissionDesignator(String itu) {
		bandwidthString = itu.substring(0, 4);
		parseItuString(itu);
		bandwidthUnit(itu);
		loadMaps();
	}	

	public EmissionDesignator(String bandwidthString, ModulationTypes modulationType,
			SignalTypes signalType, InformationTypes informationType) {
		this(bandwidthString, modulationType, signalType, informationType,
			InformationDetails.NULL, MultiplexingTypes.NULL);
	}
	
	public EmissionDesignator(String bandwidthString, ModulationTypes modulationType,
			SignalTypes signalType, InformationTypes informationType, InformationDetails informationDetail,
			MultiplexingTypes multiplexingType) {
		this.bandwidthString = bandwidthString;
		this.modulationType = modulationType;
		this.signalType = signalType;
		this.informationType = informationType;
		this.informationDetail = informationDetail;
		this.multiplexingType = multiplexingType;
		bandwidthUnit(bandwidthString);
		loadMaps();
	}
	
	private void bandwidthUnit(String itu) {
		int i;
		String bw = itu.substring(0, 4);
		i = bw.indexOf("H");
		if (i == -1) i = bw.indexOf("K");
		if (i == -1) i = bw.indexOf("M");
		if (i == -1) i = bw.indexOf("G");
		
		if (i >= 1 && i <= 3) {
			String bwchar = bw.substring(i, i+1);
			switch (bwchar) {
				case "H" : bandwidthUnit = BandwidthUnits.H; break;
				case "K" : bandwidthUnit = BandwidthUnits.K; break;
				case "M" : bandwidthUnit = BandwidthUnits.M; break;
				case "G" : bandwidthUnit = BandwidthUnits.G; break;
			}
		}
	}
	
	private void parseItuString(String itu) {
		modulationType = ModulationTypes.valueOf(itu.substring(4, 5));
		signalType = SignalTypes.valueOf("S" + itu.substring(5, 6));
		informationType = InformationTypes.valueOf(itu.substring(6, 7));
		if (itu.length() > 7) {
			informationDetail = InformationDetails.valueOf(itu.substring(7, 8));
		} else {
			informationDetail = InformationDetails.NULL;
		}
		if (itu.length() > 8) {
			multiplexingType = MultiplexingTypes.valueOf(itu.substring(8, 9));
		} else {
			multiplexingType = MultiplexingTypes.NULL;
		}
	}

	private void loadMaps() {
		modeNames.put("200KF8E", ModeNames.WFM);
		modeNames.put("20K0F3E", ModeNames.FM);
		modeNames.put("11K2F3E", ModeNames.NFM);
		modeNames.put("6K00A3E", ModeNames.AM);
		modeNames.put("2K80J3EY", ModeNames.USB);
		modeNames.put("2K80J3EZ", ModeNames.LSB);
		modeNames.put("150HA1A", ModeNames.CW);
		modeNames.put("6K00F7W", ModeNames.DSTAR);
		modeNames.put("8K10F1E", ModeNames.P25);
		
		bandwidthUnits.put(BandwidthUnits.H, "Hertz");
		bandwidthUnits.put(BandwidthUnits.K, "Kilohertz");
		bandwidthUnits.put(BandwidthUnits.M, "Megahertz");
		bandwidthUnits.put(BandwidthUnits.G, "Gigahertz");
		
		modulationTypes.put(ModulationTypes.N, "Unmodulated carrier");
		modulationTypes.put(ModulationTypes.A, "Double-sideband amplitude modulation");
		modulationTypes.put(ModulationTypes.H, "Single-sideband with full carrier");
		modulationTypes.put(ModulationTypes.R, "Single-sideband with reduced or variable carrier");
		modulationTypes.put(ModulationTypes.J, "Single-sideband with suppressed carrier");
		modulationTypes.put(ModulationTypes.B, "Independent sideband");
		modulationTypes.put(ModulationTypes.C, "Vestigial sideband");
		modulationTypes.put(ModulationTypes.F, "Frequency modulation");
		modulationTypes.put(ModulationTypes.G, "Phase modulation");
		modulationTypes.put(ModulationTypes.D, "Combination of AM and FM or PM");
		modulationTypes.put(ModulationTypes.P, "Sequence of pulses without modulation");
		modulationTypes.put(ModulationTypes.K, "Pulse amplitude modulation");
		modulationTypes.put(ModulationTypes.L, "Pulse width modulation");
		modulationTypes.put(ModulationTypes.M, "Pulse position modulation");
		modulationTypes.put(ModulationTypes.Q, "Sequence of pulses, phase or frequency modulation within each pulse");
		modulationTypes.put(ModulationTypes.V, "Combination of pulse modulation methods");
		modulationTypes.put(ModulationTypes.W, "Combination of any of the above");
		modulationTypes.put(ModulationTypes.X, "None of the above");
	
		signalTypes.put(SignalTypes.S0, "No modulating signal");
		signalTypes.put(SignalTypes.S1, "One channel containing digital information, no subcarrier");
		signalTypes.put(SignalTypes.S2, "One channel containing digital information, using a subcarrier");
		signalTypes.put(SignalTypes.S3, "One channel containing analogue information");
		signalTypes.put(SignalTypes.S7, "More than one channel containing digital information");
		signalTypes.put(SignalTypes.S8, "More than one channel containing analogue information");
		signalTypes.put(SignalTypes.S9, "Combination of analogue and digital channels");
		signalTypes.put(SignalTypes.SX, "None of the above");
		signalTypes.put(SignalTypes.NULL, "No Information");
		
		informationTypes.put(InformationTypes.N, "No transmitted information");
		informationTypes.put(InformationTypes.A, "Aural telegraphy, intended to be decoded by ear, such as Morse code");
		informationTypes.put(InformationTypes.B, "Electronic telegraphy, intended to be decoded by machine (radioteletype and digital modes)");
		informationTypes.put(InformationTypes.C, "Facsimile (still images)");
		informationTypes.put(InformationTypes.D, "Data transmission, telemetry or telecommand (remote control)");
		informationTypes.put(InformationTypes.E, "Telephony (voice or music intended to be listened to by a human)");
		informationTypes.put(InformationTypes.F, "Video (television signals)");
		informationTypes.put(InformationTypes.W, "Combination of any of the above");
		informationTypes.put(InformationTypes.X, "None of the above");
		informationTypes.put(InformationTypes.NULL, "No Information");
		
		informationDetails.put(InformationDetails.A, "Two-condition code, elements vary in quantity and duration");
		informationDetails.put(InformationDetails.B, "Two-condition code, elements fixed in quantity and duration");
		informationDetails.put(InformationDetails.C, "Two-condition code, elements fixed in quantity and duration, error-correction included");
		informationDetails.put(InformationDetails.D, "Four-condition code, one condition per signal element");
		informationDetails.put(InformationDetails.E, "Multi-condition code, one condition per signal element");
		informationDetails.put(InformationDetails.F, "Multi-condition code, one character represented by one or more conditions");
		informationDetails.put(InformationDetails.G, "Monophonic broadcast-quality sound");
		informationDetails.put(InformationDetails.H, "Stereophonic or quadraphonic broadcast-quality sound");
		informationDetails.put(InformationDetails.J, "Commercial-quality sound (non-broadcast)");
		informationDetails.put(InformationDetails.K, "Commercial-quality sound—frequency inversion and-or band-splitting employed");
		informationDetails.put(InformationDetails.L, "Commercial-quality sound, independent FM signals, such as pilot tones, used to control the demodulated signal");
		informationDetails.put(InformationDetails.M, "Greyscale images or video");
		informationDetails.put(InformationDetails.N, "Full-color images or video");
		informationDetails.put(InformationDetails.W, "Combination of two or more of the above");
		informationDetails.put(InformationDetails.X, "None of the above");
		informationDetails.put(InformationDetails.Y, "Upper - Non-Standard");
		informationDetails.put(InformationDetails.Z, "Lower - Non-Standard");
		informationDetails.put(InformationDetails.NULL, "No Information");
		
		multiplexingTypes.put(MultiplexingTypes.N, "None used");
		multiplexingTypes.put(MultiplexingTypes.C, "Code-division (excluding spread spectrum)");
		multiplexingTypes.put(MultiplexingTypes.F, "Frequency-division");
		multiplexingTypes.put(MultiplexingTypes.T, "Time-division");
		multiplexingTypes.put(MultiplexingTypes.W, "Combination of Frequency-division and Time-division");
		multiplexingTypes.put(MultiplexingTypes.X, "None of the above");
		multiplexingTypes.put(MultiplexingTypes.NULL, "No Information");

		modes.put("60H0J2B","PSK31");
		modes.put("100HN0N","Speed Radar (10525 MHz X band; 24150 MHz Ka band)");
		modes.put("150HA1A","Continuous Wave Telegraphy");
		modes.put("500HJ2D","MT63-500 50 WPM");
		modes.put("1K00J2D","MT63-1000 100 WPM");
		modes.put("2K00J2D","MT63-2000 200 WPM");
		modes.put("2K80J2B","HF RTTY");
		modes.put("2K80J2D","HF PACTOR-III");
		modes.put("2K80J3E","Single sideband suppressed carrier voice");
		modes.put("3K00H2B","HF ALE MIL-STD-188-141A/FED-STD-1045");
		modes.put("3K30F1D","6.25 kHz SCADA link (CalAmp Viper SC 173 MHz)");
		modes.put("4K00F1D","6.25 kHz data NXDN (IDAS, NEXEDGE)");
		modes.put("4K00F1E","6.25 kHz voice NXDN (IDAS, NEXEDGE)");
		modes.put("4K00F1W","6.25 kHz voice and data NXDN (IDAS, NEXEDGE)");
		modes.put("4K00F2D","6.25 kHz analog CW ID NXDN (IDAS, NEXEDGE)");
		modes.put("4K00J1D","Amplitude Compandored Sideband (pilot tone/carrier)");
		modes.put("4K00J2D","Amplitude Compandored Sideband (pilot tone/carrier)");
		modes.put("4K00J3E","Amplitude Compandored Sideband (pilot tone/carrier) voice");
		modes.put("5K60F2D","SCADA");
		modes.put("5K76G1E","P25 CQPSK voice");
		modes.put("6K00A3E","Double sideband AM voice");
		modes.put("6K00F1D","SCADA Carrier Frequency Shift Keying");
		modes.put("6K00F2D","SCADA Audio Frequency Shift Keying");
		modes.put("6K00F3D","SCADA Analog data that is not AFSK");
		modes.put("6K00F7W","D-STAR");
		modes.put("7K60FXD","2-slot DMR (Motorola MOTOTRBO) TDMA data");
		modes.put("7K60FXE","2-slot DMR (Motorola MOTOTRBO) TDMA voice");
		modes.put("8K10F1D","P25 Phase I C4FM data");
		modes.put("8K10F1E","P25 Phase I C4FM voice");
		modes.put("8K10F1W","P25 Phase II subscriber units (Harmonized Continuous Phase Modulation H-CPM)");
		modes.put("8K30F1D","12.5 kHz data NXDN (Wide IDAS, NEXEDGE)");
		modes.put("8K30F1E","12.5 kHz voice NXDN (Wide IDAS, NEXEDGE)");
		modes.put("8K30F1W","P25 Phase I C4FM hybridized voice and data applications");
		modes.put("8K30F7W","12.5 kHz voice and data NXDN (Wide IDAS, NEXEDGE)");
		modes.put("8K50F9W","Harris OpenSky (2 slot narrowband)");
		modes.put("8K70D1W","P25 Linear Simulcast Modulation ASTRO (9.6 kbps in 12.5 kHz channelspace)");
		modes.put("9K20F2D","Zetron-based alphanumeric paging/alerting system");
		modes.put("9K30F1D","SCADA / Remote Control");
		modes.put("9K70F1D","P25 Linear Simulcast Modulation WCQPSK data");
		modes.put("9K70F1E","P25 Linear Simulcast Modulation WCQPSK voice");
		modes.put("9K80D7W","P25 Phase II fixed-end 2-slot TDMA (Harmonized Differential Quadrature Phase Shift Keyed modulation H-DQPSK)");
		modes.put("9K80F1D","P25 Phase II fixed-end 2-slot TDMA H-DQPSK data");
		modes.put("9K80F1E","P25 Phase II fixed-end 2-slot TDMA H-DQPSK voice");
		modes.put("10K0F1D","LTI Automated Vehicle Location (AVL) system");
		modes.put("10K0F1D","RD-LAP 9.6 kbps data on narowband channel");
		modes.put("10K0F1D","Motorola Widepulse ASTRO simulcast data");
		modes.put("10K0F1D","Motorola Widepulse ASTRO simulcast control channel");
		modes.put("10K0F1E","Motorola Widepulse ASTRO simulcast voice");
		modes.put("11K2F2D","Audio frequency shift keying within a 12.5 kHz channelspace");
		modes.put("11K2F3D","DTMF or other audible, non-frequency shift signaling, such as Whelen outdoor warning sirens or Knox-Box activation");
		modes.put("11K2F3E","2.5 kHz deviation FM narrowband 12.5 kHz analog voice");
		modes.put("12K1F9W","Harris OpenSky (NPSPAC - 4 slot)");
		modes.put("13K1F9W","Harris OpenSky (SMR - 4 slot)");
		modes.put("13K6F3E","Frequency modulated (FM) voice");
		modes.put("13K6W7W","Motorola iDEN (900 MHz)");
		modes.put("14K0F1D","Motorola 3600 baud trunked control channel (NPSPAC)");
		modes.put("16K0F1D","Motorola 3600 baud trunked control channel");
		modes.put("16K0F2D","4 kHz deviation FM audio frequency shift keying (72 MHz fire alarm boxes)");
		modes.put("16K0F3E","4 kHz deviation FM analog voice (NPSPAC)");
		modes.put("16K8F1E","Encrypted Quantized Voice (Motorola DVP, DES, DES-XL on NPSPAC)");
		modes.put("17K7D7D","Motorola HPD High Performance Data Astro 25 suite, as Motorola HAI (High performance data Air Interface) 700/800 MHz requires 25 kHz channel space");
		modes.put("20K0D1E","Reduced power TETRA PowerTrunk 4/TDMA fixed-end (voice)");
		modes.put("20K0D1W","Reduced power TETRA PowerTrunk 4/TDMA fixed-end (simultaneous mixed modes)");
		modes.put("20K0F1D","RD-LAP 19.2 kbps within a wideband channel (2013 compliant, meets data throughput requirement)");
		modes.put("20K0F1E","Encrypted Quantized Voice (Motorola DVP, DES, DES-XL - NOT P25 DES-OFB/AES)");
		modes.put("20K0F3E","5 kHz deviation FM wideband 25 kHz analog voice");
		modes.put("20K0G7W","Motorola iDEN (800 MHz)");
		modes.put("20K0W7W","Motorola iDEN (800 MHz)");
		modes.put("20K1D1D","Reduced power TETRA PowerTrunk 4/TDMA fixed-end (data)");
		modes.put("21K0D1W","TETRA ETS 300 392 Standard");
		modes.put("30K0DXW","TDMA Cellular (North America)");
		modes.put("40K0F8W","AMPS Cellular");
		modes.put("55K0P0N","CODAR oceanographic RADAR 3.5 - 5 MHz");
		modes.put("100KC3F","ReconRobotics surveillance robot video (430-450 MHz)");
		modes.put("100KP0N","CODAR oceanographic RADAR 12 - 14 MHz");
		modes.put("170KP0N","CODAR oceanographic RADAR above 24 MHz");
		modes.put("200KF8E","Broadcast FM with Subsidiary Communications Subcarrier");
		modes.put("250KF3E","Television Broadcast Audio");
		modes.put("300KG7W","EDGE (Enhanced Data rates for GSM Evolution)");
		modes.put("300KGXW","GSM Cellular");
		modes.put("1M25F9W","CDMA Cellular");
		modes.put("2M40W7D","Remote Control Video (digital, non-NTSC)");
		modes.put("5M00G7D","Public Safety LTE (all four emissions used) 5 MHz bandwidth");
		modes.put("5M00W7W","Public Safety LTE (all four emissions used) 5 MHz bandwidth");
		modes.put("5M00G2D","Public Safety LTE (all four emissions used) 5 MHz bandwidth");
		modes.put("5M00D7D","Public Safety LTE (all four emissions used) 5 MHz bandwidth");
		modes.put("5M75C3F","NTSC Video (with 250K0F3E aural carrier)");
		modes.put("6M00C7W","ATSC Video (Digital TV)");
		modes.put("10M0W7D","WiMAX 10 MHz");
		modes.put("10M0G2D","Public Safety LTE (all four emissions used) 10 MHz bandwidth");
		modes.put("10M0W7W","Public Safety LTE (all four emissions used) 10 MHz bandwidth");
		modes.put("10M0D7D","Public Safety LTE (all four emissions used) 10 MHz bandwidth");
		modes.put("10M0G7D","Public Safety LTE (all four emissions used) 10 MHz bandwidth");	
	}
	
	public BandwidthUnits getBandwidthUnit() {
		return bandwidthUnit;
	}
	
	public SignalTypes getSignalType() {
		return signalType;
	}
	
	public ModulationTypes getModulationType() {
		return modulationType;
	}
	
	public InformationTypes getInformationType() {
		return informationType;
	}
	
	public InformationDetails getInformationDetail() {
		return informationDetail;
	}
	
	public MultiplexingTypes getMultiplexingType() {
		return multiplexingType;
	}
	
	public String getBandwidthUnitValue() {
		return bandwidthUnits.get(bandwidthUnit);
	}
	
	public String getSignalTypeValue() {
		return signalTypes.get(signalType).substring(1,2);
	}
	
	public String getModulationTypeValue() {
		return modulationTypes.get(modulationType);
	}
	
	public String getInformationTypeValue() {
		return informationTypes.get(informationType);
	}
	
	public String getInformationDetailValue() {
		return informationDetails.get(informationDetail);
	}
	
	public String getMultiplexingTypeValue() {
		return multiplexingTypes.get(multiplexingType);
	}
	
	public static String[] getModeEntrySet() {
		String[] list = new String[modes.size()];
		int i = 0;
		Iterator<Entry<String, String>> it = modes.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, String> pairs = it.next();
	        list[i] = pairs.getKey() + "  " + pairs.getValue();
	        i++;
	        it.remove();
	    }
	    return list;
	}
	
	public String getITUEmissionDesignator() {
		String str = bandwidthString + modulationType.toString() + 
			signalType.toString().substring(1) + informationType.toString();
		if (!informationDetail.toString().equals("NULL")) {
			str += informationDetail.toString();
			if (!multiplexingType.toString().equals("NULL")) str += multiplexingType.toString();
		}
		return str;
	}
	
	public String getModeName() {
		ModeNames modeName = modeNames.get(getITUEmissionDesignator());
		if (modeName == null) return "-------";
		else return modeName.toString();
	}
	
	public String getModeDescription() {
		return modes.get(getITUEmissionDesignator());
	}
}
