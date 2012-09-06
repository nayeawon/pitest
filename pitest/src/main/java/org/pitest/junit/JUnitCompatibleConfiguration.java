/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.junit;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pitest.CompoundTestSuiteFinder;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestClassIdentifier;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.Option;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;

public class JUnitCompatibleConfiguration implements Configuration {

  private static final Pattern VERSION_PATTERN = Pattern
                                                   .compile("(\\d+)\\.(\\d+).*");

  public TestUnitFinder testUnitFinder() {
    return new CompoundTestUnitFinder(Arrays.asList(
        new JUnitCustomRunnerTestUnitFinder(),
        new ParameterisedJUnitTestFinder()));
  }

  public TestSuiteFinder testSuiteFinder() {
    return new CompoundTestSuiteFinder(Arrays.<TestSuiteFinder> asList(
        new JUnit4SuiteFinder(), new RunnerSuiteFinder()));
  }

  public TestClassIdentifier testClassIdentifier() {
    return new JUnitTestClassIdentifier();
  }

  public Option<PitHelpError> verifyEnvironment() {
    try {
      final String version = junit.runner.Version.id();
      if (isInvalidVersion(version)) {
        return Option.some(new PitHelpError(Help.WRONG_JUNIT_VERSION, version));
      }
    } catch (final NoClassDefFoundError er) {
      return Option.some(new PitHelpError(Help.NO_JUNIT));
    }

    return Option.none();
  }

  boolean isInvalidVersion(final String version) {
    final Matcher matcher = VERSION_PATTERN.matcher(version);

    if (!matcher.matches()) {
      return true;
    }

    final int major = Integer.parseInt(matcher.group(1));
    final int minor = Integer.parseInt(matcher.group(2));

    return (major < 4) || ((major == 4) && (minor < 6));
  }

}
