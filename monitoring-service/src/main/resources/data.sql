-- ============================
-- INSERT MONITORED SERVICES
-- ============================
INSERT INTO monitored_service (
    service_id, name, url, check_interval, enabled, created_at
) VALUES
      (
          '11111111-1111-1111-1111-111111111111',
          'User Service',
          'http://localhost:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '22222222-2222-2222-2222-222222222222',
          'Order Service',
          'http://localhost:8081/actuator/health',
          60,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '33333333-3333-3333-3333-333333333333',
          'Payment Service',
          'http://localhost:8082/actuator/health',
          45,
          FALSE,
          CURRENT_TIMESTAMP
      );

-- ============================
-- INSERT SERVICE STATUSES
-- ============================
INSERT INTO service_status (
    service_id,
    monitored_service_id,
    status,
    http_status_code,
    response_time,
    checked_at,
    created_at
) VALUES
      (
          'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
          '11111111-1111-1111-1111-111111111111',
          'UP',
          200,
          120,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          '22222222-2222-2222-2222-222222222222',
          'DOWN',
          503,
          0,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          'cccccccc-cccc-cccc-cccc-cccccccccccc',
          '11111111-1111-1111-1111-111111111111',
          'UP',
          200,
          95,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          'dddddddd-dddd-dddd-dddd-dddddddddddd',
          '33333333-3333-3333-3333-333333333333',
          'UP',
          207,
          350,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
          '22222222-2222-2222-2222-222222222222',
          'UP',
          200,
          140,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          'ffffffff-ffff-ffff-ffff-ffffffffffff',
          '11111111-1111-1111-1111-111111111111',
          'DOWN',
          404,
          0,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          '99999999-9999-9999-9999-999999999999',
          '33333333-3333-3333-3333-333333333333',
          'DOWN',
          403,
          0,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      ),
      (
          '12121212-1212-1212-1212-121212121212',
          '22222222-2222-2222-2222-222222222222',
          'UP',
          200,
          88,
          CURRENT_TIMESTAMP,
          CURRENT_TIMESTAMP
      );
