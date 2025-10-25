package org.example.server.transport;

import org.example.server.core.ServerConfig;
import org.example.server.security.CertificateService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;

public final class NioServer {
    private final ServerConfig config;
    private final PrivateKey issuerKey;
    private final ExecutorService keygenPool;

    private final Map<String, CompletableFuture<KeyRecord>> nameToFuture = new ConcurrentHashMap<>();

    public NioServer(ServerConfig config, PrivateKey issuerKey, ExecutorService keygenPool) {
        this.config = config;
        this.issuerKey = issuerKey;
        this.keygenPool = keygenPool;
    }

    public void start() throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(config.host, config.port));
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server listening on " + config.host + ":" + config.port);

        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                try {
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) accept(selector, server);
                    if (key.isReadable()) read(key);
                    if (key.isWritable()) write(key);
                } catch (CancelledKeyException ignored) {
                }
            }
        }
    }

    private void accept(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel ch = server.accept();
        if (ch == null) return;
        ch.configureBlocking(false);
        ClientState state = new ClientState();
        ch.register(selector, SelectionKey.OP_READ, state);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        ClientState st = (ClientState) key.attachment();
        ByteBuffer buf = st.readBuffer;
        int n = ch.read(buf);
        if (n == -1) { close(key); return; }
        buf.flip();
        while (buf.hasRemaining()) {
            byte b = buf.get();
            if (b == 0) { // null-terminated ASCII name
                String name = st.nameBuilder.toString();
                st.nameBuilder.setLength(0);
                handleNameRequest(key, name);
            } else {
                if ((b & 0x80) != 0) {
                    // enforce ASCII only
                    st.invalid = true;
                }
                st.nameBuilder.append((char) b);
                if (st.nameBuilder.length() > 1024) st.invalid = true;
            }
        }
        buf.compact();
        if (st.invalid) close(key);
    }

    private void handleNameRequest(SelectionKey key, String name) {
        ClientState st = (ClientState) key.attachment();
        CompletableFuture<KeyRecord> fut = nameToFuture.computeIfAbsent(name, this::submitGeneration);
        st.pending = fut;
        fut.whenComplete((rec, err) -> {
            st.responseReady.set(true);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        });
    }

    private CompletableFuture<KeyRecord> submitGeneration(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(8192);
                KeyPair kp = kpg.generateKeyPair();
                String subjectDn = "CN=" + name;
                X509Certificate cert = CertificateService.issueCertificate(config.issuerDn, issuerKey, kp.getPublic(), subjectDn);
                return new KeyRecord(kp.getPrivate(), cert);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, keygenPool);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        ClientState st = (ClientState) key.attachment();
        if (st.pending == null || !st.responseReady.get()) return;
        if (st.outBuffer == null) {
            try {
                KeyRecord rec = st.pending.join();
                st.outBuffer = encodeResponse(rec);
            } catch (CompletionException ex) {
                st.outBuffer = encodeError(ex.getCause() != null ? ex.getCause().toString() : ex.toString());
            }
        }
        ByteBuffer buf = st.outBuffer;
        ch.write(buf);
        if (!buf.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
            st.resetResponse();
        } else {
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }
    }

    private ByteBuffer encodeResponse(KeyRecord rec) {
        byte[] priv = rec.privateKey.getEncoded();
        byte[] cert;
        try { cert = rec.certificate.getEncoded(); } catch (Exception e) { cert = new byte[0]; }
        // protocol: [status=0x00][lenPriv(4)][priv][lenCert(4)][cert]
        int len = 1 + 4 + priv.length + 4 + cert.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put((byte)0x00);
        buf.putInt(priv.length);
        buf.put(priv);
        buf.putInt(cert.length);
        buf.put(cert);
        buf.flip();
        return buf;
    }

    private ByteBuffer encodeError(String message) {
        byte[] msg = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = 1 + 4 + msg.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put((byte)0x01);
        buf.putInt(msg.length);
        buf.put(msg);
        buf.flip();
        return buf;
    }

    private void close(SelectionKey key) {
        try { key.channel().close(); } catch (IOException ignore) {}
        key.cancel();
    }

    private static final class ClientState {
        final StringBuilder nameBuilder = new StringBuilder();
        final ByteBuffer readBuffer = ByteBuffer.allocateDirect(4096);
        final java.util.concurrent.atomic.AtomicBoolean responseReady = new java.util.concurrent.atomic.AtomicBoolean(false);
        CompletableFuture<KeyRecord> pending;
        ByteBuffer outBuffer;
        boolean invalid = false;
        void resetResponse() { this.pending = null; this.outBuffer = null; this.responseReady.set(false); }
    }

    public static final class KeyRecord {
        public final PrivateKey privateKey;
        public final X509Certificate certificate;

        public KeyRecord(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }
    }
}



