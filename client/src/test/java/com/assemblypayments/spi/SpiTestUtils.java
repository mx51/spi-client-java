package com.assemblypayments.spi;

import com.assemblypayments.spi.model.Secrets;
import org.junit.Test;

public class SpiTestUtils {

    @Test
    public Spi clientWithTestSecrets() throws Spi.CompatibilityException {
        String encKey = "81CF9E6A14CDAF244A30B298D4CECB505C730CE352C6AF6E1DE61B3232E24D3F";
        String hmacKey = "D35060723C9EECDB8AEA019581381CB08F64469FC61A5A04FE553EBDB5CD55B9";
        Secrets secrets = new Secrets(encKey, hmacKey);
        Spi spi = new Spi("", "", "", secrets);
        return spi;
    }

}
