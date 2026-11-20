package com.chainauth.wallet;import org.springframework.data.mongodb.repository.MongoRepository;import java.util.Optional;
public interface WalletNonceRepository extends MongoRepository<WalletNonce,String>{Optional<WalletNonce> findByMessageAndAddress(String message,String address);}
