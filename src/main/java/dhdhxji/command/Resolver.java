package dhdhxji.command;

import java.io.InvalidObjectException;
import java.util.Vector;

import dhdhxji.command.marshaller.Command;
import dhdhxji.command.marshaller.Marshaller;
import dhdhxji.command.marshaller.commandDataImpl.CircleCmd;
import dhdhxji.command.marshaller.commandDataImpl.LoginCmd;
import dhdhxji.command.marshaller.commandDataImpl.SetCmd;
import dhdhxji.command.marshaller.commandDataImpl.SizeCmd;
import dhdhxji.command.marshaller.commandDataImpl.StripCmd;
import dhdhxji.connection_manager.ConnectionManager;
import dhdhxji.connection_manager.Message;
import dhdhxji.connection_manager.ProcessCommandListener;
import dhdhxji.connection_manager.IdMap.IdItemHandle;
import dhdhxji.pixmap.DrawInterface;
import dhdhxji.pixmap.Pixel;
import dhdhxji.pixmap.Strip;

public class Resolver implements ProcessCommandListener {
    public Resolver(DrawInterface drawer){
        _drawer = drawer;
        _commandMarshaller = new Marshaller()
            .registerCommand("login", LoginCmd.class)
            .registerCommand("set", SetCmd.class)
            .registerCommand("size", SizeCmd.class)
            .registerCommand("strip", StripCmd.class)
            .registerCommand("circle", CircleCmd.class);
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
            } else if(request.commandName.equals("circle")) {
                final CircleCmd reqData = (CircleCmd)request.commandData;
                
                final int radius = reqData.radius;
                final int sqPointRadius = radius*radius;
                final int color = reqData.color;
                final int x = reqData.x;
                final int y = reqData.y;
                
                final Vector<Pixel> circle = new Vector<>();
                for(int yd = -radius; yd < radius; ++yd) {
                    final int chordArm = (int)Math.sqrt(sqPointRadius - yd*yd);
                    
                    for(int xd = -chordArm; xd < chordArm; ++xd) {
                        circle.add(new Pixel(x+xd, y+yd, color));
                    }
                }

                _drawer.setMultiPix(circle.toArray(new Pixel[circle.size()]));

                server.broadcast(serializeCommand(request));
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
        Strip[] strips = _drawer.getNonZeroStrips();

        for(Strip strip: strips) {
            Command stripResp = new Command(
                "strip",
                new StripCmd(strip.x_start, strip.y_start, strip.colors)
            );

            mng.send(serializeCommand(stripResp), client);
        }
    }

    public Marshaller getMarshaller() {
        return _commandMarshaller;
    }


    private DrawInterface _drawer = null;
    private Marshaller _commandMarshaller = null;
}
