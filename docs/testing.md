# Testing

This page lists the test suites for `sptribs-case-api` and how to run them. The repository has Gradle suites for unit, integration, functional, smoke, contract and in-process end-to-end tests. Browser end-to-end tests that span this API and the two citizen front ends live in a separate Playwright repository, [sptribs-e2etests](https://github.com/hmcts/sptribs-e2etests).

## The suites

| Layer | Source set | Command | Needs |
|---|---|---|---|
| Unit | `src/test` | `./gradlew test` | Nothing |
| Integration | `src/integrationTest` | `./gradlew integration` | Docker (Testcontainers Postgres) |
| Contract | `src/contractTest` | `./gradlew runAndPublishConsumerPactTests` | Pact Broker access to publish |
| Functional | `src/functionalTest` | `./gradlew functional` | AAT and the VPN |
| Smoke | `src/functionalTest` (smoke package) | `./gradlew smoke` | A deployed instance |
| In-process end-to-end | `src/cftlibTest` | `./gradlew cftlibTest` | Docker. The task starts the stack |
| Static checks | all | `./gradlew check` | Nothing |

Run them from the repository root. All tasks are defined in [`build.gradle`](../build.gradle).

### Unit

`./gradlew test` runs `src/test/java` with JUnit 5. Fail-fast is on, and the task adds `--add-opens` JVM arguments for `java.base/java.lang.reflect` and `java.base/java.util`. JaCoCo coverage from this task feeds Sonar.

Unit tests for events usually assert the generated config (state transition, grants) and each callback's response. See [Adding an event](adding-an-event.md).

### Integration

`./gradlew integration` runs `**/*IT.class` from the `integrationTest` source set under the `integration` Spring profile, against a Testcontainers Postgres. Fail-fast is on. Flyway is disabled in this profile.

If Testcontainers can't talk to Docker, see [Running locally](running-locally.md#troubleshooting).

### Functional

`./gradlew functional` runs `**/*FT.class` from `functionalTest`, except the smoke package, under the `functional` profile. `application-functional.yaml` points at real AAT IDAM, S2S and CCD with IDAM test users, so you need the VPN.

The task reads the same dotenv file as `bootRun`: `.local-env` if present, otherwise `.aat-env`. Run `./gradlew loadEnvSecrets` first to write `.aat-env` from the `sptribs-case-api-dot-env` secret in the `sptribs-aat` vault. See [Running locally](running-locally.md#environment-files).

Test tasks fork across half the available processors. Set `WA_FUNCTIONAL_TESTS_ENABLED=true` to use all of them.

### Smoke

`./gradlew smoke` runs `uk/gov/hmcts/sptribs/smoke/**` from the functional sources under the `functional` profile. The pipeline runs it against preview and AAT after each deployment.

### Contract

`./gradlew runAndPublishConsumerPactTests` runs the Pact consumer tests in `src/contractTest` and publishes the pacts to `PACT_BROKER_FULL_URL`, which defaults to `https://pact-broker.platform.hmcts.net/`.

### In-process end-to-end

`./gradlew cftlibTest` starts CCD, Elasticsearch, XUI, Postgres and the IDAM (port 5000) and S2S (port 8489) simulators in-process with local auth and `XUI_JURISDICTIONS=ST_CIC`. It then runs the Playwright tests in `src/cftlibTest` against them. Downstream services such as doc assembly, fees and DM store point at WireMock on port 8765, and CDAM document attach is off. Test classes run concurrently and methods in the same thread. The task depends on `installChromium`, which fetches the browser.

### Static checks

`./gradlew check` runs the tests plus Checkstyle (`config/checkstyle`, zero warnings allowed) and PMD. The OWASP dependency check and Sonar run in the Jenkins pipeline.

## Continuous integration

- **GitHub Actions.** [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) runs `check` and `integration` on Java 21 for every PR to master and every push to master.
- **Jenkins.** `Jenkinsfile_CNP` runs the unit tests and static checks, deploys to preview (on a PR) or AAT (on master), and then runs the smoke and functional tests against that deployment. See [Release and deploy](release-and-deploy.md).

### PR labels that change testing

| Label | Effect |
|---|---|
| `pr-values:wa` | Deploys the Work Allocation pods into the preview environment |
| `pr-values:wa-ft-tests` | Runs the Work Allocation functional tests. Needs `pr-values:wa` as well |
| `enable-e2e-tests` | Before the preview functional tests, a "UI Tests" stage runs the `sptribs-e2etests` Jenkins job against the PR's XUI. The job waits and passes its result back, so a failure fails the build |
| `enable_ccd_diff` | Posts a CCD definition diff against master as a PR comment |

Without `enable-e2e-tests`, no browser end-to-end tests run on a PR to this repository.

## Browser end-to-end tests (sptribs-e2etests)

[sptribs-e2etests](https://github.com/hmcts/sptribs-e2etests) holds the Playwright suites for DSS submit, DSS update and the Case API / XUI caseworker flows. Its README is the full reference. The short version:

1. Connect the VPN and run `az login`.
2. In a checkout of that repository, run `yarn install`, then `yarn configure` once (it runs `playwright install --with-deps`).
3. Run `yarn load-secrets`, which writes `.env` from the `sptribs-aat` Key Vault. Alternatively, copy `.env.example` to `.env` and fill it in.
4. Set the base URLs. The tests read `DSS_BASE_URL`, `UC_BASE_URL` and `CASEAPI_BASE_URL`.
5. Run a suite:

| Command | Scope |
|---|---|
| `yarn test:ChromeDSS` | DSS submit (`@DSSCreate`) |
| `yarn test:ChromeUC` | DSS update case (`@DSSUpdate`) |
| `yarn test:ChromeCaseAPI` | Case API / XUI caseworker flows (`@CaseAPI`) |
| `yarn test:ChromeCaseAPI1`, `2`, `3` | Case API split by `@CaseAPI1`, `@CaseAPI2` and `@CaseAPI3` |
| `yarn test:accessibility` | `@accessibility` tests, Chrome only |
| `yarn test:ci` | `@continuousIntegration` subset, Chrome. Used for preview runs |
| `yarn test:fullfunctional` | All tests, Chrome |
| `yarn test:crossbrowser` | All tests, Firefox and WebKit |

To run against a preview deployment of this API, point `CASEAPI_BASE_URL` at its XUI:

```bash
export CASEAPI_BASE_URL=https://xui-sptribs-case-api-pr-<PR>.preview.platform.hmcts.net
yarn test:ci
```

On preview, run `test:ci`, as the pipeline does. The DSS tests check that the case is visible in Case API, so they need a matching case-api deployment.

Open the local HTML report with `yarn test:report`.

The `sptribs-e2etests` nightly pipeline runs the full suite against AAT on weekdays and posts results to `#special-tribunals-builds`. It is the main regression signal across the three applications.

## Verify

- `./gradlew test integration` ends with `BUILD SUCCESSFUL`.
- In `sptribs-e2etests`, after `yarn load-secrets`, `yarn test:ChromeDSS` passes and `yarn test:report` lists the `@DSSCreate` tests as passed.

## See also

- [Running locally](running-locally.md)
- [Adding an event](adding-an-event.md): the test pattern for a new event
- [Release and deploy](release-and-deploy.md): where each suite runs in the pipeline
