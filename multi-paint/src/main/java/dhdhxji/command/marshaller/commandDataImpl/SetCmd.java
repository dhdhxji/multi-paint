package dhdhxji.command.marshaller.commandDataImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

import dhdhxji.command.marshaller.CommandData;

public class SetCmd extends CommandData {

    @JsonProperty("x")
    public int x;

    @JsonProperty("y")
    public int y;

    @JsonProperty("color")
    public int color;

    public SetCmd(int xVal, int yVal, int colorVal) {
        x = xVal;
        y = yVal;
        color = colorVal;
    }

    public SetCmd() {}


    @Override
    public String toString() {
        return "x: " + x + " y: " + y + " color: " + color;
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


        SetCmd command = (SetCmd) o;
        // field comparison
        return x == command.x && 
               y == command.y &&
               color == command.color;
    }
}
