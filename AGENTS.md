# Repository Guidelines

## Encoding Rules
- All files must be read and written using UTF-8 without BOM.
- Never modify file encoding.

## Project Structure & Module Organization
IRAS is a multi-module Java + Vue monorepo.
- Backend modules: `iras-admin` (entry point/config/controllers), `iras-framework` (security/config), `iras-system` (system management), `iras-common` (shared utils/entities), `iras-bi` (BI and data-quality features), `iras-generator` (code generation).
- Frontend: `ui/` (Vue 2 + Element UI + ECharts), with API clients in `ui/src/api`, pages in `ui/src/views`, reusable components in `ui/src/components`.
- SQL and migration helpers: `sql/`.
- Tests: primarily under `iras-bi/src/test/java` and `iras-common/src/test/java`.

## Build, Test, and Development Commands
Backend (run at repo root):
- `mvn clean compile` - compile all modules.
- `mvn clean package -DskipTests` - build distributables without tests.
- `mvn test -pl iras-bi` - run module tests.
- `mvn test -Dtest=ClassName#methodName` - run a single test.

Frontend (run in `ui/`):
- `npm install --registry=https://registry.npmmirror.com` - install dependencies.
- `npm run dev` - start local dev server.
- `npm run build:prod` - build production assets.

## Coding Style & Naming Conventions
- Java: 4-space indentation, `UpperCamelCase` classes, `lowerCamelCase` methods/fields, package names lowercase.
- Vue/JS: follow `ui/.editorconfig` (UTF-8, 2 spaces, LF, trim trailing whitespace).
- Keep controller/service/mapper naming aligned (`*Controller`, `*Service`, `*Mapper`), and place MyBatis XML in `src/main/resources/mapper/`.
- Use `jakarta.*` APIs for new backend code.

## Testing Guidelines
- Backend uses JUnit-style tests (`*Test.java`).
- Add/extend tests near changed modules, especially controller/service/engine logic in `iras-bi`.
- Run targeted tests first, then module-level tests before opening a PR.
- Frontend has no dedicated test script configured; validate key flows with `npm run dev` and production build checks.

## Commit & Pull Request Guidelines
- Prefer Conventional Commits (seen in history): `feat: ...`, `refactor: ...`.
- Avoid generic messages like `Changes`; use clear, scoped summaries.
- PRs should include:
  - What changed and why.
  - Affected modules/paths (for example, `iras-bi/...`, `ui/src/views/...`).
  - Verification evidence (commands run and results).
  - Screenshots/GIFs for UI changes and related issue/task links.

## Security & Configuration Tips
- Do not commit real credentials; keep environment-specific values in local config.
- Review `iras-admin/src/main/resources/application.yml` changes carefully, especially datasource and auth settings.
