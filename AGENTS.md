# Commons eID Agent Instructions

## Project Summary
- **Type:** Java Maven Multi-module project.
- **License:** GNU LGPL v3.0 (check `LICENSE.txt`).
- **Source:** `be.fedict.commons.eid.*`.

## Build & Run
- **Full Build:** `mvn clean install` (required to build all modules and install artifacts locally).
- **Java Version:** **Java 8** (1.8.0_401+).
    - Do not use Java 9+ syntax (no `var`, no switch expressions).
    - Ensure JCE Unlimited Strength Jurisdiction Policy is installed if running on older Java 8 distributions (as per `README.md`).

## Module Topology
- **Root:** `be.fedict:commons-eid` (aggregator + dependency management).
- **BOM:** `commons-eid-bom` (manages dependency versions for consumers).
- **client:** `commons-eid-client` (PC/SC based card access core library).
- **dialogs:** `commons-eid-dialogs` (UI dialog wrapper implementations, depends on `client`).
- **jca:** `commons-eid-jca` (JCA Security Provider implementation, depends on `client` + `dialogs`).
- **consumer:** `commons-eid-consumer` (Consumer/Sid library for signature verification, no internal deps).
- **tests:** `commons-eid-tests` (Integration tests with PC/SC simulation).

## Testing
- **Unit Tests:** Standard JUnit 5 (Jupiter) within respective modules.
- **Integration Tests:** Located in `commons-eid-tests/`.
    - Uses simulated `SimulatedCardChannel` and `.tlv`/`.bin` test resources.
    - Ensure `mvn clean install` is run first so dependencies are available in the local repo.

## Deployment
- **Release:** Configured for `autoVersionSubmodules`.
- **Targets:** Internal SCP/SFTP repositories (do not attempt external deployment without config).
