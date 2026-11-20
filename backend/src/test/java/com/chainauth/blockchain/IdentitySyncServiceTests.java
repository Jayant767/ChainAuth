package com.chainauth.blockchain;

import com.chainauth.wallet.Wallet;
import com.chainauth.wallet.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdentitySyncServiceTests {
    @Test void enqueuesJobForLinkedWalletAndPersistsIdempotencyKey() {
        IdentitySyncJobRepository jobs = mock(IdentitySyncJobRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        Wallet wallet = new Wallet(); wallet.setUserId("user-1"); wallet.setAddress("0x0000000000000000000000000000000000000001");
        when(wallets.findByAddress(wallet.getAddress())).thenReturn(Optional.of(wallet));
        when(jobs.findByIdempotencyKey("request-1")).thenReturn(Optional.empty());
        when(jobs.save(any(IdentitySyncJob.class))).thenAnswer(inv -> inv.getArgument(0));
        IdentitySyncJob result = new IdentitySyncService(jobs, wallets).enqueueRegistration(wallet.getAddress(), IdentityRole.USER, "request-1");
        assertEquals("request-1", result.getIdempotencyKey());
        assertEquals("user-1", result.getUserId());
        assertEquals(SyncStatus.QUEUED, result.getStatus());
        verify(jobs).save(any(IdentitySyncJob.class));
    }

    @Test void repeatedIdempotencyKeyReturnsExistingJob() {
        IdentitySyncJobRepository jobs = mock(IdentitySyncJobRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        IdentitySyncJob existing = new IdentitySyncJob(); existing.setIdempotencyKey("same-key"); existing.setWalletAddress("0x0000000000000000000000000000000000000001"); existing.setOperation("REGISTER"); existing.setRole(IdentityRole.USER); existing.setStatus(SyncStatus.SUBMITTED);
        Wallet wallet = new Wallet(); wallet.setUserId("user-1"); wallet.setAddress("0x0000000000000000000000000000000000000001");
        when(wallets.findByAddress(wallet.getAddress())).thenReturn(Optional.of(wallet));
        when(jobs.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));
        IdentitySyncJob result = new IdentitySyncService(jobs, wallets).enqueueRegistration("0x0000000000000000000000000000000000000001", IdentityRole.USER, "same-key");
        assertSame(existing, result);
        verify(wallets).findByAddress(wallet.getAddress());
        verify(jobs, never()).save(any());
    }

    @Test void rejectsIdempotencyKeyReusedForDifferentPayload() {
        IdentitySyncJobRepository jobs = mock(IdentitySyncJobRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        Wallet wallet = new Wallet(); wallet.setUserId("user-1"); wallet.setAddress("0x0000000000000000000000000000000000000001");
        when(wallets.findByAddress(wallet.getAddress())).thenReturn(Optional.of(wallet));
        IdentitySyncJob existing = new IdentitySyncJob(); existing.setIdempotencyKey("same-key"); existing.setWalletAddress(wallet.getAddress()); existing.setOperation("REGISTER"); existing.setRole(IdentityRole.USER);
        when(jobs.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class, () -> new IdentitySyncService(jobs, wallets).enqueueRegistration(wallet.getAddress(), IdentityRole.ADMIN, "same-key"));
        verify(jobs, never()).save(any());
    }

    @Test void rejectsUnlinkedWallet() {
        IdentitySyncJobRepository jobs = mock(IdentitySyncJobRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        when(wallets.findByAddress(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> new IdentitySyncService(jobs, wallets)
            .enqueueRegistration("0x0000000000000000000000000000000000000001", IdentityRole.USER, "key"));
    }
}
