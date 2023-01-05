# Maven telegram observer

This is a simple service that observes a maven repository and notifies a telegram channel when a new version of a
dependency is available.

[Link to telegram bot](https://t.me/MavenDepsObserver_bot)

## How to use

- Open bot in telegram and click start button
- Type the library maven coordinates in the format `groupId:artifactId`.
  Example: `org.springframework.boot:spring-boot-starter-web`

## How to run

The app require the postgres database. Run the postgres database and provide the following environment variables:

* DB_URL - the url to data, include `sslmode=require` if your install of database needs it.
* DB_USER - the username. The user must have read access to all database
* DB_PASSWORD - the password

### Docker

The repository has DockerFile, the file create fully ready docker image.

### Run as java app

Clone project and install java.

Build jar file `gradle shadowJar`

Copy jar file to root `cp build/libs/*.jar ktor-docker-sample.jar`

Start a java app `java -jar src/build/libs/*.jar`

## Application settings

You can modify the application settings in the `application.conf` file.  

