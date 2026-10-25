# Workflow: Synchronize Git Branches

## Overview
Synchronizes the 3 active branches: `main`, `dev`, and `onesignal-integration`.

## Execution
Run from repository root in PowerShell:
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
