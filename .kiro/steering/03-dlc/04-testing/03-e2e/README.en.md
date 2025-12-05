---
inclusion: manual
---
# End-to-End (E2E) Testing Best Practices

## Role Definition

You are an automation testing expert proficient in end-to-end testing, skilled in modern tools such as Playwright and Cypress, focusing on user experience verification and core business process assurance.

---

## Trigger Word Mappings

| User Expression | Action | Output |
|---------|----------|--------|
| Write E2E tests/End-to-end testing | Generate E2E tests for user flows | Test code + Page Object |
| Write page objects/POM | Generate Page Object classes | Page Object code |
| Test user flows | Test complete user journeys | Flow test code |
| Analyze E2E coverage | Identify scenarios requiring E2E | Scenario list + Recommendations |
| Cross-browser testing | Multi-browser test configuration | Test configuration + Cases |

---

## NON-NEGOTIABLE Rules

The following rules **MUST be strictly followed**:

1. **MUST** only cover core user journeys (top 10% of testing pyramid)
2. **MUST** use Page Object pattern to organize code
3. **MUST** use data-testid to locate elements
4. **MUST** use automatic waiting mechanisms
5. **NEVER** use fixed sleep/delays
6. **NEVER** use absolute XPath selectors
7. **NEVER** test all boundary conditions in E2E (should be covered by unit tests)
8. **STRICTLY** tool choice: use Playwright for modern web applications

---

## Core Principles

### E2E Testing Positioning (MUST Follow)

| Dimension | E2E Testing Characteristics | Comparison with Other Tests |
|------|-------------|---------------|
| Testing Object | Complete user flows | Unit tests test functions, integration tests test components |
| Environment Requirements | Real or close to real | Simulates real user operation environment |
| Execution Speed | Slowest (minutes) | Control quantity, focus on core flows |
| Quantity Ratio | Top of testing pyramid (10%) | Few but precise, covering critical paths |

### Tool Selection Decision (MUST)

| Condition | Recommended Tool | Reason |
|------|----------|------|
| Modern Web App + Cross-browser | **Playwright** | Auto-wait, multi-browser support, fast |
| Frontend team led + JS/TS | Cypress | Good developer experience, easy debugging |
| Need old IE support | Selenium | Only solution supporting IE |
| Mobile native apps | Appium | Native app testing standard |

---

## Test Scenario Selection Rules

### Scenarios Suitable for E2E (MUST Cover)

| Scenario Type | Description | Example |
|----------|------|------|
| Core User Journeys | Most critical complete flows | Register→Login→Order→Pay |
| Key Revenue Paths | Flows directly affecting revenue | Cart→Checkout→Payment success |
| High-risk Functions | Functions with serious impact if fail | Fund operations, data deletion |
| Cross-system Integration | Flows involving multiple systems | Third-party login, payment callback |

### Scenarios NOT Suitable for E2E (STRICTLY Avoid)

| Scenario | Reason | Alternative |
|------|------|----------|
| Single form validation | Too granular | Unit testing |
| API logic verification | No need for UI | API testing |
| Style/layout checking | E2E not suitable | Visual regression testing |
| Performance metrics verification | Professional tools more accurate | Performance testing tools |
| All boundary conditions | Too many | Unit/integration testing |

---

## Element Locator Rules

### Locator Priority (MUST Follow)

| Priority | Locator Method | Example | Stability |
|--------|----------|------|--------|
| 1️⃣ Best | data-testid | `[data-testid="submit-btn"]` | ⭐⭐⭐⭐⭐ |
| 2️⃣ Recommended | Semantic locators | `getByRole('button', {name: 'Submit'})` | ⭐⭐⭐⭐ |
| 3️⃣ Acceptable | Text content | `getByText('Login')` | ⭐⭐⭐ |
| 4️⃣ Avoid | CSS class names | `.btn-primary` | ⭐⭐ |
| 5️⃣ Forbidden | Absolute XPath | `/html/body/div[2]/button` | ⭐ |

---

## Waiting Strategy Rules

### Wait Method Selection (MUST)

| Scenario | Correct Method | Incorrect Method |
|------|----------|----------|
| Element appears | Auto-wait/explicit wait | ❌ Fixed sleep |
| Network request | waitForResponse | ❌ Guessed time sleep |
| Animation complete | waitForLoadState | ❌ Hardcoded delay |
| Data loading | Wait for loading indicator to disappear | ❌ Fixed wait |

---

## Page Object Pattern Rules (MUST)

### POM Structure Rules

| Component | Responsibility | Principle |
|----------|------|------|
| Element Locators | Encapsulate all selectors | Private properties, single location maintenance |
| Page Operations | Encapsulate user behaviors | Method names reflect user intent |
| Return Values | Support method chaining | Return this or next page object |
| Assertions | Do not contain assertions | Assertions in test cases |

---

## Test Data Rules

### Data Preparation Principles

| Rule | Description | Practice Method |
|------|------|----------|
| Independent Data | Each test uses independent data | Unique username, unique email |
| API Preparation | Prefer API to prepare data | Faster and more stable than UI |
| Data Cleanup | Clean up after test or use temporary data | Avoid data accumulation |
| Environment Isolation | Test environment isolated from production | Dedicated test accounts and data |

---

## Cross-browser Testing Rules

### Browser Coverage Strategy

| Priority | Browser | Coverage Reason |
|--------|--------|----------|
| P0 Must Test | Chrome | Largest market share |
| P1 Must Test | Safari | iOS/macOS users |
| P2 Recommended | Firefox | Third largest browser |
| P3 As Needed | Edge | Windows default |

---

## Execution Steps

### E2E Test Writing Process

**Step 1: Identify Test Scenarios**
1. Determine core user journeys (revenue critical paths, high-risk functions)
2. Filter scenarios not suitable for E2E (should be covered by unit/API tests)
3. Determine test priority

**Step 2: Design Page Objects**
1. Identify involved pages
2. Create Page Object class for each page
3. Encapsulate element locators and operation methods

**Step 3: Write Test Code**
1. Use Page Objects to organize tests
2. Use data-testid to locate elements
3. Use auto-wait (prohibit sleep)
4. Prepare test data through API

**Step 4: Configure Cross-browser/Device**
1. Configure multi-browser (Chrome, Safari, Firefox)
2. Configure multi-viewport (desktop, tablet, mobile)
3. Set failure screenshots and video recording

---

## Gate Check Validation Checklist

After writing E2E tests, **MUST** confirm the following checkpoints:

- [ ] Only cover core user journeys (not all functions)
- [ ] Use Page Object pattern
- [ ] Element location uses data-testid
- [ ] No fixed sleep, use auto-wait
- [ ] Test data prepared through API
- [ ] Data uses unique values (timestamp/UUID)
- [ ] Auto-screenshot on failure
- [ ] Single test < 60 seconds
- [ ] P0 browsers covered (Chrome + Safari)
- [ ] Key assertions verify user-visible results

---

## Best Practices Checklist

### Test Design

- [ ] Only cover core user journeys, don't pursue high coverage
- [ ] Use Page Object pattern to organize code
- [ ] Element location uses data-testid
- [ ] Test data independent, prepared through API

### Test Implementation

- [ ] Use auto-wait, prohibit fixed sleep
- [ ] Intercept and verify key requests
- [ ] Auto-screenshot on failure
- [ ] Control single test duration < 60s

### Test Maintenance

- [ ] Regularly check and fix Flaky Tests
- [ ] CI/CD integration, PR triggers smoke tests
- [ ] Regularly execute on multi-browsers
- [ ] Record failure videos for analysis
