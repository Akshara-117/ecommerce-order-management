package com.portfolio.ecommerce.config;

import com.portfolio.ecommerce.repository.CustomerRepository;
import com.portfolio.ecommerce.repository.CustomerCredentialRepository;
import com.portfolio.ecommerce.entity.Customer;
import com.portfolio.ecommerce.entity.CustomerCredential;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitytest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "JWT_SECRET=integration-test-secret-key-at-least-32-characters"
})
class SecurityConfigIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CustomerRepository customers;
    @Autowired CustomerCredentialRepository credentials;

    @Test void anonymousCustomerOrdersAreUnauthorized() throws Exception {
        mvc.perform(get("/api/customers/1/orders")).andExpect(status().isUnauthorized());
    }

    @Test void productBrowsingRemainsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test void anonymousProductMutationIsUnauthorized() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Nope\",\"price\":1,\"stock\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test void customerCannotReadAnotherCustomersOrders() throws Exception {
        String email = "security-test-" + java.util.UUID.randomUUID() + "@example.com";
        String registration = "{\"name\":\"Security Test\",\"email\":\"" + email + "\",\"password\":\"password123\"}";
        String registered = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON).content(registration))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long customerId = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(registered).get("customerId").asLong();
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
            .readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"security-test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
            .get("accessToken").asText();
        mvc.perform(get("/api/customers/" + customerId + "/orders")
            .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/customers/" + (customerId + 1) + "/orders")
            .header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }
}
