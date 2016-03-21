package interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import types.Layer;
import types.Style;


public interface MapObject {

    Layer getLayer();

    void setLayer(Layer layer);

    Style getStyle();

    Style getStyleAssigned();

    Color getColor();

    Color getBackColor();

    Stroke getStroke();

    Font getFont();

    String getName();
    
    int getID();

    boolean isVisible();
}
