package org.example.server;

import org.example.server.core.KeyGenerationCoordinator;
import org.example.server.core.ServerConfig;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        ServerConfig config = ServerConfig.fromArgs(args);
        KeyGenerationCoordinator coordinator = new KeyGenerationCoordinator(config);
        coordinator.start();
    }
}



