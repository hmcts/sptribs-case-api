# Special Tribunals Case API [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

`sptribs-case-api` is the backend for Special Tribunals (ST). It defines the Criminal Injuries Compensation (CIC) CCD case type and handles every CCD callback for it. The service is designed to host further tribunals as additional case types.

## Overview

- A Spring Boot service on port `4013`, built with Gradle.
- A **decentralised** CCD service. It owns the `CriminalInjuriesCompensation` case type in the `ST_CIC` jurisdiction, and CCD routes every callback for that case type here. Case data is stored in this service's own Postgres database.
- The CCD definition (events, states, fields, tabs, access) is written in Java with [ccd-config-generator](https://github.com/hmcts/ccd-config-generator) and generated at build time.
- Caseworkers, judges and legal representatives use Manage Cases (XUI). Citizens use [sptribs-frontend](https://github.com/hmcts/sptribs-frontend) and [sptribs-dss-update-case-web](https://github.com/hmcts/sptribs-dss-update-case-web).
- The service calls CDAM, GOV.UK Notify, document assembly, bundling and reference data. It publishes case events that Work Allocation consumes.

## Architecture

```
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
```

CCD data store calls this service for every `about-to-start`, `mid-event`, `about-to-submit` and `submitted` callback. See [docs/architecture.md](docs/architecture.md) for the full component map, integrations and data flow.

## Quick start

Prerequisites: Java 21, access to the `sptribs-aat` Key Vault, and an active VPN (the local stack depends on services running in AAT).

```sh
az login
az acr login --name hmctsprod
./gradlew loadLocalEnvSecrets   # writes the local env file
./gradlew bootWithCCD           # starts the API plus CCD and XUI in-process
```

Then open XUI at `http://localhost:3000`. See [docs/running-locally.md](docs/running-locally.md) for users, ports and troubleshooting.

## Documentation

Concepts:

- [Architecture](docs/architecture.md)
- [Case lifecycle](docs/case-lifecycle.md): states and transitions
- [Events reference](docs/events-reference.md): every event and state
- [Roles and access](docs/roles-and-access.md)
- [Work allocation](docs/work-allocation.md)
- [Notifications](docs/notifications.md)
- [Documents and bundles](docs/documents-and-bundles.md)
- [Hearings](docs/hearings.md)
- [Data and persistence](docs/data-and-persistence.md)
- [Configuration](docs/configuration.md)
- [Scheduled tasks](docs/scheduled-tasks.md)

How-to guides:

- [Adding an event](docs/adding-an-event.md)
- [Adding a notification](docs/adding-a-notification.md)
- [Adding a work allocation task](docs/adding-a-work-allocation-task.md)
- [Adding a case type](docs/adding-a-case-type.md): onboarding a new tribunal
- [Running locally](docs/running-locally.md)
- [Testing](docs/testing.md)
- [Release and deploy](docs/release-and-deploy.md)
- [Support runbook](docs/support-runbook.md)

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It contains the `./gradlew` wrapper script, so there is no need to install Gradle.

To build the project, run:

    ./gradlew build

### Running the application locally

You need access to the `sptribs-aat` Key Vault and an active VPN, because the service depends on services running in AAT.

To point local database access at local instances (needed for `bootWithCCD` and the functional tests), run:

    ./gradlew loadLocalEnvSecrets

This generates the local env file over the AAT one.

It requires an Azure login, using the Azure CLI:

- `az login`, then follow the steps to log in to Azure.
- `az acr login --name hmctsprod` to access the ACR namespace used by `bootWithCCD`, `generateCCDConfig` and `buildCCDXlsx`.

Run the application on its own with:

    ./gradlew bootRun

This starts the API on port `4013`. To check that it is up, call its health endpoint:

    curl http://localhost:4013/health

You should get a response similar to this:

    {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}

### Running the application locally with CCD and XUI

To run the full CCD and XUI stack locally, use:

    ./gradlew bootWithCCD

This starts containers for the CFTLib components (`ccd-elasticsearch`, `xui-manage-org`, `xui-manage-cases` and a Postgres database). XUI is then available on `http://localhost:3000`.

### Generate CCD JSON files

The CCD JSON files are generated on every `./gradlew bootWithCCD`. To generate them manually, run:

    ./gradlew generateCCDConfig

### Generate TypeScript definitions for the CCD definition

    ./gradlew generateTypeScript

### Using a local version of ccd-config-generator

To test changes to `ccd-config-generator`, check out that project, make the changes and run its `publishToMavenLocal` Gradle task. Then uncomment the dependency in `build.gradle`:

```groovy
  implementation group: 'com.github.hmcts', name: 'ccd-config-generator', version: 'DEV-SNAPSHOT'
```

### GitHub labels

On a pull request you can add the following labels:

- `enable_keep_helm` - keep the deployment in the preview environment for testing after the pipeline has finished
- `pr-values:wa` - use `values.wa.preview.template.yaml` to deploy with Work Allocation pods
- `pr-values:wa-ft-tests` - run the Work Allocation functional tests (requires the label above)
- `enable_ccd_diff` - run the `ccd-diff` workflow to show changes to the CCD definition

See [docs/release-and-deploy.md](docs/release-and-deploy.md) for the pipeline.

### Crons

You can run a cron task manually from the command line:

```
TASK_NAME=[task] java -jar sptribs-case-api.jar run

# E.g.
TASK_NAME=SystemClearInactiveDssDraftCasesTask java -jar sptribs-case-api.jar

# or
TASK_NAME=SystemClearInactiveDssDraftCasesTask ./gradlew bootRun
```

See [docs/scheduled-tasks.md](docs/scheduled-tasks.md) for every task and its schedule.

## Scripts

- [`bin/generate-env-ccd-definition.sh`](bin/generate-env-ccd-definition.sh) generates a local copy of the definition files for manual upload, per environment.
- [`bin/wa/deploy-demo-dmn.sh`](bin/wa/deploy-demo-dmn.sh) deploys local DMN files to the Demo environment manually.

## Licence

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
