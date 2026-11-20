package com.chainauth.blockchain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityRoleTests {
    @Test void mapsContractRoleValuesToApplicationNames() {
        assertEquals(IdentityRole.USER, IdentityRole.fromContractValue(0));
        assertEquals(IdentityRole.MODERATOR, IdentityRole.fromContractValue(1));
        assertEquals(IdentityRole.ADMIN, IdentityRole.fromContractValue(2));
        assertEquals(0, IdentityRole.USER.contractValue());
        assertEquals(1, IdentityRole.MODERATOR.contractValue());
        assertEquals(2, IdentityRole.ADMIN.contractValue());
    }

    @Test void rejectsUnknownContractRoleValues() {
        assertThrows(IllegalArgumentException.class, () -> IdentityRole.fromContractValue(3));
    }
}
