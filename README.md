# JSpecify Package Report

## Running

```shell
./gradlew run --args="<path to spring boot project folder or module folder>"
```

Example which prints all packages in the whole Spring Boot project:

```shell
./gradlew run --args="/home/mhalbritter/Projects/spring-boot/main"
```

Example which prints all packages in the `core/spring-boot` module:

```shell
./gradlew run --args="/home/mhalbritter/Projects/spring-boot/main/core/spring-boot"
```

## Building

```shell
./gradlew build
```
