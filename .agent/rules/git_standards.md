# Git & Branch Management Guidelines

## 1. Commit Message Convention
Always use the Conventional Commits specification:
- `feat(...)`: New user-facing feature or enhancement (e.g. `feat(wifi): implement automated 1-step quick sheet`).
- `fix(...)`: Bug fix or error resolution (e.g. `fix(alerts): eliminate overload conflicts in MainViewModel`).
- `refactor(...)`: Code changes that neither fix a bug nor add a feature.
- `docs(...)`: Documentation and guide updates.
- `chore(...)`: Dependency updates, build configuration, and maintenance scripts.

## 2. Multi-Branch Synchronization
Keep the 3 primary branches synchronized:
- `main`: Production-stable baseline.
- `dev`: Active development and feature staging.
- `onesignal-integration`: Targeted push notification and alert subsystem branch.

When pushing commits:
```powershell
git checkout main
git push origin main
git checkout dev
git merge main
git push origin dev
git checkout onesignal-integration
git merge dev
git push origin onesignal-integration
git checkout main
```
