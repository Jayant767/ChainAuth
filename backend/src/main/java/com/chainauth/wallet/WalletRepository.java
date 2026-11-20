package com.chainauth.wallet;import org.springframework.data.mongodb.repository.MongoRepository;import java.util.*;
public interface WalletRepository extends MongoRepository<Wallet,String>{Optional<Wallet> findByAddress(String address);List<Wallet> findAllByUserIdOrderByLinkedAtDesc(String userId);Optional<Wallet> findByIdAndUserId(String id,String userId);}
