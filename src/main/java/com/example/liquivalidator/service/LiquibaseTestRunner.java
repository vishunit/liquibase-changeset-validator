package com.example.liquivalidator.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Service
public class LiquibaseTestRunner implements CommandLineRunner {

    @Value("${liquibase.changelog}")
    private String changelog;

    @Value("${liquibase.changeset.id}")
    private String changeSetId;

    @Value("${liquibase.rollback.enabled:false}")
    private boolean rollbackEnabled;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));

            Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database);

            // Get all changesets from the changelog
            DatabaseChangeLog databaseChangeLog = liquibase.getDatabaseChangeLog();
            List<ChangeSet> changeSets = databaseChangeLog.getChangeSets();

            // Find the specific changeset we want to run
            ChangeSet targetChangeSet = changeSets.stream()
                    .filter(cs -> cs.getId().equals(changeSetId))
                    .findFirst()
                    .orElse(null);

            if (targetChangeSet == null) {
                System.err.println("❌ ChangeSet not found: " + changeSetId);
                return;
            }

            // Check if changeset was already executed
            if (database.getRanChangeSet(targetChangeSet) != null) {
                System.out.println("ℹ️ ChangeSet already executed: " + changeSetId);
                return;
            }

            System.out.println("🔍 Applying ChangeSet for validation: " + changeSetId);

            // Create a new changelog containing only our target changeset
            DatabaseChangeLog singleChangeLog = new DatabaseChangeLog(databaseChangeLog.getPhysicalFilePath());
            singleChangeLog.addChangeSet(targetChangeSet);

            // Create a new Liquibase instance with our single changeset
            Liquibase singleLiquibase = new Liquibase(singleChangeLog,
                    new ClassLoaderResourceAccessor(), database);

            // Execute ONLY our target changeset
            singleLiquibase.update(new Contexts(), new LabelExpression());

            if (rollbackEnabled) {
                System.out.println("🌀 Rolling back ChangeSet: " + changeSetId);
                // Rollback using the changeset's identification
                singleLiquibase.rollback(1, new Contexts(), new LabelExpression());
                System.out.println("✅ Rolled back successfully.");
            } else {
                System.out.println("✅ ChangeSet executed successfully: " + changeSetId);
            }

        } catch (RollbackImpossibleException e) {
            System.err.println("⚠️ Rollback not defined for this ChangeSet.");
        } catch (Exception e) {
            System.err.println("❌ Error validating ChangeSet: " + changeSetId);
            e.printStackTrace();
        }
    }
}