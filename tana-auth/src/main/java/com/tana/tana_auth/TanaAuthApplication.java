package com.tana.tana_auth;

import com.tana.tana_auth.utils.ExcelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@SpringBootApplication(scanBasePackages = {"com.tana.tana_auth", "com.tana.tana_common"})
@EntityScan(basePackages = {"com.tana.tana_common.model"}) // scan your entity package
@EnableJpaRepositories(basePackages = {
        "com.tana.tana_common",
        "com.tana.tana_auth"
})
@EnableCaching
@EnableAsync
public class TanaAuthApplication {

    @Value(value = "${app.excel-path}")
    private String excelPath;

    @Autowired
    private ExcelParser excelParser;

    public static void main(String[] args) {
        SpringApplication.run(TanaAuthApplication.class, args);
    }


    /**
     * This bean runs once after the application context is loaded.
     * It fetches XLSX metadata and saves it to the database.
     */
//    @Bean
//    public CommandLineRunner run() {
//        return args -> {
//            System.out.println("path : " + excelPath);
//
//            File file = new File(excelPath);
//            if (!file.exists()) {
//                System.err.println("XLSX file not found at path: " + excelPath);
//                return;
//            }
//
//            excelParser.parse(excelPath);
//        };
//    }
}
