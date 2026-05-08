# Monitoring Service Tests

Tests use **JUnit 5**, **Mockito**, and **H2** (in-memory database) configured via `application-test.yml`.

## How to run

```bash
# All tests
mvn test

# One specific class
mvn test -Dtest=MonitoringServiceTest
```

## Test types

| Folder | What it tests |
|---|---|
| `service/` | Unit tests — service layer logic, mocked dependencies |
| `client/` | Unit tests — AlertServiceClient HTTP communication |
| `mapper/` | Unit tests — entity ↔ DTO conversions |
| `scheduler/` | Unit tests — scheduler calls the right service method |
| `controller/` | Integration tests — full HTTP request/response cycle |
| `repository/` | Integration tests — JPA queries against H2 |

## Utilities

- **`TestConstants`** — shared string/int constants (URLs, names, intervals) used across tests
- **`TestConfig`** — Spring `@TestConfiguration` that registers a `WebClient` bean for integration tests

## Patterns used

**Unit test** — mock dependencies with Mockito, verify behaviour:
```java
@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock private MonitoredServiceRepository serviceRepository;
    @InjectMocks private MonitoringService monitoringService;

    @Test
    void checkAllServices_shouldSaveUpStatus_whenServiceRespondsSuccessfully() {
        // Given
        when(serviceRepository.findByEnabledTrue()).thenReturn(List.of(testService));

        // When
        monitoringService.checkAllServices();

        // Then
        verify(statusRepository).save(any(ServiceStatus.class));
    }
}
```

**Integration test** — starts the full Spring context with H2, sends real HTTP requests:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MonitoredServiceControllerIntegrationTest {

    @Test
    void create_returnsCreatedService() {
        webTestClient.post()
                .uri("/api/monitoring/services")
                .bodyValue(new MonitoredServiceRequestDto("My API", "http://example.com", 30))
                .exchange()
                .expectStatus().isCreated();
    }
}
```
