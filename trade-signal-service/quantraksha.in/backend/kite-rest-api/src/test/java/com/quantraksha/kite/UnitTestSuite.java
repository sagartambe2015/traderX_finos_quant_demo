package com.quantraksha.kite;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Every unit test (*Test): no Spring context, no database, no network. Maven runs the same set
 * with {@code mvn test} (Surefire); this suite is for running them together from the IDE.
 */
@Suite
@SuiteDisplayName("Unit tests")
@SelectPackages("com.quantraksha.kite")
@IncludeClassNamePatterns(".*Test")
public class UnitTestSuite {
}
