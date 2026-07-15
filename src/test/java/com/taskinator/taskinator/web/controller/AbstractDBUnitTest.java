package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import com.taskinator.taskinator.web.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

@DBRider
@DBUnit(schema = "PUBLIC", tableType = {"TABLE"})
public abstract class AbstractDBUnitTest extends AbstractIntegrationTest {

    @Autowired
    protected DataSource dataSource;
}