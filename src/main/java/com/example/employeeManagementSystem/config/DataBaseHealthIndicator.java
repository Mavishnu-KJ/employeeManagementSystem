package com.example.employeeManagementSystem.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Component
public class DataBaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    //Constructor Injection
    public DataBaseHealthIndicator(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override //http://localhost:8080/actuator/health
    public Health health(){

        try(Connection connection = dataSource.getConnection()){
            if(connection.isValid(1)){ // 1 second timeout
                DatabaseMetaData dataBaseMetaData = connection.getMetaData();

                return Health.up()
                        .withDetail("Database", "Connected Successfully")
                        .withDetail("DataBaseName", dataBaseMetaData.getDatabaseProductName())
                        .withDetail("DataBaseVersion", dataBaseMetaData.getDatabaseProductVersion())
                        .withDetail("Driver", dataBaseMetaData.getDriverName())
                        .withDetail("DriverVersion", dataBaseMetaData.getDriverVersion())
                        .build();
            }else{
                return Health.down()
                        .withDetail("Reason","Connection is not valid")
                        .build();
            }

        }catch (SQLException e){
            return Health.down()
                    .withDetail("error", "Failed to connect "+e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
