package com.knowledgenetwork.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductionReadinessTest {

    @Configuration
    static class EmptyConfig {
    }

    @Nested
    @DisplayName("Server Port Configuration Tests")
    class PortConfigurationTests {

        @Test
        @DisplayName("Defaults server.port to 8080 when neither PORT nor SERVER_PORT is set")
        void defaultsTo8080() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("server.port")).isEqualTo("8080");
            }
        }

        @Test
        @DisplayName("Uses SERVER_PORT when PORT is unset")
        void usesServerPortFallback() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties("SERVER_PORT=9090")
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("server.port")).isEqualTo("9090");
            }
        }

        @Test
        @DisplayName("Prioritizes Render PORT environment variable over SERVER_PORT")
        void prioritizesRenderPort() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties("PORT=10000", "SERVER_PORT=9090")
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("server.port")).isEqualTo("10000");
            }
        }
    }

    @Nested
    @DisplayName("Production CORS Property Resolution Tests")
    class ProdCorsPropertyTests {

        @Test
        @DisplayName("Prod profile defaults to empty allowed origins and disables local origins")
        void prodDefaultsToNoCrossOriginFallback() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("app.cors.include-local-origins")).isEqualTo("false");
                assertThat(ctx.getEnvironment().getProperty("app.cors.allowed-origins")).isEmpty();
                assertThat(ctx.getEnvironment().getProperty("app.cookie.secure")).isEqualTo("true");
                assertThat(ctx.getEnvironment().getProperty("management.health.mail.enabled")).isEqualTo("false");
            }
        }

        @Test
        @DisplayName("Prod profile uses RENDER_EXTERNAL_URL when APP_CORS_ALLOWED_ORIGINS is unset")
        void prodUsesRenderExternalUrlWhenAllowedOriginsUnset() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties("RENDER_EXTERNAL_URL=https://knowledgenetwork.onrender.com")
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("app.cors.allowed-origins"))
                        .isEqualTo("https://knowledgenetwork.onrender.com");
            }
        }

        @Test
        @DisplayName("Prod profile prefers explicit APP_CORS_ALLOWED_ORIGINS over RENDER_EXTERNAL_URL")
        void prodPrefersExplicitCorsAllowedOrigins() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties(
                            "RENDER_EXTERNAL_URL=https://knowledgenetwork.onrender.com",
                            "APP_CORS_ALLOWED_ORIGINS=https://knowledgenetwork.app"
                    )
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("app.cors.allowed-origins"))
                        .isEqualTo("https://knowledgenetwork.app");
            }
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Actuator Health and SPA Routing Security Tests")
    class HealthAndSpaRoutingTests {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Actuator health and probe endpoints are publicly accessible and return UP")
        void actuatorHealthEndpointsReturnUp() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));

            mockMvc.perform(get("/actuator/health/liveness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));

            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("SPA frontend routes forward to index.html without requiring prior authentication")
        void spaRoutesForwardWithoutAuthentication() throws Exception {
            for (String route : new String[]{
                    "/", "/login", "/register", "/verify-email", "/forgot-password", "/reset-password",
                    "/explore", "/explore/concepts", "/creator/alice", "/creator/alice.smith",
                    "/graphs", "/graphs/123", "/graphs/123/edit",
                    "/dashboard", "/settings", "/settings/audit-log"
            }) {
                mockMvc.perform(get(route))
                        .andExpect(status().isOk())
                        .andExpect(forwardedUrl("/index.html"));
            }

            // Ensure /index.html itself is never intercepted by SpaController (no infinite forward loop)
            mockMvc.perform(get("/index.html"))
                    .andExpect(forwardedUrl(null));
        }

        @Test
        @DisplayName("Protected API and non-public actuator endpoints remain 401 when unauthenticated")
        void protectedEndpointsRemainAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/workspaces")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/v1/graphs")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/v1/audit-logs")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "app.cors.include-local-origins=false",
            "app.cors.allowed-origins=",
            "server.forward-headers-strategy=framework"
    })
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Production Same-Origin Only CORS Tests")
    class ProdSameOriginCorsTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CorsConfigurationSource corsConfigurationSource;

        @Test
        @DisplayName("Rejects localhost cross-origin requests when local origins are disabled and allowed origins are empty")
        void rejectsLocalhostWhenNoAllowedOriginsConfigured() throws Exception {
            CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(
                    new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/public/networks")
            );
            assertThat(config).isNotNull();
            assertThat(config.getAllowedOriginPatterns()).isEmpty();

            mockMvc.perform(options("/api/v1/public/networks")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }

        @Test
        @DisplayName("Allows same-origin requests behind Render HTTPS reverse proxy")
        void allowsSameOriginRequestsBehindProxy() throws Exception {
            mockMvc.perform(get("/api/v1/public/networks")
                            .header("X-Forwarded-Proto", "https")
                            .header("X-Forwarded-Host", "knowledgenetwork.onrender.com")
                            .header("X-Forwarded-Port", "443")
                            .header(HttpHeaders.ORIGIN, "https://knowledgenetwork.onrender.com"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "app.cors.include-local-origins=false",
            "app.cors.allowed-origins=https://knowledgenetwork.onrender.com/"
    })
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Production Explicit Deployment Origin CORS Tests")
    class ProdExplicitOriginCorsTests {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Permits normalized deployment origin and rejects other origins")
        void permitsOnlyConfiguredDeploymentOrigin() throws Exception {
            mockMvc.perform(options("/api/v1/public/networks")
                            .header(HttpHeaders.ORIGIN, "https://knowledgenetwork.onrender.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://knowledgenetwork.onrender.com"));

            mockMvc.perform(options("/api/v1/public/networks")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

            mockMvc.perform(options("/api/v1/public/networks")
                            .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }
}
