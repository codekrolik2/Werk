package org.werk.engine.sql.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.pillar.log4j.Log4JUtils;
import org.pillar.db.jdbc.JDBCBatchStatementsTool;

public class SchemaCreatorWerk extends JDBCBatchStatementsTool {
	public static void createSchema(Connection connection, String dbName) throws SQLException {
		String creationScript = "-- MySQL Workbench Forward Engineering\n" + 
				"\n" + 
				"SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;\n" + 
				"SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;\n" + 
				"SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Schema werk_db\n" + 
				"-- -----------------------------------------------------\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Schema werk_db\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE SCHEMA IF NOT EXISTS `werk_db` DEFAULT CHARACTER SET utf8 ;\n" + 
				"USE `werk_db` ;\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Table `werk_db`.`servers`\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE TABLE IF NOT EXISTS `werk_db`.`servers` (\n" + 
				"  `id_server` BIGINT NOT NULL AUTO_INCREMENT,\n" + 
				"  `registration_time` BIGINT NOT NULL,\n" + 
				"  `last_heartbeat_time` BIGINT NOT NULL,\n" + 
				"  `heartbeat_period` BIGINT NOT NULL,\n" + 
				"  `server_info` VARCHAR(1024) NULL,\n" + 
				"  PRIMARY KEY (`id_server`))\n" + 
				"ENGINE = InnoDB;\n" + 
				"\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Table `werk_db`.`steps`\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE TABLE IF NOT EXISTS `werk_db`.`steps` (\n" + 
				"  `id_step` BIGINT NOT NULL AUTO_INCREMENT,\n" + 
				"  `id_job` BIGINT NOT NULL,\n" + 
				"  `step_type` VARCHAR(255) NOT NULL,\n" + 
				"  `is_rollback` TINYINT(1) NOT NULL,\n" + 
				"  `step_number` INT NOT NULL,\n" + 
				"  `execution_count` INT NOT NULL,\n" + 
				"  `step_parameter_state` TEXT NULL,\n" + 
				"  `step_processing_log` TEXT NULL,\n" + 
				"  PRIMARY KEY (`id_step`),\n" + 
				"  INDEX `fk_step_to_job_idx` (`id_job` ASC),\n" + 
				"  CONSTRAINT `fk_step_to_job`\n" + 
				"    FOREIGN KEY (`id_job`)\n" + 
				"    REFERENCES `werk_db`.`jobs` (`id_job`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION)\n" + 
				"ENGINE = InnoDB;\n" + 
				"\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Table `werk_db`.`jobs`\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE TABLE IF NOT EXISTS `werk_db`.`jobs` (\n" + 
				"  `id_job` BIGINT NOT NULL AUTO_INCREMENT,\n" + 
				"  `job_type` VARCHAR(255) NOT NULL,\n" + 
				"  `version` BIGINT NOT NULL,\n" + 
				"  `job_name` VARCHAR(255) NULL,\n" + 
				"  `parent_job_id` BIGINT NULL,\n" + 
				"  `current_step_id` BIGINT NULL,\n" + 
				"  `status` INT NOT NULL,\n" + 
				"  `next_execution_time` BIGINT NOT NULL,\n" + 
				"  `job_parameter_state` TEXT NOT NULL,\n" + 
				"  `job_initial_parameter_state` TEXT NOT NULL,\n" + 
				"  `wait_for_N_jobs` INT NULL,\n" + 
				"  `join_parameter_name` VARCHAR(255) NULL,\n" + 
				"  `id_locker` BIGINT NULL,\n" + 
				"  `step_count` INT NOT NULL,\n" + 
				"  PRIMARY KEY (`id_job`),\n" + 
				"  INDEX `fk_job_to_parent_idx` (`parent_job_id` ASC),\n" + 
				"  INDEX `fk_jobs_to_current_step_idx` (`current_step_id` ASC),\n" + 
				"  INDEX `fk_jobs_to_servers_idx` (`id_locker` ASC),\n" + 
				"  CONSTRAINT `fk_job_to_parent`\n" + 
				"    FOREIGN KEY (`parent_job_id`)\n" + 
				"    REFERENCES `werk_db`.`jobs` (`id_job`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION,\n" + 
				"  CONSTRAINT `fk_jobs_to_current_step`\n" + 
				"    FOREIGN KEY (`current_step_id`)\n" + 
				"    REFERENCES `werk_db`.`steps` (`id_step`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION,\n" + 
				"  CONSTRAINT `fk_jobs_to_servers`\n" + 
				"    FOREIGN KEY (`id_locker`)\n" + 
				"    REFERENCES `werk_db`.`servers` (`id_server`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION)\n" + 
				"ENGINE = InnoDB;\n" + 
				"\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Table `werk_db`.`join_record_jobs`\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE TABLE IF NOT EXISTS `werk_db`.`join_record_jobs` (\n" + 
				"  `id_awaiting_job` BIGINT NOT NULL,\n" + 
				"  `id_job` BIGINT NOT NULL,\n" + 
				"  PRIMARY KEY (`id_awaiting_job`, `id_job`),\n" + 
				"  INDEX `fk_id_job_idx` (`id_job` ASC),\n" + 
				"  CONSTRAINT `fk_id_awaiting_job`\n" + 
				"    FOREIGN KEY (`id_awaiting_job`)\n" + 
				"    REFERENCES `werk_db`.`jobs` (`id_job`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION,\n" + 
				"  CONSTRAINT `fk_id_job`\n" + 
				"    FOREIGN KEY (`id_job`)\n" + 
				"    REFERENCES `werk_db`.`jobs` (`id_job`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION)\n" + 
				"ENGINE = InnoDB;\n" + 
				"\n" + 
				"\n" + 
				"-- -----------------------------------------------------\n" + 
				"-- Table `werk_db`.`step_rollback`\n" + 
				"-- -----------------------------------------------------\n" + 
				"CREATE TABLE IF NOT EXISTS `werk_db`.`step_rollback` (\n" + 
				"  `id_rollback_step` BIGINT NOT NULL,\n" + 
				"  `id_step_being_rolled_back` BIGINT NOT NULL,\n" + 
				"  PRIMARY KEY (`id_rollback_step`, `id_step_being_rolled_back`),\n" + 
				"  INDEX `fk_to_step_to_rollback_idx` (`id_step_being_rolled_back` ASC),\n" + 
				"  CONSTRAINT `fk_to_step`\n" + 
				"    FOREIGN KEY (`id_rollback_step`)\n" + 
				"    REFERENCES `werk_db`.`steps` (`id_step`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION,\n" + 
				"  CONSTRAINT `fk_to_step_to_rollback`\n" + 
				"    FOREIGN KEY (`id_step_being_rolled_back`)\n" + 
				"    REFERENCES `werk_db`.`steps` (`id_step`)\n" + 
				"    ON DELETE NO ACTION\n" + 
				"    ON UPDATE NO ACTION)\n" + 
				"ENGINE = InnoDB;\n" + 
				"\n" + 
				"\n" + 
				"SET SQL_MODE=@OLD_SQL_MODE;\n" + 
				"SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;\n" + 
				"SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;\n" + 
				"";
		
		creationScript = creationScript.replaceAll("werk_db", dbName);
		
		Iterator<String> statementsIter = getStatementsIterator(creationScript);
		executeStatementsBatch(connection, statementsIter);
	}
	
