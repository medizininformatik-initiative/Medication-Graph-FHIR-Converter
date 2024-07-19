This module contains utility classes for testing of the medgraph module and those that depend on it.
Because Kotlin Multiplatform as of now (July 19th, 2024) does not support dependencies on other projects'
test fixtures, the test fixtures are implemented in the *main* directory.

**Do not use this module for productive code. Do not reference it from productive code.**