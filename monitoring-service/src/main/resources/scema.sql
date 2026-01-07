-- ============================
-- MONITORED_SERVICE TABLE
-- ============================
CREATE TABLE monitored_service (
                                   id UUID PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL,
                                   url VARCHAR(500) NOT NULL,
                                   check_interval INT NOT NULL,
                                   enabled BOOLEAN NOT NULL,
                                   created_at TIMESTAMP,
                                   updated_at TIMESTAMP
);

-- ============================
-- SERVICE_STATUS TABLE
-- ============================
CREATE TABLE service_status (
                                id UUID PRIMARY KEY,
                                monitored_service_id UUID NOT NULL,
                                status VARCHAR(10) NOT NULL,
                                http_status_code INT NOT NULL,
                                response_time BIGINT,
                                checked_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP,
                                updated_at TIMESTAMP,

                                CONSTRAINT fk_service_status_service
                                    FOREIGN KEY (monitored_service_id)
                                        REFERENCES monitored_service(id)
);

-- ============================
-- INSERT MONITORED SERVICES
-- ============================
INSERT INTO monitored_service (
    id, name, url, check_interval, enabled, created_at
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
    id,
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
      );
