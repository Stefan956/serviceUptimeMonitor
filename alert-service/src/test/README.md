# Alert Service Tests

Tests use **JUnit 5**, **Mockito**, and **H2** (in-memory database) configured via `application-test.yml`.

## How to run

```bash
# All tests
mvn test

# One specific class
mvn test -Dtest=AlertProcessorServiceTest
```

## Test types

| Folder | What it tests |
|---|---|
| `service/` | Unit tests — alert processing, cooldown logic, email and console notification |
| `controller/` | Unit + integration tests — HTTP endpoints for ingesting and querying alerts |
| `exception/` | Unit tests — exception handler returns correct ProblemDetail responses |
| `repository/` | Integration tests — JPA queries against H2 |

## Patterns used

**Unit test** — mock dependencies with Mockito, verify behaviour:
```java
@ExtendWith(MockitoExtension.class)
class AlertProcessorServiceTest {

    @Mock private AlertRepository alertRepository;
    @InjectMocks private AlertProcessorService alertProcessorService;

    @Test
    void processStatusChange_shouldSkipNotification_whenInCooldown() {
        // Given
        when(alertRepository.findTopByServiceNameAndNewStatusOrderByNotifiedAtDesc(...))
                .thenReturn(Optional.of(recentAlert));

        // When
        alertProcessorService.processStatusChange(request);

        // Then
        verify(alertRepository, never()).save(any());
    }
}
```

**Integration test** — starts the full Spring context with H2, sends real HTTP requests:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AlertControllerIntegrationTest {

    @Test
    void postStatusChange_returnsAccepted() {
        webTestClient.post()
                .uri("/api/alerts/status-change")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted();
    }
}
```
