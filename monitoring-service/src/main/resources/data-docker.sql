-- ============================
-- INSERT MONITORED SERVICES
-- ============================
INSERT INTO monitored_service (
    id, name, url, check_interval, enabled, created_at
) VALUES
      (
          '11111111-1111-1111-1111-111111111111',
          'User Auth Service',
          'http://monitoring-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '22222222-2222-2222-2222-222222222222',
          'Product Catalog Service',
          'http://dashboard-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '33333333-3333-3333-3333-333333333333',
          'Notification Service',
          'http://alert-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '44444444-4444-4444-4444-444444444444',
          'Payment Gateway',
          'http://demo-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '55555555-5555-5555-5555-555555555555',
          'Order Processing Service',
          'http://order-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      ),
      (
          '66666666-6666-6666-6666-666666666666',
          'Inventory Service',
          'http://inventory-service:8080/actuator/health',
          30,
          TRUE,
          CURRENT_TIMESTAMP
      )
ON CONFLICT (id) DO NOTHING;
