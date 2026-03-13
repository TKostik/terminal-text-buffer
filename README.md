# terminal-text-buffer

Simple terminal buffer implementation in Java.

## Compile / Build

From repository root:

```bash
./gradlew :app:build
```

## Start the project

Run the CLI application from repository root:

```bash
./gradlew :app:run
```

## Run tests

```bash
./gradlew test
```

## JaCoCo coverage

Generate coverage report:

```bash
./gradlew :app:test :app:jacocoTestReport
```

Open HTML report:

```bash
open app/build/reports/jacoco/test/html/index.html
```