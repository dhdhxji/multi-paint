package dhdhxji.command.marshaller;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Command {
    
    @JsonProperty("command-name")
    public String commandName;

    @JsonProperty("command-data")
    public CommandData commandData;

    public Command(String name, CommandData data) {
        commandName = name;
        commandData = data;
    }

    public Command() {}

    @Override
    public String toString() {
        return "name: " + commandName + " data: " + commandData.toString();
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


        Command command = (Command) o;
        // field comparison
        return this.commandName.equals(command.commandName) && 
               this.commandData.equals(command.commandData);
    }
}
