package com.chainauth.auth;

import com.chainauth.user.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceUnitTests {
 @Test void normalizesEmailUsingLocaleIndependentLowercase(){assertEquals("person@example.com",AuthService.normalize("  Person@Example.COM  "));}
 @Test void userDefaultsToEnabledAndUserRole(){User user=new User();assertTrue(user.isEnabled());assertTrue(user.getRoles().contains(Role.USER));}
}
