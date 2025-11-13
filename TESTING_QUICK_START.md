# Testing Quick Start Guide

## 🚀 Quick Commands

### Run All Tests
```bash
# Run all unit tests (fast)
./gradlew test

# Run all instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run everything including lint
./gradlew check
```

### Run Specific Tests
```bash
# Run a specific test class
./gradlew test --tests GameServiceTest

# Run a specific test method
./gradlew test --tests GameServiceTest.createGame_with_valid_title_creates_game_successfully

# Run tests matching a pattern
./gradlew test --tests "*ServiceTest"
```

### Generate Coverage Reports
```bash
# Generate HTML coverage report
./gradlew koverHtmlReport

# View report (after running above command)
open app/build/reports/kover/html/index.html
```

### Run Tests with Logging
```bash
# Verbose output
./gradlew test --info

# Debug output
./gradlew test --debug

# Show test output
./gradlew test --console=verbose
```

## 📊 Test Categories

| Test Type | Command | Time | Prerequisites |
|-----------|---------|------|--------------|
| Unit Tests | `./gradlew test` | ~30s | None |
| UI Tests | `./gradlew connectedAndroidTest` | ~5min | Emulator/Device |
| Lint | `./gradlew lint` | ~1min | None |
| Coverage | `./gradlew koverHtmlReport` | ~40s | Run tests first |

## 🎯 Coverage Targets

- **Minimum Overall**: 60%
- **Services**: 80%
- **ViewModels**: 70%
- **Extensions**: 90%

## 📝 Current Test Count

- ✅ **130+ Unit Tests**
- ✅ **15+ UI Tests**
- ✅ **12+ Integration Tests**
- ✅ **Total: 157+ Tests**

## 🐛 Troubleshooting

### Issue: Tests not running
**Solution**: Clean and rebuild
```bash
./gradlew clean test
```

### Issue: Instrumented tests failing
**Solution**: Check emulator is running
```bash
adb devices
```

### Issue: Coverage report empty
**Solution**: Run tests before generating report
```bash
./gradlew test koverHtmlReport
```

## 📱 Android Studio

### Run Tests
1. Right-click on test class or method
2. Select "Run 'TestName'"

### View Coverage
1. Right-click on test class
2. Select "Run 'TestName' with Coverage"
3. View coverage in Coverage window

### Generate Report
1. Run → Edit Configurations
2. Add Gradle task: `koverHtmlReport`
3. Run the configuration

## 🔄 CI/CD Integration

Add to `.github/workflows/test.yml`:

```yaml
- name: Run Unit Tests
  run: ./gradlew test

- name: Upload Test Results
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: app/build/test-results/
```

## 📚 More Information

See [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) for detailed information about:
- Test structure and organization
- Writing new tests
- Best practices
- Detailed coverage reports

---

**Need Help?** Check the [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) or contact the development team.
