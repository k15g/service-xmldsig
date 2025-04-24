package no.k15g.service.xmldsig;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.security.cert.X509Certificate;

public interface SignatureValidator {

    static X509Certificate validate(InputStream inputStream) throws Exception {
        // Create a DocumentBuilderFactory and set it to be namespace aware
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Read received XML into a DOM Document
        var doc = factory.newDocumentBuilder().parse(inputStream);

        // Find signature
        var nodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nodes.getLength() == 0)
            throw new Exception("Cannot find Signature element");

        // Create a DOMValidateContext and specify a KeySelector
        var keySelector = new X509KeySelector();
        var valContext = new DOMValidateContext(keySelector, nodes.item(0));

        // Validate the signature
        var fac = XMLSignatureFactory.getInstance("DOM");
        var signature = fac.unmarshalXMLSignature(valContext);

        if (signature.validate(valContext))
            return keySelector.getCertificate();

        throw new Exception("Signature validation failed");
    }
}
