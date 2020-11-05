package dhdhxji.command.marshaller;

import java.io.InvalidObjectException;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


class CommandWrapper {
    
    @JsonProperty("command")
    public Command command;



    public CommandWrapper(Command c) {
        command = c;
    }
}

public class Marshaller {
    public String serialize(Command c) throws InvalidObjectException {
        if(c.commandData.getClass() != _classes.get(c.commandName)) {
            throw new InvalidObjectException(
                String.format("Command '%s' requires data type '%s', not '%s'", 
                c.commandName, _classes.get(c.commandName), c.commandData.getClass()));
        }
        
        CommandWrapper wrapper = new CommandWrapper(c);

        try {
            return new ObjectMapper().writeValueAsString(wrapper);
        } catch(JsonProcessingException e) {
            throw new InvalidObjectException(e.toString());
        }
    }

    public Command deserialize(String json) throws InvalidObjectException {
        Command ret = new Command();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode tree = null;
        try {
            tree = mapper.readTree(json);
        } catch(JsonMappingException e) {
            throw new InvalidObjectException(e.toString());
        } catch(JsonProcessingException e) {
            throw new InvalidObjectException(e.toString());
        }

        tree = tree.get("command"); 

        ret.commandName = tree.get("command-name").asText();
        final JsonNode commandData = tree.get("command-data");

        try {
            ret.commandData = mapper.treeToValue(commandData, _classes.get(ret.commandName));
        } catch(JsonProcessingException e) {
            throw new InvalidObjectException(e.toString());
        }

        return ret;
    }

    public Marshaller registerCommand(String name,
                        Class<? extends CommandData> dataClass)
    {
        _classes.put(name, dataClass);

        return this;
    }

    private HashMap<String, Class<? extends CommandData>> _classes = 
                    new HashMap<String, Class<? extends CommandData>>();
}