	public static void main(String[] args) throws SQLException {
		Log4JUtils.debugInitLog4j();
		
		if (args.length < 3) {
			System.out.println("PARAMETERS:");
			System.out.println("<host> <user> <password> <dbname>");
		} else {
			String host = args[0];
			String user = args[1];
			String password = args[2];
			
			String dbname = "werk_db";
			if (args.length == 4) {
				dbname = args[3];
			}
			
			String url = JDBCTransactionFactory.createMySQLUrl(host);
			generateDummyDB(url, user, password, dbname);
			System.out.println("Werk DB created: server " + host + "; DB Name " + dbname);
		}
	}
	
	public static void generateDummyDB(String url, String user, String password, String dbname) throws SQLException {
		final Connection connection = DriverManager.getConnection(url, user, password);
		connection.setTransactionIsolation(JDBCTransactionFactory.transactionIsolationLevel);
		connection.setAutoCommit(false);
    	
		SchemaCreatorWerk.createSchema(connection, dbname);
    	
    	connection.commit();
    	connection.close();
	}

	public static void deleteDummyDB(String url, String user, String password, String dbname) throws SQLException {
		final Connection connection = DriverManager.getConnection(url, user, password);
		connection.setTransactionIsolation(JDBCTransactionFactory.transactionIsolationLevel);
		connection.setAutoCommit(false);

		SchemaCreatorWerk.deleteSchema(connection, dbname);
    	
    	connection.commit();
    	connection.close();
	}
}