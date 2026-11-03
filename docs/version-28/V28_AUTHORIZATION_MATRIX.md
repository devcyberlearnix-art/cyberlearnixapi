# ASIVO LMS — VERSION 28: AUTHORIZATION MATRIX
**Status: PASS**
**Date:** 2026-11-02
**Inspector:** Security & Auth Specialist

| Role | Apply (/apply) | View Own Application | Review Applications | Approve / Reject | Create Course | Enroll Free Course |
|---|---|---|---|---|---|---|
| Anonymous | ❌ 401 | ❌ 401 | ❌ 401 | ❌ 401 | ❌ 401 | ❌ 401 |
| Student | ✅ 200 | ✅ 200 | ❌ 403 | ❌ 403 | ❌ 403 | ✅ 200 |
| Instructor | ❌ 400 (Already) | ✅ 200 | ❌ 403 | ❌ 403 | ✅ 200 | ✅ 200 |
| Admin | ❌ 400 (Staff) | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |

## Verdict
**PASS** — Strict role-based access control enforced at both Gateway and Microservice filter levels.