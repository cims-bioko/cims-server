# CIMS Server

The server application providing demographic data and user management for the
CIMS. It is responsible for handling XML form submissions as they are uploaded
by tablet users to ODK Aggregate, creating and maintaining demographic
information, and providing a data synchronization servics used by cims-tablet to
assist with location-sensitive data entry. 

For more information, please see the [CIMS background
documentation](https://github.com/cims-bioko/cims-bioko.github.io/wiki/Background).

## Usage

The cims-server application is a web application written in Java. To use it, you
must deploy it to a Java servlet container, such as Apache Tomcat. For more
information, please read [the server setup section of the general CIMS
deployment
instructions](https://github.com/cims-bioko/cims-bioko.github.io/wiki/Deployment#server-setup).

