package com.chainauth.wallet;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SiweMessageFactoryTests {
    @Test void createsSiweMessageWithExpectedEip4361Fields() {
        SiweMessageFactory factory = new SiweMessageFactory("localhost:5173", "http://localhost:5173", 300);
        Instant issued = Instant.parse("2026-11-20T10:00:00Z");
        Instant expires = factory.expiry(issued);
        String message = factory.create("0x0000000000000000000000000000000000000001", 31337,
            "0123456789abcdef0123456789abcdef", issued, expires);
        assertTrue(message.startsWith("localhost:5173 wants you to sign in with your Ethereum account:"));
        assertTrue(message.contains("URI: http://localhost:5173"));
        assertTrue(message.contains("Version: 1"));
        assertTrue(message.contains("Chain ID: 31337"));
        assertTrue(message.contains("Nonce: 0123456789abcdef0123456789abcdef"));
        assertTrue(message.contains("Issued At: 2026-11-20T10:00:00Z"));
        assertTrue(message.contains("Expiration Time: 2026-11-20T10:05:00Z"));
        assertEquals(300, expires.getEpochSecond() - issued.getEpochSecond());
    }
}
