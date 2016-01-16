package com.MAVLink.gps;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* This contains the status of the GPS readings
*/
public class msg_status_gps extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_STATUS_GPS = 194;
    public static final int MAVLINK_MSG_LENGTH = 11;
    private static final long serialVersionUID = MAVLINK_MSG_ID_STATUS_GPS;

    /**
    * Magnetic variation, degrees 
    */
    public float magVar;
      
    /**
    * Number of times checksum has failed
    */
    public int csFails;
      
    /**
    * The quality indicator, 0=fix not available or invalid, 1=GPS fix, 2=C/A differential GPS, 6=Dead reckoning mode, 7=Manual input mode (fixed position), 8=Simulator mode, 9= WAAS a
    */
    public short gpsQuality;
      
    /**
    *  Indicates if GN, GL or GP messages are being received
    */
    public short msgsType;
      
    /**
    *  A = data valid, V = data invalid
    */
    public short posStatus;
      
    /**
    *  Magnetic variation direction E/W. Easterly variation (E) subtracts from True course and Westerly variation (W) adds to True course
    */
    public byte magDir;
      
    /**
    *  Positioning system mode indicator. A - Autonomous;D-Differential; E-Estimated (dead reckoning) mode;M-Manual input; N-Data not valid
    */
    public short modeInd;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_STATUS_GPS;
              
        packet.payload.putFloat(magVar);
              
        packet.payload.putUnsignedShort(csFails);
              
        packet.payload.putUnsignedByte(gpsQuality);
              
        packet.payload.putUnsignedByte(msgsType);
              
        packet.payload.putUnsignedByte(posStatus);
              
        packet.payload.putByte(magDir);
              
        packet.payload.putUnsignedByte(modeInd);
        
        return packet;
    }

    /**
    * Decode a status_gps message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.magVar = payload.getFloat();
              
        this.csFails = payload.getUnsignedShort();
              
        this.gpsQuality = payload.getUnsignedByte();
              
        this.msgsType = payload.getUnsignedByte();
              
        this.posStatus = payload.getUnsignedByte();
              
        this.magDir = payload.getByte();
              
        this.modeInd = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_status_gps(){
        msgid = MAVLINK_MSG_ID_STATUS_GPS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_status_gps(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_STATUS_GPS;
        unpack(mavLinkPacket.payload);        
    }
             
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_STATUS_GPS -"+" magVar:"+magVar+" csFails:"+csFails+" gpsQuality:"+gpsQuality+" msgsType:"+msgsType+" posStatus:"+posStatus+" magDir:"+magDir+" modeInd:"+modeInd+"";
    }
}
        