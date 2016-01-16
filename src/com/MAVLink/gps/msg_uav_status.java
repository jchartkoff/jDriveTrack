package com.MAVLink.gps;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Transmits the actual status values UAV in flight
*/
public class msg_uav_status extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_UAV_STATUS = 193;
    public static final int MAVLINK_MSG_LENGTH = 21;
    private static final long serialVersionUID = MAVLINK_MSG_ID_UAV_STATUS;

    /**
    * Latitude UAV
    */
    public float latitude;
      
    /**
    * Longitude UAV
    */
    public float longitude;
      
    /**
    * Altitude UAV
    */
    public float altitude;
      
    /**
    * Speed UAV
    */
    public float speed;
      
    /**
    * Course UAV
    */
    public float course;
      
    /**
    * The ID system reporting the action
    */
    public short target;

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_UAV_STATUS;
              
        packet.payload.putFloat(latitude);
              
        packet.payload.putFloat(longitude);
              
        packet.payload.putFloat(altitude);
              
        packet.payload.putFloat(speed);
              
        packet.payload.putFloat(course);
              
        packet.payload.putUnsignedByte(target);
        
        return packet;
    }

    /**
    * Decode a uav_status message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.latitude = payload.getFloat();
              
        this.longitude = payload.getFloat();
              
        this.altitude = payload.getFloat();
              
        this.speed = payload.getFloat();
              
        this.course = payload.getFloat();
              
        this.target = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_uav_status(){
        msgid = MAVLINK_MSG_ID_UAV_STATUS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_uav_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_UAV_STATUS;
        unpack(mavLinkPacket.payload);        
    }
           
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_UAV_STATUS -"+" latitude:"+latitude+" longitude:"+longitude+" altitude:"+altitude+" speed:"+speed+" course:"+course+" target:"+target+"";
    }
}
        