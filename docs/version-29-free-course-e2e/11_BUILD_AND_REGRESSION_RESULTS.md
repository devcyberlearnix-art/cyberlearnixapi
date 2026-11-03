# ASIVO LMS — VERSION 29
# 11 BUILD AND REGRESSION RESULTS

**Verdict:** PASS  
**Scope:** Frontend production build, microservices compilation, regression verification.

---

## 1. Frontend Production Build

- **Tool:** Vite 5.4.21
- **Command:** `npm run build`
- **Build Duration:** 1m 40s
- **Transformed Modules:** 2,602 modules
- **Output:** Clean bundle generated in `dist/`:
  - `dist/index.html` (3.62 kB)
  - `dist/assets/index-BYS33EjE.css` (127.94 kB)
  - Code-split routes: `MyLearning-oJ1HpV_s.js`, `Courses-BpjhJXGF.js`, `ClassroomView-B-Px5V7H.js`
- **Errors / Warnings:** 0 errors.

---

## 2. Microservice Compilation (course-service)

- **Tool:** Gradle 8.x
- **Command:** `./gradlew :course-service:compileJava :course-service:bootJar --no-build-cache --rerun-tasks -x test`
- **Result:** `BUILD SUCCESSFUL in 3m 45s`
- **Actionable Tasks:** 6 executed, 0 failed.
- **Artifact:** `course-service-0.0.1-SNAPSHOT.jar` (97,144,857 bytes)
- **Deployment:** Copied to `cyberlearnix-course-service:/app/app.jar`, SHA-256: `c841a181602d9c3902dbdff818802cf0704ce17a1d5ab65ae05aea377fcbaf35`.
- **Runtime Health:** Actuator `/actuator/health` status: `UP`.

---

## 3. Regression Suite Summary

| Component | Assertions | Passed | Failed | Success Rate |
|---|---|---|---|---|
| **Free Course E2E Test Suite** | 30 | 30 | 0 | 100.0% |
| **Instructor Onboarding Suite (V27/V28)** | 45 | 45 | 0 | 100.0% |
| **Playwright Browser Visual Suite** | 8 | 8 | 0 | 100.0% |
| **Total Platform Assertions** | **83** | **83** | **0** | **100.0%** |
