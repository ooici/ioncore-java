==================================================
ION-CORE - OOI CI Integrated Observatory Network Core Libraries (Java)
==================================================

February 2011

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

NOTE: Unless otherwise noted - all commands should be run from the "ioncore-java" directory
::
	cd ioncore-java


Dependencies
============
Dependencies are managed using Apache Ivy.  If you haven't installed Ivy, please refer to the "Installing Ivy" section below.


Ivy Installation*
================
1. Download Apache Ivy (OOICI hosted) from: http://ooici.net/packages/ivy.jar

2. Copy/move the ivy.jar to the "lib" directory of your ant installation:
        ac OSX: you can place the .jar in your user ant configuration -->  ~/.ant/lib/
                or in the root ant installation, usually --> /usr/share/ant/lib/
        Linux/Windows: wherever you have ant installed (check "%ANT_HOME%" if you're not sure)

3. To verify the installation run (from eoi-agents directory):
::
        ant resolve

* Full install instructions: http://ant.apache.org/ivy/history/2.2.0-rc1/install.html


**********************************
**********************************

Compiling
=========
To resolve (process and download) dependencies run:
::
	ant resolve

Compile the project by running:
::
	ant compile

Unit Test
=========
The TestApplicationIntegrationService junit test requires that a capability container be up and running
with pre-loaded data sets.  You can point the junit class to the capability container by defining
an ooici-conn.properties file in your home directory.  Example content is shown below:

ion.sysname=<sys name used when starting the CC>
ion.host=<host running RabbitMQ>
ion.username=guest
ion.password=guest

Once the CC is up and running and the ooici-conn.properties file has been defined, you can runn the junit
test cases with:
::
    ant test

To investigate junit failures, first look in the "report/*.xml file(s) for lines like the following:
    <failure message...>
    
The TestAppIntegrationService junit test attempts to dump out helpful failure messages received from the CC
into the JSONFormatExamples.txt file.  This file can be found at the root level in the ioncore-java repository.


Build.xml
========
All tasks are performed with ant, and are run with:
::
	ant <target>

Tasks can be viewed with the following command:
::
	ant -p

Main targets:

 clean                    --> Clean the project
 clean-ivy-cache          --> Clean the ivy cache
 clean-more               --> Called at the end of 'clean' - empty by default, override in build.xml to use
 clean-ooici-cache        --> Clean the ivy cache of the ooici dependencies only
 compile                  --> Compile the project
 deep-clean               --> Cleans both this directory and the ivy cache
 dist                     --> Package Distribution
 javadoc                  --> Generate Javadoc
 ooici_base.post-compile  --> Called after javac compilation - empty by default, override in build.xml to use
 post-dist                --> Called after all actions in the dist target - empty by default, override in build.xml to use
 pre-compile              --> Called before javac compilation - empty by default, override in build.xml to use
 pre-dist                 --> Called before all actions in the dist target - empty by default, override in build.xml to use
 report-deps              --> Generates a report of dependencies
 resolve                  --> Retreive dependencies with ivy
 runex                    --> Run Example
 test-all                 --> Runs JUnit tests for all classes ending in 'Test.java')
Default target: dist


IDE Setup
=========
1. Run: ant resolve
2. Make a "existing source" (or similar) project in your IDE of choice and select the src directory of the project.
3. Put all of the jar files in the "lib" directory on the project classpath in your IDE.
** Note - when you "ant clean" the lib directory is wiped out.  If you discover your project not compiling, make sure
there's a lib directory!

