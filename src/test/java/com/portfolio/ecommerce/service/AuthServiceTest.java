package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.*;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CustomerCredentialRepository credentials = mock(CustomerCredentialRepository.class);
    private final AuthService auth = new AuthService(customers, credentials,
            "local-test-secret-key-32-characters-minimum");

    @Test
    void registrationHashesPasswordAndNeverReturnsIt() {
        when(customers.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.id = 42L;
            return customer;
        });
        var result = auth.register(new RegisterRequest("New", "NEW@example.com", "password123"));
        assertEquals(42L, result.get("customerId"));
        assertFalse(result.containsKey("password"));
        var captor = org.mockito.ArgumentCaptor.forClass(CustomerCredential.class);
        verify(credentials).save(captor.capture());
        assertNotEquals("password123", captor.getValue().passwordHash);
        assertTrue(captor.getValue().passwordHash.startsWith("$2"));
    }

    @Test
    void duplicateRegistrationReturnsConflict() {
        when(customers.findByEmailIgnoreCase("taken@example.com"))
                .thenReturn(Optional.of(new Customer("Existing", "taken@example.com")));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> auth.register(new RegisterRequest("New", "taken@example.com", "password123")));
        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void unknownLoginReturnsUnauthorized() {
        when(customers.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> auth.login(new LoginRequest("missing@example.com", "password123")));
        assertEquals(401, error.getStatusCode().value());
    }
}
