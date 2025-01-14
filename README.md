# Special Tribunals Case API [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This API handles callbacks from CCD for the Criminal Injuries Compensation (CIC) (and other services, in the future e.g. Mental Health) case type for Special Tribunals (ST).

## Overview

                        ┌──────────────────┐
                        │                  │
                        │ SPTRIBS-CASE-API │
                        │                  │
                        └───────▲──────────┘
                                │
                                │
                        ┌───────▼────────┐
                        │                │
                  ┌─────►      CCD       ◄─────┐
                  │     │                │     │
                  │     └────────────────┘     │
                  │                            │
          ┌───────┴───────────┐        ┌───────┴───────┐
          │                   │        │               │
          │ SPTRIBS-FRONTEND  │        │       XUI     │
          │                   │        │               │
          └───────────────────┘        └───────────────┘

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project, execute the following command:

    ./gradlew build

### Running the application locally

You will need access to the sptribs-aat vault, and an active VPN to run locally, as it depends on services running on AAT.

Run the application by executing the following command:

    ./gradlew bootRun

This will start the API container exposing the application's port
(set to `4013` in this template app).

In order to test if the application is up, you can call its health endpoint:

    curl http://localhost:4013/health

You should get a response similar to this:

    {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}

### Running the application locally with CCD and XUI

If you would like to run the full CCD and XUI stack locally you can use:

    ./gradlew bootWithCcd

Then you can access XUI on `http://localhost:3000`

### Generate CCD JSON files

Generating the CCD JSON files will happen on every `./gradlew bootWithCcd` but you can manually trigger this with:

    ./gradlew generateCCDConfig

### Generate TypeScript definitions for CCD definition

    ./gradlew generateTypeScript

### Crons

You can manually run a cron task from the cli:

```
TASK_NAME=[task] java -jar sptribs-case-api.jar run

# E.g.
TASK_NAME=SystemProgressHeldCasesTask java -jar sptribs-case-api.jar

# or
TASK_NAME=SystemProgressHeldCasesTask ./gradlew bootRun
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

