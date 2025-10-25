package org.example.server.security;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

public final class CertificateService {
    public static PrivateKey loadPrivateKeyFromPem(Path pemPath) throws Exception {
        try (Reader r = Files.newBufferedReader(pemPath, StandardCharsets.UTF_8);
             PemReader pemReader = new PemReader(r)) {
            PemObject obj = pemReader.readPemObject();
            if (obj == null) throw new IllegalArgumentException("PEM файл пуст: " + pemPath);
            byte[] content = obj.getContent();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(content));
        }
    }

    public static X509Certificate issueCertificate(String issuerDn, PrivateKey issuerKey, PublicKey subjectPublicKey, String subjectDn) throws Exception {
        X500Name issuer = new X500Name(issuerDn);
        X500Name subject = new X500Name(subjectDn);
        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(subjectPublicKey.getEncoded());

        Instant now = Instant.now();
        Date notBefore = Date.from(now.minusSeconds(60));
        Date notAfter = Date.from(now.plusSeconds(31536000L)); // ~1 год

        BigInteger serial = new BigInteger(160, SecureRandom.getInstanceStrong());
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                spki
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey);
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }
}


