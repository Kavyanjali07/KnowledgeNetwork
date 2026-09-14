package com.knowledgenetwork.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test-pg")
public abstract class AbstractPostgresIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    static {
        // Configure Podman socket for Testcontainers if Docker socket is not present
        File podmanSock = new File("/run/user/1000/podman/podman.sock");
        if (podmanSock.exists() && System.getenv("DOCKER_HOST") == null) {
            System.setProperty("tc.host", "unix:///run/user/1000/podman/podman.sock");
        }
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");

        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("knowledgenetwork_test")
                .withUsername("kn_test")
                .withPassword("kn_test_password");
        postgres.start();
    }
}
