# Dashboard Service Tests

Tests use **JUnit 5**, **Mockito**, and a mocked `MonitoringServiceClient` (no real HTTP calls) configured via `application-test.yml`.

## How to run

```bash
# All tests
mvn test

# One specific class
mvn test -Dtest=DashboardServiceTest
```

## Test types

| Folder | What it tests |
|---|---|
| `service/` | Unit tests — overview aggregation logic, SSE emitter behaviour |
| `controller/` | Unit + integration tests — HTTP endpoints and exception handling |
| `exception/` | Unit tests — exception handler returns correct ProblemDetail responses |
| `client/` | Unit tests — MonitoringServiceClient calls the correct URIs |

## Patterns used

**Unit test** — mock dependencies with Mockito, verify behaviour:
```java
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private MonitoringServiceClient monitoringServiceClient;
    @InjectMocks private DashboardService dashboardService;

    @Test
    void getOverview_returnsCorrectCounts() {
        // Given
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(List.of(
                summaryUp("service-a"), summaryDown("service-b")
        ));

        // When
        DashboardOverviewDto overview = dashboardService.getOverview();

        // Then
        assertThat(overview.servicesUp()).isEqualTo(1);
        assertThat(overview.servicesDown()).isEqualTo(1);
    }
}
```

**Integration test** — starts the full Spring context, mocks only the external HTTP client:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DashboardControllerIntegrationTest {

    @MockitoBean
    private MonitoringServiceClient monitoringServiceClient;

    @Test
    void getStatuses_returnsCurrentStatuses() {
        when(monitoringServiceClient.getCurrentStatuses()).thenReturn(List.of(...));

        webTestClient.get()
                .uri("/api/dashboard/statuses")
                .exchange()
                .expectStatus().isOk();
    }
}
```
