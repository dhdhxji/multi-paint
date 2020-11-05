package dhdhxji.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import dhdhxji.command.marshaller.Command;
import dhdhxji.command.marshaller.Marshaller;
import dhdhxji.command.marshaller.commandDataImpl.LoginCmd;
import dhdhxji.command.marshaller.commandDataImpl.SetCmd;
import dhdhxji.command.marshaller.commandDataImpl.SizeCmd;
import dhdhxji.command.marshaller.commandDataImpl.StripCmd;
import dhdhxji.connection_manager.ConnectionManager;
import dhdhxji.connection_manager.Message;
import dhdhxji.connection_manager.IdMap.IdItemHandle;
import dhdhxji.pixmap.DrawPixmap;

public class ResolverTest {
    class FakeConnectionManager extends ConnectionManager {

        FakeConnectionManager() {
            super(null, 0);
        }

        @Override
        public void send(Message cmd, IdItemHandle receiver) {
            sendCommands.add(cmd.message);
        }

        @Override
        public void broadcast(Message cmd) {
            broadcastCommands.add(cmd.message);
        }

        public Vector<String> sendCommands = new Vector<String>();
        public Vector<String> broadcastCommands = new Vector<String>();

        /*
         * Dumb methods, for prevent change state of parent class
        */
        @Override
        public void run() {
        }


        @Override
        public void stopServer() {
        }
    }

    public FakeConnectionManager cm = null;
    public Resolver testResolver = null;
    public Marshaller commandMarshaller = null;

    private boolean checkResponseSizes(int single, int broadcast) {
        return single == cm.sendCommands.size() && 
               broadcast == cm.broadcastCommands.size();
    }

    @Before
    public void updateContext() {
        cm = new FakeConnectionManager();
        testResolver = new Resolver(new DrawPixmap(20,20));
        commandMarshaller = testResolver.getMarshaller();
    }

    @Test
    public void testSetCommand() throws InvalidObjectException {
        Command setCmd = new Command(
            "set",
            new SetCmd(5, 5, 1234)
        );
        testResolver.process_command(cm, null, 
            commandMarshaller.serialize(setCmd));


        assertTrue(checkResponseSizes(0, 1));


        String response = cm.broadcastCommands.get(0);

        final String expectedResponse = commandMarshaller.serialize(
            new Command(
                "set",
                new SetCmd(5, 5, 1234)
            )
        );
        
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testLoginCommand() throws InvalidObjectException {
        //set 2 pixels for test
        Command setCmd = new Command(
            "set",
            new SetCmd(5, 5, 1234)
        );
        testResolver.process_command(cm, null, 
            commandMarshaller.serialize(setCmd));

        setCmd = new Command(
            "set",
            new SetCmd(8, 6, 1234)
        );
        testResolver.process_command(cm, null, 
            commandMarshaller.serialize(setCmd));
        
        //clear responses
        cm.broadcastCommands.clear();


        Command loginCmd = new Command(
            "login",
            new LoginCmd("Vasia")
        );
        testResolver.process_command(cm, null, 
            commandMarshaller.serialize(loginCmd));

        //there are should be size and 2 strips
        assertTrue(checkResponseSizes(3, 0));

        //size
        String response = cm.sendCommands.get(0);

        String expectedResponse = commandMarshaller.serialize(
            new Command(
                "size",
                new SizeCmd(20, 20)
        ));
        
        assertEquals(expectedResponse, response);

        //strip #1
        response = cm.sendCommands.get(1);

        int[] stripColors = {1234};
        expectedResponse = commandMarshaller.serialize(
            new Command(
                "strip",
                new StripCmd(5, 5, stripColors)
            )
        );

        assertEquals(expectedResponse, response);

        //strip #2
        response = cm.sendCommands.get(2);

        expectedResponse = commandMarshaller.serialize(
            new Command(
                "strip",
                new StripCmd(8, 6, stripColors)
            )
        );

        assertEquals(expectedResponse, response);
    }
}
