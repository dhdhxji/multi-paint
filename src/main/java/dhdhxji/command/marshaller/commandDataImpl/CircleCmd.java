package dhdhxji.command.marshaller.commandDataImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

import dhdhxji.command.marshaller.CommandData;

public class CircleCmd extends CommandData {
    @JsonProperty("x")
    public int x;

    @JsonProperty("y")
    public int y;

    @JsonProperty("radius")
    public int radius;

    @JsonProperty("color")
    public int color;

    public CircleCmd(int xVal, int yVal, int radiusVal, int colorVal) {
        x = xVal;
        y = yVal;
        color = colorVal;
        radius = radiusVal;
    }

    public CircleCmd() {}


    @Override
    public String toString() {
        return "x: " + x + " y: " + y + " radius " + radius + " color: " + color;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;


        CircleCmd command = (CircleCmd) o;
        // field comparison
        return x == command.x && 
               y == command.y &&
               color == command.color &&
               radius == command.radius;
    }
}
