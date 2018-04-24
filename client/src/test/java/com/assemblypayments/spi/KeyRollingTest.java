package com.assemblypayments.spi;

import com.assemblypayments.spi.model.KeyRollingResult;
import com.assemblypayments.spi.model.Message;
import com.assemblypayments.spi.model.Secrets;
import com.assemblypayments.spi.util.Events;
import com.assemblypayments.spi.util.KeyRollingHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyRollingTest {

    @Test
    public void testKeyRolling() {
        Message krRequest = new Message("x", Events.KEY_ROLL_REQUEST, null, false);

        Secrets oldSecrets = new Secrets("11A1162B984FEF626ECC27C659A8B0EEAD5248CA867A6A87BEA72F8A8706109D", "40510175845988F13F6162ED8526F0B09F73384467FA855E1E79B44A56562A58");

        KeyRollingResult krResult = KeyRollingHelper.performKeyRolling(krRequest, oldSecrets);

        assertEquals("0307C53DD0F119A1BC4CE61AA395882FB63BF8FCD0E0D27BBEB0D56AA9B24162", krResult.getNewSecrets().getEncKey());
        assertEquals("E4C3908437C14AC442C925FC8ED536C69FF67080D15FE007D69F8580D73FDF9D", krResult.getNewSecrets().getHmacKey());

        assertEquals("x", krResult.getKeyRollingConfirmation().getId());
        assertEquals(Events.KEY_ROLL_RESPONSE, krResult.getKeyRollingConfirmation().getEventName());
    }

}
