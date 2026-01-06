# serviceUptimeMonitor
The project uses Java 21 - the latest LTS version of Java as of 12.2025
There are 3 Mricorservices:
1. monitoring-service: it stores what services should be monitored -> periodically checks their health ->
persists results -> detects status changes -> notifies the Alert Service when a service goes down or comes back up
2. 