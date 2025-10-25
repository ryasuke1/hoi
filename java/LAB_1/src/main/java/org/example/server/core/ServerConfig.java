package org.example.server.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ServerConfig {
    public final String host;
    public final int port;
    public final int generatorThreads;
    public final String issuerDn;
    public final Path signingKeyPath;

    private ServerConfig(String host, int port, int generatorThreads, String issuerDn, Path signingKeyPath) {
        this.host = host;
        this.port = port;
        this.generatorThreads = generatorThreads;
        this.issuerDn = issuerDn;
        this.signingKeyPath = signingKeyPath;
    }

    public static ServerConfig fromArgs(String[] args) {
        Map<String, String> map = parseArgs(args);
        String host = map.getOrDefault("--host", "0.0.0.0");
        int port = Integer.parseInt(map.getOrDefault("--port", "9000"));
        int generatorThreads = Integer.parseInt(map.getOrDefault("--generator-threads", String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors()))));
        String issuer = map.get("--issuer");
        String signingKey = map.get("--signing-key");
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("--issuer обязателен, например: \"CN=Test CA\"");
        }
        if (signingKey == null || signingKey.isBlank()) {
            throw new IllegalArgumentException("--signing-key обязателен, путь к приватному ключу Issuer в PEM PKCS#8");
        }
        return new ServerConfig(host, port, generatorThreads, stripQuotes(issuer), Path.of(signingKey));
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> out = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    out.put(a, args[++i]);
                } else {
                    out.put(a, "true");
                }
            }
        }
        return out;
    }

    private static String stripQuotes(String s) {
        if (s == null) return null;
        String t = s.trim();
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            return t.substring(1, t.length() - 1);
        }
        return t;
    }
}


