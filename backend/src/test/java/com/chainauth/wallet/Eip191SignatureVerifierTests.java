package com.chainauth.wallet;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import static org.junit.jupiter.api.Assertions.*;
class Eip191SignatureVerifierTests {
 @Test void recoversSignerForPersonalSignAndRejectsAlteredMessage() throws Exception {
  // Deterministic TEST-ONLY key fixture; never funded or used outside this unit test.\n  Credentials signer=Credentials.create("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
  String message="ChainAuth SIWE test challenge";
  Sign.SignatureData sig=Sign.signPrefixedMessage(message.getBytes(java.nio.charset.StandardCharsets.UTF_8),signer.getEcKeyPair());
  byte[] joined=new byte[65];System.arraycopy(sig.getR(),0,joined,0,32);System.arraycopy(sig.getS(),0,joined,32,32);joined[64]=sig.getV();
  String signature=Numeric.toHexString(joined),address="0x"+Keys.getAddress(signer.getEcKeyPair());
  Eip191SignatureVerifier verifier=new Eip191SignatureVerifier();
  assertTrue(verifier.verify(message,signature,address));assertFalse(verifier.verify(message+" altered",signature,address));assertFalse(verifier.verify(message,"0x1234",address));
 }
}
