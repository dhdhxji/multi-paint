package dhdhxji;

import dhdhxji.command.Resolver;
import dhdhxji.connection_manager.ConnectionManager;
import dhdhxji.pixmap.DrawInterface;
import dhdhxji.pixmap.DrawPixmap;

public class App 
{
    public static void main( String[] args )
    {
        //Main draw interface
        DrawInterface mainDrawer = new DrawPixmap(1920, 1080);
        
        //Command resolver: parse, deserialize and execute commands
        Resolver mainResolver = new Resolver(mainDrawer);
        
        //  Connection manager. Manages User sessions, transfer
        //data between clients and ProcessCommandListener interface
        //(Resolver in this particular example)
        ConnectionManager mainConnectionManager = 
            new ConnectionManager(mainResolver, 3113);

        //Run the server
        mainConnectionManager.start();
    }
}
