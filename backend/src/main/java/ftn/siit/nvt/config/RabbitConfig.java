package ftn.siit.nvt.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Factory Exchange
    @Bean
    public TopicExchange factoriesExchange() {
        return new TopicExchange("smart-manufacturing.factories", true, false);
    }

    // Factory Queues
    @Bean
    public Queue factoryProductionQueue() {
        return new Queue("factory-production-updates", true);
    }

    @Bean
    public Queue factoryHeartbeatQueue() {
        return new Queue("factory-heartbeat", true);
    }

    // Bindings
    @Bean
    public Binding productionBinding(Queue factoryProductionQueue, TopicExchange factoriesExchange) {
        return BindingBuilder.bind(factoryProductionQueue)
                .to(factoriesExchange)
                .with("factory.*.production");
    }

    @Bean
    public Binding heartbeatBinding(Queue factoryHeartbeatQueue, TopicExchange factoriesExchange) {
        return BindingBuilder.bind(factoryHeartbeatQueue)
                .to(factoriesExchange)
                .with("factory.*.heartbeat");
    }

    // ===== WAREHOUSE EXCHANGE =====

    @Bean
    public TopicExchange warehousesExchange() {
        return new TopicExchange("smart-manufacturing.warehouses", true, false);
    }

    // ===== WAREHOUSE QUEUES =====

    @Bean
    public Queue warehouseTemperatureQueue() {
        return new Queue("warehouse-temperature-updates", true);
    }

    @Bean
    public Queue warehouseHeartbeatQueue() {
        return new Queue("warehouse-heartbeat", true);
    }

    // ===== WAREHOUSE BINDINGS =====

    @Bean
    public Binding warehouseTemperatureBinding(
            Queue warehouseTemperatureQueue,
            TopicExchange warehousesExchange) {
        return BindingBuilder.bind(warehouseTemperatureQueue)
                .to(warehousesExchange)
                .with("warehouse.*.temperature");  // warehouse.123.temperature
    }

    @Bean
    public Binding warehouseHeartbeatBinding(
            Queue warehouseHeartbeatQueue,
            TopicExchange warehousesExchange) {
        return BindingBuilder.bind(warehouseHeartbeatQueue)
                .to(warehousesExchange)
                .with("warehouse.*.heartbeat");  // warehouse.123.heartbeat
    }

    // --- VEHICLES ---

    @Bean
    public TopicExchange vehiclesExchange() {
        return new TopicExchange("smart-manufacturing.vehicles", true, false);
    }

    @Bean
    public Queue vehicleDistanceQueue() {
        return new Queue("vehicle-distance-updates", true);
    }

    @Bean
    public Queue vehicleHeartbeatQueue() {
        return new Queue("vehicle-heartbeat", true);
    }

    @Bean
    public Binding distanceBinding(Queue vehicleDistanceQueue, TopicExchange vehiclesExchange) {
        return BindingBuilder.bind(vehicleDistanceQueue)
                .to(vehiclesExchange)
                .with("vehicle.*.distance");
    }

    @Bean
    public Binding vehicleHeartbeatBinding(Queue vehicleHeartbeatQueue, TopicExchange vehiclesExchange) {
        return BindingBuilder.bind(vehicleHeartbeatQueue)
                .to(vehiclesExchange)
                .with("vehicle.*.heartbeat");
    }

    // ===== ORDERS (EMAILS/INVOICES) =====

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange("smart-manufacturing.orders", true, false);
    }

    @Bean
    public Queue orderEmailQueue() {
        return new Queue("order-email-tasks", true);
    }

    @Bean
    public Binding orderEmailBinding(Queue orderEmailQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderEmailQueue)
                .to(ordersExchange)
                .with("order.created.email");
    }

}
