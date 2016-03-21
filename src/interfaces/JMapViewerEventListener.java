package interfaces;

import java.util.EventListener;

import events.JMVCommandEvent;


/**
 * Must be implemented for processing commands while user
 * interacts with map viewer.
 *
 * @author Jason Huntley
 *
 */
public interface JMapViewerEventListener extends EventListener {
    void processCommand(JMVCommandEvent command);
}
