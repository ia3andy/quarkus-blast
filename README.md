# Quarkus Blast

This project is built with [Quarkus](quarkus.io), the Supersonic Subatomic Java Framework, [Quarkus Renarde]([url](https://github.com/quarkiverse/quarkus-renarde)), the [Quarkus Web Bundler]([url](https://github.com/quarkiverse/quarkus-web-bundler)) and [htmx](https://htmx.org/). It is a fun and animated board game to push to boundaries of htmx and server side rendering to its limit.


## Install the Quarkus CLI

https://quarkus.io/guides/cli-tooling

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
quarkus dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
quarkus build
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.
