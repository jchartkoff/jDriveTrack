package events;

import java.util.EventObject;

public class JMVCommandEvent extends EventObject {
    public enum COMMAND {
        MOVE,
        ZOOM,
        ZOOM_IN_DISABLED,
        ZOOM_OUT_DISABLED
    }

    private COMMAND command;
    private Object newValue;

    private static final long serialVersionUID = 8701544867914969620L;

    public JMVCommandEvent(COMMAND cmd, Object source, Object newValue) {
        super(source);

        setCommand(cmd);
        setNewValue(newValue);
    }

    public JMVCommandEvent(Object source) {
        super(source);
    }

    public COMMAND getCommand() {
        return command;
    }

    public void setCommand(COMMAND command) {
        this.command = command;
    }
    
    public Object getNewValue() {
    	return newValue;
    }
    
    public void setNewValue(Object newValue) {
    	this.newValue = newValue;
    }
}
