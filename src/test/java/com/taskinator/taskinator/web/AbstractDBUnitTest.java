package com.taskinator.taskinator.web;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

@DBRider
@DBUnit(schema = "PUBLIC")
public abstract class AbstractDBUnitTest extends AbstractIntegrationTest {

    @Autowired
    protected DataSource dataSource;
}