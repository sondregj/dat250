# Experiment Assignment 7: Docker

## 1. Using a dockerized application: PostgreSQL

Flags for `docker run`:

- The `-p` flag is shorthand for `--publish`, which publishes a container's port to the host machine. The format is `hostPort:containerPort`.
- The `-e` flag is shorthand for `--env`, which sets environment variables in the container.

I first started a PostgreSQL container with the following command:

```sh
docker run -p 5432:5432 \
 -e POSTGRES_PASSWORD=mysecretpassword \
 -d --name dat250-postgres --rm postgres
```

It is displayed as running by `docker ps`.

```sh
$ docker ps
CONTAINER ID   IMAGE      COMMAND                  CREATED         STATUS         PORTS                                       NAMES
a3702059f8f7   postgres   "docker-entrypoint.s…"   4 seconds ago   Up 3 seconds   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp   dat250-postgres
```

Displaying the logs:

```sh
$ docker logs dat250-postgres
# truncated
2024-10-13 11:21:29.706 UTC [1] LOG:  database system is ready to accept connections
```

I conncected to the server with [TablePlus](https://tableplus.com) and created and user for the JPA application with the needed permissions.

```sql
CREATE USER jpa_client WITH PASSWORD 'secret';

GRANT ALL ON SCHEMA public TO jpa_client;
GRANT ALL ON ALL TABLES IN SCHEMA public TO jpa_client;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO jpa_client;
```

I also updated the `persistence.xml`-file with the new credentials.

```xml
<!-- PostgreSQL driver -->
<property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
<property name="hibernate.connection.driver_class" value="org.postgresql.Driver"/>
<property name="hibernate.connection.url" value="jdbc:postgresql://127.0.0.1:5432/postgres"/>
<property name="hibernate.connection.username" value="jpa_client"/>
<property name="hibernate.connection.password" value="secret"/>

<!-- Automatic schema generation -->
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

The tests pass with automatic schema generation enabled.

```sh
$ ./gradlew test

BUILD SUCCESSFUL in 386ms
4 actionable tasks: 4 up-to-date
```

The code is in this repository: [github.com/sondregj/dat250-jpa-tutorial](https://github.com/sondregj/dat250-jpa-tutorial)

## 2. Building your own dockerized application

> I had already done most of this as part of Experiment Assignment 3

I set up a Dockerfile for the Polls app, that builds and serves the backend. The app runs as a non-root user.

I opted to build the JAR inside the Dockerfile in a build stage instead of copying the JAR from the host machine to make the entire build process self-contained.

See the [Dockerfile](../../Dockerfile).

The image is also built in CI, see the [workflow](../../.github/workflows/ci.yml).
