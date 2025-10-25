package org.example.client;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        Map<String, String> a = parseArgs(args);
        String host = a.getOrDefault("--server", "127.0.0.1");
        int port = Integer.parseInt(a.getOrDefault("--port", "9000"));
        String name = a.get("--name");
        int delaySec = Integer.parseInt(a.getOrDefault("--delay", "0"));
        boolean abort = Boolean.parseBoolean(a.getOrDefault("--abort", "false"));
        if (name == null || name.isBlank()) {
            System.err.println("--name обязателен");
            System.exit(2);
        }

        try (SocketChannel ch = SocketChannel.open()) {
            ch.connect(new InetSocketAddress(host, port));
            byte[] payload = (name + "\0").getBytes(StandardCharsets.US_ASCII);
            ch.write(ByteBuffer.wrap(payload));

            if (abort) {
                return; // имитация аварийного завершения до чтения ответа
            }
            if (delaySec > 0) Thread.sleep(delaySec * 1000L);

            // читать ответ: [status][lenPriv][priv][lenCert][cert] либо ошибка
            ByteBuffer header = ByteBuffer.allocate(1);
            readFully(ch, header);
            header.flip();
            byte status = header.get();
            if (status == 0x01) {
                ByteBuffer lenBuf = ByteBuffer.allocate(4);
                readFully(ch, lenBuf);
                lenBuf.flip();
                int msgLen = lenBuf.getInt();
                ByteBuffer msgBuf = ByteBuffer.allocate(msgLen);
                readFully(ch, msgBuf);
                msgBuf.flip();
                String err = StandardCharsets.UTF_8.decode(msgBuf).toString();
                throw new RuntimeException("Ошибка сервера: " + err);
            }

            ByteBuffer len1 = ByteBuffer.allocate(4);
            readFully(ch, len1); len1.flip();
            int privLen = len1.getInt();
            ByteBuffer privBuf = ByteBuffer.allocate(privLen);
            readFully(ch, privBuf); privBuf.flip();

            ByteBuffer len2 = ByteBuffer.allocate(4);
            readFully(ch, len2); len2.flip();
            int certLen = len2.getInt();
            ByteBuffer certBuf = ByteBuffer.allocate(certLen);
            readFully(ch, certBuf); certBuf.flip();

            byte[] privDer = new byte[privBuf.remaining()];
            privBuf.get(privDer);
            byte[] certDer = new byte[certBuf.remaining()];
            certBuf.get(certDer);

            // сохранить .key (PEM PKCS#8) и .crt (PEM)
            String base = sanitize(name);
            Path keyPath = Path.of(base + ".key");
            Path crtPath = Path.of(base + ".crt");

            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privDer));
            try (OutputStream os = Files.newOutputStream(keyPath);
                 JcaPEMWriter w = new JcaPEMWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                w.writeObject(privateKey);
            }

            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new java.io.ByteArrayInputStream(certDer));
            try (OutputStream os = Files.newOutputStream(crtPath);
                 JcaPEMWriter w = new JcaPEMWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                w.writeObject(cert);
            }

            System.out.println("Saved: " + keyPath.toAbsolutePath() + " and " + crtPath.toAbsolutePath());
        }
    }

    private static void readFully(SocketChannel ch, ByteBuffer buf) throws Exception {
        while (buf.hasRemaining()) {
            int n = ch.read(buf);
            if (n == -1) throw new IllegalStateException("Connection closed");
        }
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
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
}



