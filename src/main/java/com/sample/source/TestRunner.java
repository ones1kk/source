package com.sample.source;

import com.sample.source.property.LabelService;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestRunner implements ApplicationRunner {

    private final DataSource dataSource;

    private final LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Connection connection = dataSource.getConnection();
        log.info("url : {} ", connection.getMetaData().getURL());
        log.info("userName : {} ", connection.getMetaData().getUserName());
        log.info("productName : {} ", connection.getMetaData().getDatabaseProductName());
        log.info("labels {} ", labelService.findAllLabels());

    }
}
