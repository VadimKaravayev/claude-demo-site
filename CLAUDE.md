# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AEM Cloud Service (AEMaaCS) project for "Claude Demo Site". Multi-module Maven build with a Webpack-based frontend pipeline. Java 11, Maven 3.3.9+, Node 16.17.0.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build and deploy to local AEM author (localhost:4502)
mvn clean install -PautoInstallSinglePackage

# Deploy to publish instance (localhost:4503)
mvn clean install -PautoInstallSinglePackagePublish

# Deploy only the core OSGi bundle
mvn clean install -PautoInstallBundle

# Deploy a single content package (run from submodule dir, e.g. ui.apps/)
mvn clean install -PautoInstallPackage

# Unit tests
mvn clean test

# Integration tests against local AEM
mvn clean verify -Plocal

# Run a single test class
mvn test -pl core -Dtest=HelloWorldModelTest

# Frontend only (from ui.frontend/)
npm run dev    # dev build + clientlib generation
npm run prod   # production build + clientlib generation
npm start      # webpack-dev-server with hot reload
npm run watch  # parallel: dev-server + chokidar + aemsync
```

## Architecture

This is a standard AEM archetype project with these modules:

- **core** — Java OSGi bundle: Sling Models, servlets, filters, schedulers, listeners. Package: `com.claude.demo.core`
- **ui.apps** — AEM components (HTL templates), clientlibs, templates. Content root: `/apps/claude-demo-site/`
- **ui.frontend** — Webpack/TypeScript/SCSS build. Entry point: `src/main/webpack/site/main.ts`. Outputs AEM clientlibs via `aem-clientlib-generator`
- **ui.content** — Sample content pages and DAM assets
- **ui.config** — OSGi configurations per runmode
- **ui.apps.structure** — Repository structure definition (deploy order dependency)
- **all** — Aggregator package embedding all modules + vendor dependencies (Core WCM Components 2.28.0)
- **dispatcher** — Apache Dispatcher configuration for AEMaaCS CDN
- **it.tests** — Java integration tests using AEM Testing Clients (JUnit 4, `*IT.java` pattern)
- **ui.tests** — Cypress E2E tests against AEM author instance

## Key Conventions

- **Sling Models**: Use `@Model(adaptables = Resource.class)` with `@ValueMapValue`, `@SlingObject`, `@OSGiService` injectors and `@PostConstruct` for init logic
- **Servlets**: Extend `SlingSafeMethodsServlet`, annotated with `@SlingServletResourceTypes`
- **OSGi configs**: Use `@Designate(ocd = Config.class)` with `@ObjectClassDefinition`
- **HTL components**: Located at `ui.apps/src/main/content/jcr_root/apps/claude-demo-site/components/`. Use `data-sly-use` to bind Sling Models
- **CSS**: BEM naming — `.cmp-[component]__[element]--[modifier]`. Component SCSS files in `ui.frontend/src/main/webpack/components/`
- **Clientlib categories**: `claude-demo-site.base` (base styles), `claude-demo-site.site` (main JS/CSS)
- **Unit tests**: JUnit 5 with `io.wcm.testing.aem-mock` (`AemContext`, `AemContextExtension`). Located alongside source in `core/src/test/`
- **Integration tests**: Must be in `it.tests/src/main/java/` (not src/test) and match `*IT.java` naming
