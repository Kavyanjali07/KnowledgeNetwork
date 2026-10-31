package com.knowledgenetwork.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class RuntimeProfileValidator implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

    private static final Logger log = LoggerFactory.getLogger(RuntimeProfileValidator.class);

    /**
     * Permitted runtime deployment profiles. Test profiles are strictly excluded.
     */
    public static final Set<String> ALLOWED_PROFILES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("local", "dev", "prod")
    ));

    /**
     * Recognized test profiles allowed exclusively during test suite execution.
     */
    public static final Set<String> TEST_PROFILES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("test", "test-pg")
    ));

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.environment == null) {
            throw new IllegalStateException("Environment cannot be null during profile validation");
        }
        String[] activeProfiles = this.environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            throw new IllegalStateException(
                    "No active Spring profile specified. An explicit runtime profile ('local', 'dev', or 'prod') " +
                    "is required to start the KnowledgeNetwork application."
            );
        }

        if (isTestEnvironment()) {
            for (String profile : activeProfiles) {
                if (!ALLOWED_PROFILES.contains(profile) && !TEST_PROFILES.contains(profile)) {
                    throw new IllegalStateException(
                            "Unsupported Spring profile '" + profile + "'. Allowed test profiles: " +
                            TEST_PROFILES + "; allowed runtime profiles: " + ALLOWED_PROFILES
                    );
                }
            }
            log.info("Test execution verified with Spring profiles: {}", Arrays.toString(activeProfiles));
        } else {
            validateProfiles(this.environment);
        }
    }

    /**
     * Validates that all active profiles in the given environment strictly belong to the runtime allowlist.
     * Rejects test profiles (e.g. test, test-pg) to prevent test configurations or keys in deployment runtimes.
     */
    public static void validateProfiles(Environment env) {
        if (env == null) {
            throw new IllegalStateException("Environment cannot be null during profile validation");
        }
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            throw new IllegalStateException(
                    "No active Spring profile specified. An explicit runtime profile ('local', 'dev', or 'prod') " +
                    "is required to start the KnowledgeNetwork application."
            );
        }

        for (String profile : activeProfiles) {
            if (!ALLOWED_PROFILES.contains(profile)) {
                throw new IllegalStateException(
                        "Unsupported Spring profile '" + profile + "'. Allowed runtime profiles are: " + ALLOWED_PROFILES
                );
            }
        }

        log.info("Active runtime Spring profiles verified: {}", Arrays.toString(activeProfiles));
    }

    /**
     * Determines whether execution is taking place within an authentic test runtime.
     * Checks for the presence of the JUnit Jupiter test framework on the classpath,
     * which is scoped to test-only and excluded from the production package.
     */
    public static boolean isTestEnvironment() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
