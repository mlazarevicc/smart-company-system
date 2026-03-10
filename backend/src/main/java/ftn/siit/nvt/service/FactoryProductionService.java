package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.model.FactoryProductQuantity;
import ftn.siit.nvt.model.Product;
import ftn.siit.nvt.repository.FactoryProductQuantityRepository;
import ftn.siit.nvt.repository.FactoryRepository;
import ftn.siit.nvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryProductionService {

    private final FactoryRepository factoryRepository;
    private final ProductRepository productRepository;
    private final FactoryProductQuantityRepository productQuantityRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    public void recordProduction(Long factoryId, LocalDateTime timestamp,
                                 Long productId, Long quantity) {

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));
        Product product = productRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", factoryId));

        Point point = Point
                .measurement("factory_production")
                .addTag("factory_id", factoryId.toString())
                .addTag("product_id", productId.toString())
                .addTag("city", factory.getCity().getName())
                .addTag("country", factory.getCountry().getCode())
                .addField("quantity", quantity)
                .time(timestamp.atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.MS);
        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
            FactoryProductQuantity productQuantity = new FactoryProductQuantity();
            productQuantity.setFactory(factory);
            productQuantity.setProduct(product);
            productQuantity.setQuantity(quantity);
            productQuantityRepository.save(productQuantity);
        } catch (Exception e) {
            log.error("Failed to write production metrics to Influx for factory {}", factoryId, e);
        }
    }

    public void recordAvailability(Long factoryId, boolean isOnline,
                                   LocalDateTime timestamp, String reason) {

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

        Point point = Point
                .measurement("factory_availability")
                .addTag("factory_id", factoryId.toString())
                .addTag("city", factory.getCity().getName())
                .addTag("country", factory.getCountry().getCode())
                .addField("online", isOnline)
                .addField("reason", reason)
                .time(timestamp.atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.MS);

        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
        } catch (Exception e) {
            log.error("Failed to write availability metrics to Influx for factory {}", factoryId, e);
        }
    }
}