package com.knowledgenetwork.config;

import com.knowledgenetwork.security.JwtTokenProvider;
import com.knowledgenetwork.KnowledgeNetworkApplication;
import org.springframework.mock.env.MockEnvironment;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureConfigurationTest {

    private static final String VALID_TEST_SECRET = "01234567890123456789012345678901"; // 32 bytes

    @Nested
    @DisplayName("JwtTokenProvider Key Strength and Validation")
    class JwtTokenProviderValidationTests {

        @Test
        @DisplayName("Rejects null JWT secret")
        void rejectsNullSecret() {
            assertThatThrownBy(() -> new JwtTokenProvider(null, 900000L, "test-issuer"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("Rejects blank/whitespace JWT secret")
        void rejectsBlankSecret() {
            assertThatThrownBy(() -> new JwtTokenProvider("   ", 900000L, "test-issuer"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("Rejects undersized JWT secret (< 32 bytes)")
        void rejectsUndersizedSecret() {
            assertThatThrownBy(() -> new JwtTokenProvider("too-short-secret-key-16b", 900000L, "test-issuer"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 32 bytes");
        }

        @Test
        @DisplayName("Accepts valid 32-byte (256-bit) secret")
        void acceptsValidSecret() {
            JwtTokenProvider provider = new JwtTokenProvider(VALID_TEST_SECRET, 900000L, "test-issuer");
            assertThat(provider).isNotNull();
            assertThat(provider.getAccessTokenExpirationMs()).isEqualTo(900000L);
        }
    }

    @Nested
    @DisplayName("Spring Profile Fail-Fast Configuration Tests")
    class ProfileFailFastTests {

        @Test
        @DisplayName("Prod profile fails startup when APP_JWT_SECRET is missing")
        void prodProfileFailsStartupWhenJwtSecretMissing() {
            assertThatThrownBy(() -> {
                new SpringApplicationBuilder(TestJwtConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .run();
            }).satisfies(ex -> assertThat(getRootMessage(ex)).containsAnyOf("APP_JWT_SECRET", "at least 32 bytes"));
        }

        @Test
        @DisplayName("Dev profile fails startup when APP_JWT_SECRET is missing")
        void devProfileFailsStartupWhenJwtSecretMissing() {
            assertThatThrownBy(() -> {
                new SpringApplicationBuilder(TestJwtConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("dev")
                        .run();
            }).satisfies(ex -> assertThat(getRootMessage(ex)).containsAnyOf("APP_JWT_SECRET", "at least 32 bytes"));
        }

        @Test
        @DisplayName("Local profile fails startup when APP_JWT_SECRET is missing")
        void localProfileFailsStartupWhenJwtSecretMissing() {
            assertThatThrownBy(() -> {
                new SpringApplicationBuilder(TestJwtConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("local")
                        .run();
            }).satisfies(ex -> assertThat(getRootMessage(ex)).containsAnyOf("APP_JWT_SECRET", "at least 32 bytes"));
        }

        @Test
        @DisplayName("Local profile succeeds when APP_JWT_SECRET is supplied via environment")
        void localProfileSucceedsWithSuppliedSecret() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestJwtConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("local")
                    .properties("APP_JWT_SECRET=" + VALID_TEST_SECRET)
                    .run()) {
                assertThat(ctx.isActive()).isTrue();
                assertThat(ctx.getEnvironment().getProperty("app.jwt.secret")).isEqualTo(VALID_TEST_SECRET);
            }
        }

        @Test
        @DisplayName("Dev profile fails startup when database password is missing")
        void devProfileFailsStartupWhenDbPasswordMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("dev")
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.password");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_PASSWORD"));
        }

        @Test
        @DisplayName("Dev profile fails placeholder resolution when SPRING_DATASOURCE_URL is missing")
        void devProfileFailsWhenDatasourceUrlMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("dev")
                        .properties(
                                "SPRING_DATASOURCE_USERNAME=testuser",
                                "SPRING_DATASOURCE_PASSWORD=testpass"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.url");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_URL"));
        }

        @Test
        @DisplayName("Dev profile fails placeholder resolution when SPRING_DATASOURCE_USERNAME is missing")
        void devProfileFailsWhenDatasourceUsernameMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("dev")
                        .properties(
                                "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/knowledgenetwork_dev",
                                "SPRING_DATASOURCE_PASSWORD=testpass"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.username");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_USERNAME"));
        }

        @Test
        @DisplayName("Dev profile fails placeholder resolution when SPRING_DATASOURCE_PASSWORD is missing independently")
        void devProfileFailsWhenDatasourcePasswordMissingIndependently() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("dev")
                        .properties(
                                "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/knowledgenetwork_dev",
                                "SPRING_DATASOURCE_USERNAME=testuser"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.password");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_PASSWORD"));
        }

        @Test
        @DisplayName("Dev profile resolves all database properties when variables are supplied")
        void devProfileResolvesAllDatabasePropertiesWhenSupplied() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("dev")
                    .properties(
                            "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/knowledgenetwork_dev",
                            "SPRING_DATASOURCE_USERNAME=dev_user",
                            "SPRING_DATASOURCE_PASSWORD=dev_pass"
                    )
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.url"))
                        .isEqualTo("jdbc:postgresql://localhost:5432/knowledgenetwork_dev");
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.username"))
                        .isEqualTo("dev_user");
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.password"))
                        .isEqualTo("dev_pass");
            }
        }

        @Test
        @DisplayName("Prod profile fails startup when database credentials are missing")
        void prodProfileFailsStartupWhenDbCredentialsMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.password");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_PASSWORD"));
        }

        @Test
        @DisplayName("Prod profile fails placeholder resolution when SPRING_DATASOURCE_URL is missing")
        void prodProfileFailsWhenDatasourceUrlMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .properties(
                                "SPRING_DATASOURCE_USERNAME=prod_user",
                                "SPRING_DATASOURCE_PASSWORD=prod_pass"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.url");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_URL"));
        }

        @Test
        @DisplayName("Prod profile fails placeholder resolution when SPRING_DATASOURCE_USERNAME is missing")
        void prodProfileFailsWhenDatasourceUsernameMissing() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .properties(
                                "SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/knowledgenetwork_prod",
                                "SPRING_DATASOURCE_PASSWORD=prod_pass"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.username");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_USERNAME"));
        }

        @Test
        @DisplayName("Prod profile fails placeholder resolution when SPRING_DATASOURCE_PASSWORD is missing independently")
        void prodProfileFailsWhenDatasourcePasswordMissingIndependently() {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .properties(
                                "SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/knowledgenetwork_prod",
                                "SPRING_DATASOURCE_USERNAME=prod_user"
                        )
                        .run()) {
                    ctx.getEnvironment().getProperty("spring.datasource.password");
                }
            }).satisfies(ex -> assertThat(getRootMessage(ex)).contains("SPRING_DATASOURCE_PASSWORD"));
        }

        @Test
        @DisplayName("Prod profile resolves all database properties when variables are supplied")
        void prodProfileResolvesAllDatabasePropertiesWhenSupplied() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties(
                            "SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/knowledgenetwork_prod",
                            "SPRING_DATASOURCE_USERNAME=prod_admin",
                            "SPRING_DATASOURCE_PASSWORD=prod_secret"
                    )
                    .run()) {
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.url"))
                        .isEqualTo("jdbc:postgresql://prod-db:5432/knowledgenetwork_prod");
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.username"))
                        .isEqualTo("prod_admin");
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.password"))
                        .isEqualTo("prod_secret");
            }
        }

        @Test
        @DisplayName("Prod profile does not inherit dev database password fallback")
        void prodProfileDoesNotInheritDevPasswordFallback() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .profiles("prod")
                    .properties("SPRING_DATASOURCE_PASSWORD=test_provided_password")
                    .run()) {
                String password = ctx.getEnvironment().getProperty("spring.datasource.password");
                assertThat(password).isEqualTo("test_provided_password");
                assertThat(password).isNotEqualTo("ci_test_postgres_password_2026");
            }
        }

        @Test
        @DisplayName("No active profile does not implicitly activate local or dev profiles")
        void noActiveProfileDoesNotActivateLocalOrDev() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestEmptyConfig.class)
                    .web(WebApplicationType.NONE)
                    .run()) {
                String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
                assertThat(activeProfiles).isEmpty();
                assertThat(activeProfiles).doesNotContain("local", "dev", "prod");

                // Verify base datasource properties are unconfigured (not falling back to H2 or Postgres)
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.url")).isNull();
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.username")).isNull();
                assertThat(ctx.getEnvironment().getProperty("spring.datasource.password")).isNull();
            }
        }

        @Test
        @DisplayName("No active profile fails startup when APP_JWT_SECRET is missing")
        void noActiveProfileFailsStartupWhenJwtSecretMissing() {
            assertThatThrownBy(() -> {
                new SpringApplicationBuilder(TestJwtConfig.class)
                        .web(WebApplicationType.NONE)
                        .run();
            }).satisfies(ex -> assertThat(getRootMessage(ex)).containsAnyOf("APP_JWT_SECRET", "at least 32 bytes"));
        }

        @Test
        @DisplayName("No active profile succeeds when APP_JWT_SECRET is supplied via environment")
        void noActiveProfileSucceedsWithSuppliedSecret() {
            try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestJwtConfig.class)
                    .web(WebApplicationType.NONE)
                    .properties("APP_JWT_SECRET=" + VALID_TEST_SECRET)
                    .run()) {
                assertThat(ctx.isActive()).isTrue();
                assertThat(ctx.getEnvironment().getProperty("app.jwt.secret")).isEqualTo(VALID_TEST_SECRET);
            }
        }
    }

    @Nested
    @DisplayName("Runtime Profile Validation Layer Tests")
    class RuntimeProfileValidationLayerTests {

        @Test
        @DisplayName("Full application startup with no profile is rejected even when JWT secret is supplied")
        void fullApplicationStartupWithNoProfileIsRejected() {
            assertThatThrownBy(() -> {
                new SpringApplicationBuilder(KnowledgeNetworkApplication.class)
                        .web(WebApplicationType.NONE)
                        .properties("APP_JWT_SECRET=" + VALID_TEST_SECRET)
                        .run();
            }).satisfies(ex -> assertThat(getRootMessage(ex))
                    .contains("No active Spring profile specified"));
        }

        @Test
        @DisplayName("Valid runtime profiles (local, dev, prod) are accepted at profile-validation layer")
        void validRuntimeProfilesAreAcceptedAtValidationLayer() {
            for (String profile : Arrays.asList("local", "dev", "prod")) {
                MockEnvironment env = new MockEnvironment();
                env.setActiveProfiles(profile);
                RuntimeProfileValidator.validateProfiles(env);
            }
        }

        @Test
        @DisplayName("Unsupported profiles are rejected at profile-validation layer")
        void unsupportedProfilesAreRejected() {
            for (String invalidProfile : Arrays.asList("staging", "qa", "foo", "unknown")) {
                MockEnvironment env = new MockEnvironment();
                env.setActiveProfiles(invalidProfile);
                assertThatThrownBy(() -> RuntimeProfileValidator.validateProfiles(env))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Unsupported Spring profile '" + invalidProfile + "'");
            }
        }

        @Test
        @DisplayName("Runtime validator rejects test and test-pg profiles as runtime deployment profiles")
        void runtimeValidatorRejectsTestAndTestPgProfiles() {
            for (String testProfile : Arrays.asList("test", "test-pg")) {
                MockEnvironment env = new MockEnvironment();
                env.setActiveProfiles(testProfile);
                assertThatThrownBy(() -> RuntimeProfileValidator.validateProfiles(env))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Unsupported Spring profile '" + testProfile + "'");
            }
        }

        @Test
        @DisplayName("Test environment detector correctly identifies test runtime")
        void testEnvironmentDetectorIdentifiesTestRuntime() {
            assertThat(RuntimeProfileValidator.isTestEnvironment()).isTrue();
        }

        @Test
        @DisplayName("Empty or null profile environment throws IllegalStateException at profile-validation layer")
        void emptyOrNullProfileThrowsIllegalStateException() {
            MockEnvironment emptyEnv = new MockEnvironment();
            assertThatThrownBy(() -> RuntimeProfileValidator.validateProfiles(emptyEnv))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No active Spring profile specified");

            assertThatThrownBy(() -> RuntimeProfileValidator.validateProfiles(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Environment cannot be null");
        }
    }

    private static String getRootMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable curr = t;
        while (curr != null) {
            sb.append(curr.getMessage()).append(" ");
            curr = curr.getCause();
        }
        return sb.toString();
    }

    @TestConfiguration
    @EnableConfigurationProperties
    static class TestJwtConfig {
        @Bean
        public JwtTokenProvider jwtTokenProvider(
                @Value("${app.jwt.secret}") String secret,
                @Value("${app.jwt.access-token-expiration-ms:900000}") long accessExp,
                @Value("${app.jwt.issuer:knowledgenetwork-api}") String issuer) {
            return new JwtTokenProvider(secret, accessExp, issuer);
        }
    }

    @TestConfiguration
    static class TestEmptyConfig {
    }
}
