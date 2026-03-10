package ftn.siit.nvt.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {

    @Bean
    public InfluxDBClient influxDBClient(
            @Value("${influxdb.url}") String url,
            @Value("${influxdb.token}") String token,
            @Value("${influxdb.org}") String influxOrg) {

        return InfluxDBClientFactory.create(url, token.toCharArray(), influxOrg);
    }
}
