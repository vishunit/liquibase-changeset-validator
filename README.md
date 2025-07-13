# Liquibase ChangeSet Validator

A Spring Boot utility to **validate and test Liquibase changesets locally** before committing to production environments.

This tool helps prevent issues caused by directly running Liquibase on production branches (like `master`) without proper testing.  
It supports **rollback verification** and runs against any supported DB — currently configured for **Oracle**.


## 🚀 Features

- ✅ Run and validate a specific changeset from your changelog file
- 🔁 Rollback the last executed changeset for safety testing
- 🧪 Ideal for testing migrations locally before merge/push
- 📦 Simple Spring Boot CLI runner

## 🛠 Setup

### 🔧 Requirements

- Java 17 or higher
- Maven 3.x
- Oracle DB (default: `localhost:1521/xe`)
- [Oracle JDBC Driver](https://www.oracle.com/database/technologies/appdev/jdbc.html) (`ojdbc11`) must be installed locally if not available in a public Maven repo

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=your_oracle_user
spring.datasource.password=your_oracle_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.liquibase.enabled=false

liquibase.changelog=db/changelog/master.xml
liquibase.changeset.id=create_user_table
liquibase.rollback.enabled=true

## ▶️ Usage

### Run from IDE or CLI:

./mvnw spring-boot:run

Or build and run:

mvn clean package
java -jar target/liquibase-changeset-validator-0.0.1-SNAPSHOT.jar

## 🧪 What It Does

* Applies the changeset `create_user_table` from `master.xml`
* Logs execution success or failure
* If rollback is enabled, it will attempt to roll back the last executed changeset
* Useful for CI, pre-merge hooks, or local validation


## 📁 Example Changelog

<changeSet id="create_user_table" author="example">
    <createTable tableName="users">
        <column name="id" type="NUMBER" />
        <column name="name" type="VARCHAR2(100)" />
    </createTable>
    <rollback>
        <dropTable tableName="users"/>
    </rollback>
</changeSet>

## 📌 Notes

* `rollback(1)` is used internally to revert the last executed changeset
* Liquibase `rollback` by `changeSetId` **does not exist**; it's either rollback by tag or step count
* This tool helps reduce manual DB fixes due to untested migrations


## ✅ Planned Improvements

* CLI argument support for changeset ID / changelog path
* Docker Compose setup with Oracle XE for easier local testing
* Git pre-commit integration


## 📄 License

MIT – Use it freely in your teams or CI/CD pipelines.
