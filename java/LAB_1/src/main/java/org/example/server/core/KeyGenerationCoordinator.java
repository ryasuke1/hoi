package org.example.server.core;

import org.example.server.security.CertificateService;
import org.example.server.transport.NioServer;

import java.security.PrivateKey;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class KeyGenerationCoordinator {
    private final ServerConfig config;
    private final NioServer server;

    public KeyGenerationCoordinator(ServerConfig config) throws Exception {
        this.config = config;
        PrivateKey issuerKey = CertificateService.loadPrivateKeyFromPem(config.signingKeyPath);

        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "keygen-worker");
            t.setDaemon(true);
            return t;
        };

        var keygenPool = Executors.newFixedThreadPool(config.generatorThreads, threadFactory);
        this.server = new NioServer(config, issuerKey, keygenPool);
    }

    public void start() throws Exception {
        this.server.start();
    }
}



