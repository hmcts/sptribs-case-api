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

Nothing. Every CIC config declares `CriminalInjuriesCompensationData`, so the
compiler guarantees a new case type cannot reach CIC's ~70 events and CIC cannot
reach yours. There is no fan-in and no replay.

Verified empirically: a throwaway case type with one field and no events
generated 17 definition files containing only its own field, against CIC's 258.

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

## The trap this replaced

Until every CIC config was retyped, the ~70 that declared bare `CaseData`
resolved as a second, **nameless** group — nothing calls `caseType(...)` for that
class, so its case type id was `""` and its output directory was
`new File(destinationFolder, "")`: `build/definitions/` itself, the parent of
every real case type directory.

`CCDDefinitionGenerator` clears each group's directory before writing it. While
CIC was the only case type this looked cosmetic — 15 stray `*.json` loose in
`build/definitions/`, which `ccd-build-definition.sh` ignores because it only
picks up subdirectories. With a second case type it is **destructive**: writing
the nameless group deletes every case type already written to the parent. Spring
injects beans in package order, so whichever case type sorts before `ciccase`
disappears, surfacing as

    FileNotFoundException: build/definitions/<YourCaseType>

against generated Java that compiles and is entirely correct — which is what
makes it expensive to find.

So: **never declare a config on bare `CaseData`.** If you add a config to CIC,
declare it on `CriminalInjuriesCompensationData`. A quick check that nothing has
regressed:

```sh
grep -rn 'CCDConfig<CaseData,' src/main/java   # must return nothing
ls build/definitions/                          # must contain only directories
```

Shared page classes (`CcdPageConfiguration` implementations) are the exception
and stay on the base class, via `<T extends CaseData> void addTo(PageBuilder<T>)`
— they are not `CCDConfig` beans, so they form no group.
