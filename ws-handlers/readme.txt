This is a Java library with several JAX-WS SOAP handlers.
More handlers can be added.

Instructions using Maven:
------------------------

To compile package:
  mvn package
  (a JAR file is created containing the compiled classes and a generated manifest)

To install package:
  mvn install
  (the JAR is made available in the local maven repository ~/.m2/repository)
  (other projects can now refer to the library as a dependency)

