package no.k15g.service.xmldsig;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SignatureValidatorTest {

    @Test
    public void PeppolSmpExample() throws Exception {
        var inputStream = getClass().getResourceAsStream("/examples/peppol-smp.xml");
        Assert.assertNotNull(inputStream);

        var cert = SignatureValidator.validate(inputStream);
        Assert.assertNotNull(cert);
    }
}
