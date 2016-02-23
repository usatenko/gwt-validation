# Test Plan #

The goal of this project is to provide a validation framework for web applications and validation is often a very critical procedure.  Keeping this in mind the project will place an emphasis on testing with a heavy reliance on automated unit tests.

## Automated Unit Tests ##

The JUnit framework and the GWT adapters to it will provide the unit testing framework.

## TCK Compatibility Testing ##

Each failing TCK test will have a matching JUnit test created so that it can be tested and validated in both the server (reflective) and generated (client) code.