# Running locally

This guide runs `sptribs-case-api` on a developer machine. The usual way is `./gradlew bootWithCCD`, which starts the API with the CFT stack (CCD, Elasticsearch, Postgres and XUI) in-process through [rse-cft-lib](https://github.com/hmcts/rse-cft-lib). The API listens on port 4013 and XUI Manage Cases on `http://localhost:3000`. On master the stack signs in against AAT IDAM, so you need the VPN and read access to the `sptribs-aat` Key Vault.

## Prerequisites

| Tool | Why | Notes |
|---|---|---|
| Java 21 | Build and run | Set as the Gradle toolchain in [`build.gradle`](../build.gradle) |
| Docker | cftlib dependencies, and Testcontainers in `./gradlew integration` | See [Troubleshooting](#troubleshooting) |
| Azure CLI | Loading Key Vault secrets (`sptribs-aat`, and `sptribs-preview` for `loadLocalEnvSecrets`), and `az acr login --name hmctsprod` when building the definition XLSX | |
| VPN | Any AAT dependency (`*.service.core-compute-aat.internal`, AAT IDAM, Key Vault) | |

Sign in to Azure first:

```bash
az login
az account show   # confirm the subscription that holds the sptribs vaults
```

## Environment files

`bootRun`, `bootWithCCD` and `functional` read environment variables from a dotenv file. Two Gradle tasks write one, each by decoding the base64 `sptribs-case-api-dot-env` secret, and each only if the file doesn't exist yet:

| Task | Vault | File written |
|---|---|---|
| `loadEnvSecrets` | `sptribs-aat` | `.aat-env` |
| `loadLocalEnvSecrets` | `sptribs-preview` | `.local-env` |

The tasks load `.local-env` if it exists, otherwise `.aat-env`. `bootRun` and `bootWithCCD` depend on `loadEnvSecrets`, so `.aat-env` is always fetched, but a `.local-env` wins whenever it is present. Use `loadLocalEnvSecrets` when you want `bootWithCCD` and the functional tests to point at local databases. `./gradlew reloadEnvSecrets` deletes both files and fetches `.aat-env` again.

## Option A: full stack with cftlib

`bootWithCCD` is a `CftlibExec` task. It starts the CCD definition and data stores, Elasticsearch, Postgres and XUI in the same JVM as the API. CCD sends `CriminalInjuriesCompensation` callbacks to `http://localhost:4013`, because every `CftlibExec` task sets `CCD_DECENTRALISED_CASE-TYPE-SERVICE-URLS_CriminalInjuriesCompensation`. Service Bus publishing is turned off for these tasks.

On master, `bootWithCCD` does not set `authMode`, so cftlib uses its default, which authenticates XUI and CCD against AAT IDAM and S2S. Only `cftlibTest` uses local auth. `PDF_API_URL` points at the AAT PDF service.

[`CftLibConfig`](../src/cftlib/java/uk/gov/hmcts/sptribs/cftlib/CftLibConfig.java) creates CCD user profiles for the test caseworker and solicitor for `ST_CIC` / `CriminalInjuriesCompensation`, and [`cftlib-am-role-assignments.json`](../src/main/resources/cftlib-am-role-assignments.json) seeds the caseworker's role assignments. In AAT auth mode you sign in with an account that exists in AAT IDAM.

1. Start Docker and connect the VPN.
2. From the repository root, run:

   ```bash
   ./gradlew bootWithCCD
   ```

3. Wait for the definition import to finish. Open XUI Manage Cases at `http://localhost:3000` and sign in with an AAT account. XUI Manage Organisation runs on port 3003.
4. Check the API is up:

   ```bash
   curl http://localhost:4013/health
   ```

## Option B: local auth, no AAT (send35-prototype branch)

The `send35-prototype` branch adds a `-PlocalAuth` flag to `bootWithCCD`. It is not on master. It switches cftlib to local auth, skips the Key Vault fetch, and uses:

- an IDAM simulator on port 5062 and an S2S simulator on port 8489, with test users seeded into the simulator;
- WireMock on port 8765 in place of PDF, fees, doc assembly, DM store and RD professional;
- `RSE_LIB_XUI_ENV_SERVICES_IDAM_ISS_URL=http://localhost:5062`, so XUI accepts the simulator's tokens (this needs the newer rse-cft-lib the branch pins);
- localhost for role assignment and CCD, with CDAM document attach turned off.

```bash
git fetch origin && git switch send35-prototype
./gradlew bootWithCCD -PlocalAuth
```

The branch [README](https://github.com/hmcts/sptribs-case-api/blob/send35-prototype/README.md) covers the recovery steps for this mode.

## Option C: plain `bootRun` against AAT

1. Connect the VPN and sign in with `az login`.
2. Start the app. `bootRun` runs `loadEnvSecrets` first:

   ```bash
   ./gradlew bootRun
   ```

3. The app needs a Postgres it can reach. The datasource is `${POSTGRES_HOST:localhost}:${POSTGRES_PORT:6432}/${POSTGRES_NAME:sptribs}` in [`application.yaml`](../src/main/resources/application.yaml), and Flyway runs on start-up. Service Bus is enabled by default (`spring.jms.servicebus.enabled`). See [Configuration](configuration.md).

To run a scheduled task once, set `TASK_NAME`:

```bash
TASK_NAME=SystemClearInactiveDssDraftCasesTask ./gradlew bootRun
```

See [Scheduled tasks](scheduled-tasks.md).

## Generate the CCD definitions

1. Generate the JSON. The task starts the app in the `config-gen` profile, with Flyway, JMS and the database turned off:

   ```bash
   ./gradlew generateCCDConfig
   ls build/definitions/        # one directory per case type
   ```

   The output goes to `build/definitions/`. `CftLibConfig` and [`bin/ccd-build-definition.sh`](../bin/ccd-build-definition.sh) read the same directory.

2. Build the XLSX for an environment:

   ```bash
   az acr login --name hmctsprod
   ./bin/generate-env-ccd-definition.sh aat
   ls build/ccd-config/         # ccd-<CaseType>-<env>.xlsx
   ```

   `bin/ccd-build-definition.sh` leaves out `*-nonprod.json` and `*-testing.json`.

3. Generate the TypeScript definitions, if you need them:

   ```bash
   ./gradlew generateTypeScript
   ```

## Using a local ccd-config-generator

To test changes to [ccd-config-generator](https://github.com/hmcts/ccd-config-generator), check it out, make your changes and run its `publishToMavenLocal` task. Then uncomment the `DEV-SNAPSHOT` dependency in `build.gradle`:

```groovy
implementation group: 'com.github.hmcts', name: 'ccd-config-generator', version: 'DEV-SNAPSHOT'
```

## The citizen apps

The citizen front ends are separate Node/Express repositories. Each has a README that covers running it locally:

- [sptribs-frontend](https://github.com/hmcts/sptribs-frontend): DSS submit, CICA lookup and the citizen dashboard. In development mode it runs on `http://localhost:3001` and its `config/development.yaml` points at this API on `http://localhost:4013` and the cftlib data store on `http://localhost:4452`.
- [sptribs-dss-update-case-web](https://github.com/hmcts/sptribs-dss-update-case-web): DSS update case. It runs on `http://localhost:3100`. Set `CASE_API_URL=http://localhost:4013` to point it at a local API.

Both sign in through AAT IDAM and fetch their own secrets from `sptribs-aat` with `az`.

## Troubleshooting

### Testcontainers fails with a Docker API version error

`./gradlew integration`, or another task that starts containers, fails because the client API version doesn't match the Docker daemon (see [testcontainers-java#11212](https://github.com/testcontainers/testcontainers-java/issues/11212)). Override the client API version in `~/.docker-java.properties`, using a version your daemon accepts (`docker version` shows the daemon's minimum):

```bash
echo "api.version=1.44" >> ~/.docker-java.properties
```

### Secrets don't load

`loadEnvSecrets` fails with an authentication or "forbidden" error, or the dotenv file comes out empty. Run `az login` again (tokens expire), check `az account show` is on the right subscription, and connect the VPN. Your account needs read access to `sptribs-preview` or `sptribs-aat`.

### AAT hostnames don't resolve

`UnknownHostException` or timeouts for `*.service.core-compute-aat.internal`, AAT IDAM or Key Vault mean the VPN is not connected. The functional tests, `bootWithCCD` on master and `bootRun` all reach AAT.

### Other problems

| Symptom | Fix |
|---|---|
| `buildCCDXlsx` fails pulling an image | Run `az acr login --name hmctsprod` |
| `-PlocalAuth` (branch only): XUI fails on first start | Restart it |
| `-PlocalAuth` (branch only): `Detected applied migration not resolved locally` | The shared cftlib Postgres container has platform-database migrations from a different cftlib version. Drop the platform databases (`definitionstore`, `datastore`, `userprofile`, `am`, `cft_task_db`) as the branch README describes. The `sptribs` database is not affected |

## Verify

- `curl http://localhost:4013/health` returns `UP`.
- XUI at `http://localhost:3000` lists the `ST_CIC` jurisdiction after you sign in.

## See also

- [Testing](testing.md): the test suites, including `cftlibTest`
- [Configuration](configuration.md): environment variables and profiles
- [Architecture](architecture.md): how the API, CCD and the citizen apps fit together
