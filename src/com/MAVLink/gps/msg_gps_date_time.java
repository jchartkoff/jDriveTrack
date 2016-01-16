package com.MAVLink.gps;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Pilot console PWM messages.
*/
public class msg_gps_date_time extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_GPS_DATE_TIME = 179;
    public static final int MAVLINK_MSG_LENGTH = 12;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_DATE_TIME;
  
    /**
    * Year reported by Gps 
    */
    public short year;
      
    /**
    * Month reported by Gps 
    */
    public short month;
      
    /**
    * Day reported by Gps 
    */
    public short day;
      
    /**
    * Hour reported by Gps 
    */
    public short hour;
      
    /**
    * Min reported by Gps 
    */
    public short min;
      
    /**
    * Sec reported by Gps  
    */
    public short sec;
      
    /**
    * Clock Status. See table 47 page 211 OEMStar Manual  
    */
    public short clockStat;
      
    /**
    * Visible satellites reported by Gps  
    */
    public short visSat;
      
    /**
    * Used satellites in Solution  
    */
    public short useSat;
      
    /**
    * GPS+GLONASS satellites in Solution  
    */
    public short GppGl;
      
    /**
    * GPS and GLONASS usage mask (bit 0 GPS_used? bit_4 GLONASS_used?)
    */
    public short sigUsedMask;
      
    /**
    * Percent used GPS
    */
    public short percentUsed;
    
    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gps_date_time() {
        msgid = MAVLINK_MSG_ID_GPS_DATE_TIME;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gps_date_time(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS_DATE_TIME;
        unpack(mavLinkPacket.payload);        
    }
    
    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GPS_DATE_TIME;
              
        packet.payload.putUnsignedByte(year);
              
        packet.payload.putUnsignedByte(month);
              
        packet.payload.putUnsignedByte(day);
              
        packet.payload.putUnsignedByte(hour);
              
        packet.payload.putUnsignedByte(min);
              
        packet.payload.putUnsignedByte(sec);
              
        packet.payload.putUnsignedByte(clockStat);
              
        packet.payload.putUnsignedByte(visSat);
              
        packet.payload.putUnsignedByte(useSat);
              
        packet.payload.putUnsignedByte(GppGl);
              
        packet.payload.putUnsignedByte(sigUsedMask);
              
        packet.payload.putUnsignedByte(percentUsed);
        
        return packet;
    }

    /**
    * Decode a gps_date_time message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.year = payload.getUnsignedByte();
              
        this.month = payload.getUnsignedByte();
              
        this.day = payload.getUnsignedByte();
              
        this.hour = payload.getUnsignedByte();
              
        this.min = payload.getUnsignedByte();
              
        this.sec = payload.getUnsignedByte();
              
        this.clockStat = payload.getUnsignedByte();
              
        this.visSat = payload.getUnsignedByte();
              
        this.useSat = payload.getUnsignedByte();
              
        this.GppGl = payload.getUnsignedByte();
              
        this.sigUsedMask = payload.getUnsignedByte();
              
        this.percentUsed = payload.getUnsignedByte();
        
    }
                      
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GPS_DATE_TIME -"+" year:"+year+" month:"+month+" day:"+day+" hour:"+hour+" min:"+min+" sec:"+sec+" clockStat:"+clockStat+" visSat:"+visSat+" useSat:"+useSat+" GppGl:"+GppGl+" sigUsedMask:"+sigUsedMask+" percentUsed:"+percentUsed+"";
    }
}
        