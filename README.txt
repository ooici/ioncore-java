==================================================
ION-CORE - OOI CI Integrated Observatory Network Core Libraries (Java)
==================================================

January 2011

This project contains the Java core libraries for the OOI CI Integrated
Observatory Network (ION). This library provides the core classes for
interfacing with the common OOI message Exchange, for interacting with
OOI services and OOI data objects.

See:
http://www.oceanobservatories.org/spaces/display/CIDev/IONCore+Java

This library follows the architecture and design defined by the LCAarch project.
This python based system with its core libraries represents the ION reference
implementation. For more information, please see:
http://www.oceanobservatories.org/spaces/display/CIDev/LCAARCH+Development+Project

Setting Up Required Dependencies
================================
ION Java has two dependencies.

- It depends on the Google Protocol Buffer Jar file, which must be included
in this project's CLASSPATH.
- It depends on the net.Init class from the ion-object-definitions repo and
hence the ion_proto_x.y.jar file found in the java/dist directory of that
repo.

> If you haven't done so already, pull the ion-object-definitions proto, follow
the README.txt to build the Java classes for the .proto files.
> As part of the step above, you will have pulled down the Google Protocol Buffers
zip and built the protobuf-java-x.y.z.jar file.  Via your IDE, include this jar
file in the ION Java project's CLASSPATH dependencies.
> Via your IDE, include the distribution jar file ion-object-definitions/java/dist
jar file (ion_proto_x.y.jar) as a CLASSPATH dependency.   

Building
========

Run
> ant clean
> ant dist


Examples
========

Make sure LCAarch services are running.
Run
> ant runex


Usage
=====

Add the ioncore-<version>.jar library to the classpath. See the examples