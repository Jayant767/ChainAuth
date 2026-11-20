package com.chainauth.user;

import com.chainauth.auth.TokenService;
import com.chainauth.blockchain.BlockchainService;
import com.chainauth.blockchain.IdentitySyncService;
import com.chainauth.wallet.WalletRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAccountServiceTests {
    @Test void suspendingAccountRevokesRefreshSessionsAndBumpsAccessTokenVersion() {
        UserRepository users=mock(UserRepository.class); WalletRepository wallets=mock(WalletRepository.class);
        TokenService tokens=mock(TokenService.class); BlockchainService blockchain=mock(BlockchainService.class);
        IdentitySyncService sync=mock(IdentitySyncService.class);
        User user=new User(); user.setId("u-1"); user.setEmail("user@example.com"); user.setTokenVersion(4);
        when(users.findById("u-1")).thenReturn(Optional.of(user)); when(users.save(user)).thenReturn(user);
        when(wallets.findAllByUserIdOrderByLinkedAtDesc("u-1")).thenReturn(List.of());
        var response=new AdminAccountService(users,wallets,tokens,blockchain,sync).suspend("u-1");
        assertFalse(user.isEnabled()); assertEquals(5,user.getTokenVersion());
        assertEquals(false,response.get("enabled")); verify(tokens).revokeUser("u-1");
    }

    @Test void refusesToSuspendTheLastEnabledAdministrator() {
        UserRepository users=mock(UserRepository.class); User admin=new User();admin.setId("admin-1");admin.setEnabled(true);admin.setRoles(java.util.Set.of(Role.ADMIN));
        when(users.findById("admin-1")).thenReturn(Optional.of(admin));when(users.findAll()).thenReturn(List.of(admin));
        AdminAccountService service=new AdminAccountService(users,mock(WalletRepository.class),mock(TokenService.class),mock(BlockchainService.class),mock(IdentitySyncService.class));
        var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.suspend("admin-1"));
        assertEquals(409,error.getStatusCode().value());assertTrue(admin.isEnabled());
    }

    @Test void refusesToDemoteTheLastEnabledAdministrator() {
        UserRepository users=mock(UserRepository.class); User admin=new User();admin.setId("admin-2");admin.setEnabled(true);admin.setRoles(java.util.Set.of(Role.ADMIN));
        when(users.findById("admin-2")).thenReturn(Optional.of(admin));when(users.findAll()).thenReturn(List.of(admin));
        AdminAccountService service=new AdminAccountService(users,mock(WalletRepository.class),mock(TokenService.class),mock(BlockchainService.class),mock(IdentitySyncService.class));
        var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.updateRoles("admin-2",java.util.Set.of(Role.USER)));
        assertEquals(409,error.getStatusCode().value());assertTrue(admin.getRoles().contains(Role.ADMIN));
    }

    @Test void reactivationDoesNotRestoreOldSessionsOrWalletState() {
        UserRepository users=mock(UserRepository.class); User user=new User(); user.setId("u-2"); user.setEnabled(false); user.setTokenVersion(8);
        when(users.findById("u-2")).thenReturn(Optional.of(user)); when(users.save(user)).thenReturn(user);
        var response=new AdminAccountService(users,mock(WalletRepository.class),mock(TokenService.class),mock(BlockchainService.class),mock(IdentitySyncService.class)).reactivate("u-2");
        assertTrue(user.isEnabled()); assertEquals(8,user.getTokenVersion());
        assertTrue(response.get("note").toString().contains("remain inactive"));
    }
}
