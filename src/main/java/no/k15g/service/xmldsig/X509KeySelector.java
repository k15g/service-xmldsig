package no.k15g.service.xmldsig;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.cert.X509Certificate;

public class X509KeySelector extends KeySelector {

    private X509Certificate certificate;

    @Override
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
        for (Object keyInfoContent : keyInfo.getContent()) {
            if (keyInfoContent instanceof X509Data x509Data) {
                for (Object obj : x509Data.getContent()) {
                    if (obj instanceof X509Certificate cert) {
                        certificate = cert;
                        return cert::getPublicKey;
                    }
                }
            }
        }

        // If no matching key is found, throw an exception
        throw new KeySelectorException("No key found");
    }

    public X509Certificate getCertificate() {
        return certificate;
    }
}
