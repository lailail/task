package com.example.ticket.gateway.route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 网关路由配置测试。
 * 用于验证关闭 Nacos 服务发现时，网关仍可通过显式配置的本地直连地址完成联调转发。
 */
@SpringBootTest(
        properties = {
                "TICKET_NACOS_DISCOVERY_ENABLED=false",
                "TICKET_NACOS_CONFIG_ENABLED=false",
                "TICKET_GATEWAY_USER_SERVICE_URI=http://127.0.0.1:8081",
                "TICKET_GATEWAY_TICKET_SERVICE_URI=http://127.0.0.1:8082",
                "TICKET_GATEWAY_SECKILL_SERVICE_URI=http://127.0.0.1:8083",
                "TICKET_GATEWAY_ORDER_SERVICE_URI=http://127.0.0.1:8084",
                "TICKET_GATEWAY_PAYMENT_SERVICE_URI=http://127.0.0.1:8086",
                "TICKET_GATEWAY_JOB_SERVICE_URI=http://127.0.0.1:8085"
        }
)
class GatewayRouteConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    /**
     * 关闭注册发现后，网关应优先采用显式配置的本地直连 URI。
     */
    @Test
    void should_use_direct_route_uris_when_local_integration_mode_is_enabled() {
        Map<String, URI> routeUriById = routeLocator.getRoutes()
                .toStream()
                .collect(Collectors.toMap(Route::getId, Route::getUri));

        assertEquals(URI.create("http://127.0.0.1:8081"), routeUriById.get("user-service-route"));
        assertEquals(URI.create("http://127.0.0.1:8082"), routeUriById.get("ticket-service-route"));
        assertEquals(URI.create("http://127.0.0.1:8083"), routeUriById.get("seckill-service-route"));
        assertEquals(URI.create("http://127.0.0.1:8084"), routeUriById.get("order-service-route"));
        assertEquals(URI.create("http://127.0.0.1:8086"), routeUriById.get("payment-service-route"));
        assertEquals(URI.create("http://127.0.0.1:8085"), routeUriById.get("job-service-route"));
    }
}
