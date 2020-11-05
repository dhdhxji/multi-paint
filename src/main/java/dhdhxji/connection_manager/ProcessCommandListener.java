package dhdhxji.connection_manager;

import dhdhxji.connection_manager.IdMap.IdItemHandle;

public interface ProcessCommandListener {
    public void process_command(
        ConnectionManager server,
        IdItemHandle client_handle, 
        String command
    );
}
