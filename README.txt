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

Source
======
Obtain the eoi-agents project by running:
::
	git clone git@github.com:ooici/ioncore-java

NOTE: Unless otherwise noted - all commands should be run from the "eoi-agents" directory
::
	cd ioncore-java


Dependencies
============
Dependencies are managed using Apache Ivy.  If you haven't installed Ivy, please refer to the "Installing Ivy" section below.

To resolve (process and download) dependencies run:
::
	ant resolve


Compiling
=========
Compile the project by running:
::
	ant compile


Build.xml
========
All tasks are performed with ant, and are run with:
::
	ant <target>

Tasks can be viewed with the following command:
::
	ant -p

Main targets:

 clean            --> Clean the project
 clean-ivy-cache  --> Clean the ivy cache
 compile          --> Compile the project
 deep-clean       --> Cleans both this directory and the ivy cache
 dist             --> Package Distribution
 javadoc          --> Generate Javadoc
 report-deps      --> Generates a report of dependencies
 resolve          --> Retreive dependencies with ivy
 test-all         --> Runs all tests
 test-ncagent     --> Runs a canned netcdf test
 test-sos         --> Runs a canned sos test
 test-usgs        --> Runs a canned usgs test
Default target: dist


IDE Setup
=========
1. Run: ant resolve
2. Make a "existing source" (or similar) project in your IDE of choice and select the src directory of the project.
3. Put all of the jar files in the "lib" directory on the project classpath in your IDE.
** Note - when you "ant clean" the lib directory is wiped out.  If you discover your project not compiling, make sure
there's a lib directory!


**********************************
**********************************

Ivy Installation*
================
1. Download Apache Ivy (OOICI hosted) from: http://ooici.net/packages/ivy.jar

2. Copy/move the ivy.jar to the "lib" directory of your ant installation:
	Mac OSX: usually - /usr/share/ant/lib/
	Linux/Windows: wherever you have ant installed (check "%ANT_HOME%" if you're not sure)

3. To verify the installation run (from eoi-agents directory):
::
	ant resolve

* Full install instructions: http://ant.apache.org/ivy/history/2.2.0-rc1/install.html