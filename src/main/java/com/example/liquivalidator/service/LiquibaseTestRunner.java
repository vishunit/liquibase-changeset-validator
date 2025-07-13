package com.example.liquivalidator.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.RollbackImpossibleException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

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

            System.out.println("🔍 Applying ChangeSet for validation: " + changeSetId);
            liquibase.update(new Contexts(), new LabelExpression());

            if (rollbackEnabled) {
                System.out.println("🌀 Rolling back ChangeSet: " + changeSetId);
                liquibase.rollback(1, (String) null);
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