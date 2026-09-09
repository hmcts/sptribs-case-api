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

For running functional tests locally, we need to make sure all the local database access is pointing to local instances.
please run:

        ./gradlew loadLocalEnvSecrets

to generate the local env file over the aat one, then your bootwithCCD and functional tests will use this.

This will require an Azure login, which can be done with the Azure CLI with:
`az login` and following the steps to log into Azure
`az acr login --name hmctsprod` to access the ACR namespace as part of `bootWithCCD` or `generateCCDConfig`/`buildCCDXlsx`

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

This will start a containers for the CFTLib components (`ccd-elasticsearch`,`xui-manage-org`, `xui-manage-cases`, postgres database)
Then you can access XUI on `http://localhost:3000`

#### Running without AAT (local auth)

By default `bootWithCCD` authenticates the whole stack against AAT IDAM and pulls
secrets from the `sptribs-aat` vault, so it needs an Azure login and the VPN. If AAT
is unavailable — or you simply want a self-contained stack — add `-PlocalAuth`:

    ./gradlew bootWithCCD -PlocalAuth

This switches cftlib to `AuthMode.Local`, which starts the IDAM simulator on `5062`
and the S2S simulator on `8489`, skips the `loadEnvSecrets` Key Vault fetch, and
points outbound integrations (PDF, fees, doc assembly, DM store, RD professional) at
a local wiremock on `8765` instead of AAT hostnames. Nothing in the stack reaches the
AAT network. Log in through XUI as usual — the simulator accepts any known local user.

Without the flag behaviour is unchanged, so CI and anyone with working AAT access
keeps the AAT-backed stack.

Two things to know if the stack does not come up:

- **XUI exits with `idam api must be up to start`.** cftlib's compose file has no
  `depends_on` between `xui-manage-cases` and the IDAM simulator, so if the simulator
  is being (re)created XUI can lose the race and exit. The JVM services are unaffected.
  Just restart the containers: `docker start cftlib-xui-manage-cases-1 cftlib-xui-manage-org-1`.
- **Definition store fails Flyway with `Detected applied migration not resolved locally`.**
  The shared Postgres container is reused across projects, so a newer cftlib may have
  left migrations in the platform databases that this version cannot resolve. Drop and
  let cftlib recreate them (project data in `sptribs` is untouched):

      for db in definitionstore datastore userprofile am cft_task_db; do \
        docker exec cftlib-shared-database-pg-1 psql -U postgres -c "drop database if exists $db;"; done

### Generate CCD JSON files

Generating the CCD JSON files will happen on every `./gradlew bootWithCcd` but you can manually trigger this with:

    ./gradlew generateCCDConfig

### Generate TypeScript definitions for CCD definition

    ./gradlew generateTypeScript

### Using a Local version of the CCD-Config-Generator

You can edit the `ccd-config generator` and test a local version by checking out the project, making changes and running the `publishToMavenLocal` gradle task
Then uncomment the dependency in `build.gradle` like so:
```groovy
  implementation group: 'com.github.hmcts', name: 'ccd-config-generator', version: 'DEV-SNAPSHOT'
```

### GitHub Labels
On a pull request you can add the following labels:
- `enable_keep_helm` - to keep the deployment in the preview environment for testing after the pipeline has finished
- `pr-values:wa` - use the `values.wa.preview.template.yaml` to deploy with Work Allocation pods
- `pr-values:wa-ft-tests` - run the Work Allocation Functional tests (requires the above)
- `enable_ccd_diff` - runs the `ccd-diff` workflow to show changes to the CCD definition

### Crons

You can manually run a cron task from the cli:

```
TASK_NAME=[task] java -jar sptribs-case-api.jar run

# E.g.
TASK_NAME=SystemProgressHeldCasesTask java -jar sptribs-case-api.jar

# or
TASK_NAME=SystemProgressHeldCasesTask ./gradlew bootRun
```
## Scripts
`generate-env-ccd-definition.sh` can be used to generate a local copy of definition files for manual uploads per environment

`deploy-demo-dmn.sh` can be used to manually deploy local DMN files to Demo environment

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
