package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OptimisticLockingIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void concurrentUpdatesOnSameEntity_ShouldTriggerOptimisticLockingFailure() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // Save initial workspace with version 0
        UUID workspaceId = tx.execute(status -> {
            User owner = userRepository.save(TestFixtures.createUser("opt_" + UUID.randomUUID() + "@example.com", "Opt", "User"));
            Workspace ws = workspaceRepository.save(TestFixtures.createWorkspace("Initial WS", owner));
            return ws.getId();
        });

        // Load entity in Tx 1
        Workspace wsTx1 = tx.execute(status -> workspaceRepository.findById(workspaceId).orElseThrow());

        // Load entity in Tx 2
        Workspace wsTx2 = tx.execute(status -> workspaceRepository.findById(workspaceId).orElseThrow());

        // Tx 1 updates and commits -> Version increments from 0 to 1
        tx.executeWithoutResult(status -> {
            wsTx1.setName("Updated by Tx 1");
            workspaceRepository.saveAndFlush(wsTx1);
        });

        // Tx 2 attempts to save stale entity (with version 0) -> Expected OptimisticLocking failure
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            tx.executeWithoutResult(status -> {
                wsTx2.setName("Updated by Tx 2");
                workspaceRepository.saveAndFlush(wsTx2);
            });
        });
    }
}
