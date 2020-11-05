package dhdhxji.command.marshaller.commandDataImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

import dhdhxji.command.marshaller.CommandData;

public class LoginCmd extends CommandData{
    
    @JsonProperty("username")
    public String username = null;


    
    public LoginCmd(String userNameVal) {
        username = userNameVal;
    }

    public LoginCmd() {}



    @Override
    public String toString() {
        return "username: " + username;
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


        LoginCmd command = (LoginCmd) o;
        // field comparison
        return username.equals(command.username);
    }
}
