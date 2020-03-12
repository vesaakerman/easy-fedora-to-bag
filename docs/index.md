easy-fedora2vault
==============
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-fedora2vault.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-fedora2vault)

Index a bag store

SYNOPSIS
--------

    <TODO>

DESCRIPTION
-----------
Tool for exporting datasets from Fedora and constructing AIP-bags to be stored in the bag stores

ARGUMENTS
---------

    <TODO>

EXAMPLES
--------

    <TODO>


INSTALLATION AND CONFIGURATION
------------------------------
Currently this project is build only as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/easy-fedora2vault` and the configuration files to `/etc/opt/dans.knaw.nl/easy-fedora2vault`.

BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

        git clone https://github.com/DANS-KNAW/easy-fedora2vault.git
        cd easy-fedora2vault
        mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.
