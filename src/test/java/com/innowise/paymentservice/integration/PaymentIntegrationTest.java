package com.innowise.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.paymentservice.PaymentServiceApplication;
import com.innowise.paymentservice.config.TestSecurityConfiguration;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(classes = {
        PaymentServiceApplication.class,
        TestSecurityConfiguration.class
})
class PaymentIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Jwt userJwt;

    private Jwt adminJwt;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {

        mongoTemplate.getDb().drop();

        System.setProperty("app.async.enabled", "false");

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        wireMockServer.resetAll();

        stubFor(get(urlMatching("/random"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("2")));

        userJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
//                .claim("role", "ROLE_USER")
                .build();

        adminJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
//                .claim("role", "ROLE_ADMIN")
                .build();

    }



    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("app.random-number", () -> "http://localhost:8089/random");
        registry.add("app.kafka.topics.payment-events", () -> "payment-events");
    }

    @Test
    void createPaymentTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(userJwt).authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "orderId": 1,
              "paymentAmount": 150.0
            }
            """))
                .andDo(print())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    void findByIdTest() throws Exception {

        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "orderId": 2,
                      "paymentAmount": 100
                    }
                    """))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");


        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/" + id)
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void findPaymentsTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments")
                        .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void userSummaryTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/users/1/summary")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void globalSummaryTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/summary")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
