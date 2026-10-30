package com.quantraksha.kite;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Every integration test (*IT): full Spring context on the "it" profile, H2 in place of Postgres,
 * Kite mocked. Maven runs the same set with {@code mvn verify} (Failsafe); this suite is for the IDE.
 */
@Suite
@SuiteDisplayName("Integration tests")
@SelectPackages("com.quantraksha.kite")
@IncludeClassNamePatterns(".*IT")
public class IntegrationTestSuite {
}
