package com.innowise.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.paymentservice.PaymentServiceApplication;
import com.innowise.paymentservice.config.TestSecurityConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.awaitility.Awaitility.await;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(classes = {
        PaymentServiceApplication.class,
        TestSecurityConfiguration.class
})
class PaymentIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7")
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

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
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {

        mongoTemplate.getCollection("payments").deleteMany(new org.bson.Document());

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        stubFor(get("/random")
                .willReturn(ok("2")));

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
        registry.add("app.async.enabled", () -> "false");
        registry.add("spring.kafka.listener.auto-startup", () -> "false");
    }

    @Test
    @Order(1)
    void createPaymentTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                        .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER"))
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
    @Order(2)
    void findByIdTest() throws Exception {

        MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "orderId": 2,
                      "paymentAmount": 100
                    }
                    """))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(postResult.getResponse().getContentAsString(), "$.id");


        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/" + id)
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    @Order(3)
    void findPaymentsTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                        .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "orderId": 1,
              "paymentAmount": 150.0
            }
            """))
                .andExpect(MockMvcResultMatchers.status().isAccepted()).andReturn();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments")
                        .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    @Order(4)
    void userSummaryTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
              "orderId": 1,
              "paymentAmount": 100.0
            }
            """))
                .andExpect(MockMvcResultMatchers.status().isAccepted());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/users/1/summary")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .param("from", "2020-01-01T00:00:00+03:00")
                        .param("to", "2026-12-31T23:59:59+03:00"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(5)
    void globalSummaryTest() throws Exception {

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payments")
                .with(jwt().jwt(userJwt).authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
              "orderId": 1,
              "paymentAmount": 100.0
            }
            """))
                .andExpect(MockMvcResultMatchers.status().isAccepted()).andReturn();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/payments/summary")
                        .with(jwt().jwt(adminJwt).authorities(() -> "ROLE_ADMIN"))
                        .param("from", "2020-01-01T00:00:00+03:00")
                        .param("to", "2026-12-31T23:59:59+03:00"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

    }
}
