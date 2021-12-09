package dhdhxji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dhdhxji.command.Resolver;
import dhdhxji.connection_manager.ConnectionManager;
import dhdhxji.connection_manager.RedisConnectionManager;
import dhdhxji.pixmap.DrawInterface;
import dhdhxji.pixmap.RedisPixmap;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
        String redis_addr = System.getenv("REDIS_ADDR");
        if(redis_addr == null) {
            System.out.println("WARN: REDIS_ADDR not specified, using localhost");
            redis_addr = "localhost";
        }

        
        String width = System.getenv("CANVAS_WIDTH");
        if(width == null) {
            System.out.println("WARN: CANVAS_WIDTH not specified, using localhost");
            width = "128";
        }
        String height = System.getenv("CANVAS_HEIGHT");
        if(height == null) {
            System.out.println("WARN: CANVAS_HEIGHT not specified, using localhost");
            height = "128";
        }

        int w = Integer.parseInt(width);
        int h = Integer.parseInt(height);
        

        //Main draw interface
        DrawInterface mainDrawer = new RedisPixmap(redis_addr, w, h);
        
        //Command resolver: parse, deserialize and execute commands
        Resolver mainResolver = new Resolver(mainDrawer);
        
        //  Connection manager. Manages User sessions, transfer
        //data between clients and ProcessCommandListener interface
        //(Resolver in this particular example)
        ConnectionManager mainConnectionManager = 
            new RedisConnectionManager(redis_addr, mainResolver, 3113);

        //Run the server
        System.out.println("Starting the server");
        mainConnectionManager.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        // Check is stdin & stdout are present
        if(System.console() != null) {
            while(true) {
                String cmd = null;
                try {
                    System.out.print("> ");
                    cmd = in.readLine();
                } catch(IOException e){
                    break;
                }
    
                if(cmd.equals("exit")){
                    break;
                } else {
                    System.out.println("Unknown command. Type 'exit' for exit.");
                }
            }
        } else {
            mainConnectionManager.join();
        }

        mainResolver.stop();
        mainConnectionManager.stopServer();
        System.out.println("Server successfully stopped");
    }
}
