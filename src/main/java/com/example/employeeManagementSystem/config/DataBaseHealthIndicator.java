package com.example.employeeManagementSystem.config;
import org.springframework.core.env.Environment;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;

@Component
public class DataBaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final Environment environment;

    //Constructor Injection
    public DataBaseHealthIndicator(DataSource dataSource, Environment environment){
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Override //http://localhost:8080/actuator/health
    public Health health(){

        boolean isTestProfile = Arrays.asList(environment.getActiveProfiles()).contains("test");

        if (isTestProfile) {
            // Simulated DOWN for test profile
            return Health.down()
                    .withDetail("profile", "test")
                    .withDetail("status", "Simulated DOWN for testing")
                    .build();
        }

        // Normal DB check for all other profiles
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
