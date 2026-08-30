# Adding a CCD case type

This service can register any number of CCD case types. This is what it takes.

## The mechanism

`CCDDefinitionGenerator` groups every `CCDConfig` bean in the Spring context by
its **case-data class** — generic argument 0 — and writes one definition
directory per group, named after the case type id. So a case type is simply
"the set of configs sharing a case-data class". There is no registry of events,
no annotation, no wiring: the type parameter *is* the wiring.

`nfdiv-case-api` is the reference implementation, with three case types and no
multi-case-type machinery at all.

## What does NOT leak

`CriminalInjuriesCompensation` injects every `CCDConfig<CaseData, State, UserRole>`
in the context and replays it onto its own builder:

```java
@Autowired
private List<CCDConfig<CaseData, State, UserRole>> cfgs;
```

That looks alarming, and it is easy to assume a new case type would inherit
CIC's ~70 events. **It does not.** The fan-in is a field on the CIC root config,
pulling CIC's own configs into CIC deliberately. A new root config simply does
not have that field, so nothing is replayed into it.

Verified empirically: a throwaway case type with one field and no events
generated 17 definition files containing only its own field, against CIC's 256.

## Steps

1. **Case data class.** Its own class. It may extend `CaseData` to inherit the
   platform fields (flags, links, search criteria), or stand alone. Extending is
   what CIC does via `CriminalInjuriesCompensationData`; the generator keys on
   the concrete class either way.

2. **State enum.** Its own. Do **not** reuse `ciccase.model.State` — all 21 of
   its states carry the hint `### ${cicCaseFullName}`, a CIC field, and several
   (`Concession`, `Rule27`, `DeathOfAppellant`, `DSS_*`) are CIC-domain. Name the
   enum distinctly: `generateTypeScript` has a flat namespace with
   `mapEnum='asEnum'`, so a second enum called `State` collides in `index.ts`.

3. **Root config** — `CCDConfig<YourCaseData, YourState, UserRole>`, the only
   place that calls `caseType(...)` and `jurisdiction(...)`. Set
   `setCallbackHost` if the case type is decentralised.

4. **Events, tabs, search, workbasket** — all typed on your case-data class, in
   your own package. The compiler now guarantees they cannot reach CIC and CIC's
   cannot reach them.

5. **Register it** — add a constant to `CcdCaseType` and `CcdServiceCode` (and
   `CcdJurisdiction` if it is a new jurisdiction). `HighLevelDataSetupApp` and
   `CftLibConfig` both iterate `CcdServiceCode.values()`, so nothing else needs
   editing to load the definition or seed profiles.

6. **build.gradle** — if the case type is decentralised, add
   `CCD_DECENTRALISED_CASE-TYPE-SERVICE-URLS_<CaseTypeId>` to the `CftlibExec`
   block. Append to `XUI_JURISDICTIONS` in `bootWithCCD` and `cftlibTest` only if
   the jurisdiction is new.

`bin/ccd-build-definition.sh` and `buildCCDXlsx` discover definition directories,
so they need no change.

## Known wart

The ~70 `CCDConfig<CaseData, State, UserRole>` configs also resolve as a second,
nameless group (their case-data class is bare `CaseData`, and nothing calls
`caseType(...)` for it). Its `caseType` is `""`, so `new File(dest, "")` writes
its files loose into `build/definitions/` — 15 stray `*.json` alongside the real
directories. Harmless: `ccd-build-definition.sh` only picks up subdirectories,
and the loader iterates the enum. Retyping those configs onto
`CriminalInjuriesCompensationData` would remove it, but that touches ~139 files
for a cosmetic gain and was judged not worth the risk.
