package com.chainauth.security;

import com.chainauth.user.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {
 @Test void signedTokenContainsUserEmailAsSubject(){JwtService service=new JwtService("0123456789abcdef0123456789abcdef0123456789abcdef",900);User user=new User();user.setEmail("test@example.com");String token=service.generate(user);assertEquals("test@example.com",service.subject(token));}
 @Test void rejectsWeakSigningSecret(){assertThrows(IllegalStateException.class,()->new JwtService("short",900));}
}
