==================================================
ION-CORE - OOI CI Integrated Observatory Network Core Libraries (Java)
==================================================

August 2010

This project contains the Java core libraries for the OOI CI Integrated
Observatory Network (ION). This library provides a the core classes for
interfacing with the common OOI message Exchange, for interacting with
OOI services and OOI data objects.

This library follows the architecture and design defined by the LCAarch project.
This python based system with its core libraries represents the ION reference
implementation. For more information, please see:
http://www.oceanobservatories.org/spaces/display/CIDev/LCAARCH+Development+Project


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