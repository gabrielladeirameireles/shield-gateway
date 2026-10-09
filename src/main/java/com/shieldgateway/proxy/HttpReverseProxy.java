package com.shieldgateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;

@Component
public class HttpReverseProxy {

    private final HttpClient httpClient;

    public HttpReverseProxy() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request, String targetUrl, byte[] body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(10));

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpRequest.BodyPublisher bodyPublisher = (body != null && body.length > 0)
                    ? HttpRequest.BodyPublishers.ofByteArray(body)
                    : HttpRequest.BodyPublishers.noBody();

            builder.method(method.name(), bodyPublisher);

            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    if (!isRestrictedHeader(name)) {
                        Enumeration<String> values = request.getHeaders(name);
                        while (values.hasMoreElements()) {
                            builder.header(name, values.nextElement());
                        }
                    }
                }
            }

            HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().map().forEach((key, values) -> {
                if (!key.equalsIgnoreCase("transfer-encoding")) {
                    responseHeaders.addAll(key, values);
                }
            });

            return new ResponseEntity<>(response.body(), responseHeaders, HttpStatus.valueOf(response.statusCode()));

        } catch (ConnectException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 502, \"error\": \"Bad Gateway\", \"message\": \"Não foi possível conectar ao serviço de destino.\"}".getBytes());
        } catch (SocketTimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 504, \"error\": \"Gateway Timeout\", \"message\": \"O serviço de destino demorou para responder.\"}".getBytes());
        } catch (IOException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body("{\"status\": 504, \"error\": \"Gateway Timeout\", \"message\": \"Tempo limite excedido na comunicação com o destino.\"}".getBytes());
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 502, \"error\": \"Bad Gateway\", \"message\": \"Erro de comunicação com o serviço de destino.\"}".getBytes());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Requisição interrompida.\"}".getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 502, \"error\": \"Bad Gateway\", \"message\": \"Falha de roteamento no gateway.\"}".getBytes());
        }
    }

    private boolean isRestrictedHeader(String headerName) {
        String name = headerName.toLowerCase();
        return name.equals("host") || name.equals("content-length") || name.equals("connection");
    }
}
