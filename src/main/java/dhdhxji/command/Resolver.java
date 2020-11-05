package dhdhxji.command;

import java.io.InvalidObjectException;
import java.util.Vector;

import dhdhxji.command.marshaller.Command;
import dhdhxji.command.marshaller.Marshaller;
import dhdhxji.command.marshaller.commandDataImpl.LoginCmd;
import dhdhxji.command.marshaller.commandDataImpl.SetCmd;
import dhdhxji.command.marshaller.commandDataImpl.SizeCmd;
import dhdhxji.command.marshaller.commandDataImpl.StripCmd;
import dhdhxji.connection_manager.ConnectionManager;
import dhdhxji.connection_manager.Message;
import dhdhxji.connection_manager.ProcessCommandListener;
import dhdhxji.connection_manager.IdMap.IdItemHandle;
import dhdhxji.pixmap.DrawInterface;

public class Resolver implements ProcessCommandListener {
    public Resolver(DrawInterface drawer){
        _drawer = drawer;
        _commandMarshaller = new Marshaller()
            .registerCommand("login", LoginCmd.class)
            .registerCommand("set", SetCmd.class)
            .registerCommand("size", SizeCmd.class)
            .registerCommand("strip", StripCmd.class);
    }

    public void process_command(
        ConnectionManager server,
        IdItemHandle clientHandle,
        String command
    ) {
        try {
            Command request = _commandMarshaller.deserialize(command);
            
            if(request.commandName.equals("login")) {
                Command respSize = new Command(
                    "size",
                    new SizeCmd(_drawer.getWidth(), _drawer.getHeigth())
                );
                server.send(serializeCommand(respSize), clientHandle);
                
                sendStrip(server, clientHandle);
            } else if(request.commandName.equals("set")) {
                final SetCmd reqData = (SetCmd)request.commandData;

                _drawer.setPix(reqData.x, reqData.y, reqData.color);
                Command response = new Command(
                    "set",
                    new SetCmd(reqData.x, reqData.y, reqData.color)
                );
                server.broadcast(serializeCommand(response));
            }
        } catch(InvalidObjectException e) {
            //shit happanes, command probably has unsupported 
            //format
            System.err.print("Could not process command: " + command);
            System.err.print(e);
        }
    }

    private Message serializeCommand(Command cmd) throws InvalidObjectException {
        return new Message(_commandMarshaller.serialize(cmd));
    }

    private void sendStrip(ConnectionManager mng, IdItemHandle client) 
        throws InvalidObjectException 
    {
        int startXPos = 0;
        int startYPos = 0;
        Vector<Integer> pix_to_send = new Vector<Integer>();

        for(int y = 0; y < _drawer.getHeigth(); ++y) {
            for(int x = 0; x < _drawer.getWidth(); ++x) {
                int color = _drawer.getPix(x, y); 
                
                if(color != 0) {
                    pix_to_send.add(color);
                } else {
                    if(pix_to_send.size() != 0) {
                        int[] intArr = new int[pix_to_send.size()];
                        for(int i = 0; i < pix_to_send.size(); ++i) {
                            intArr[i] = pix_to_send.get(i).intValue();
                        }
    
                        Command stripResp = new Command(
                            "strip",
                            new StripCmd(startXPos, startYPos, intArr)
                        );
    
                        mng.send(serializeCommand(stripResp), client);
                        pix_to_send.clear();
                    }
 
                    startXPos = x+1;
                    startYPos = y + Math.max(0, (x+1-_drawer.getWidth() + 1));//(x+1 == _drawer.getWidth()); 
                }
            }
        }
    }

    public Marshaller getMarshaller() {
        return _commandMarshaller;
    }


    private DrawInterface _drawer = null;
    private Marshaller _commandMarshaller = null;
}
