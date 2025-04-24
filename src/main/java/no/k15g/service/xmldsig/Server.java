package no.k15g.service.xmldsig;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

public class Server extends AbstractVerticle {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Server());
    }

    @Override
    public void start() {
        var server = vertx.createHttpServer();
        server.requestHandler(this::handler);

        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.printf("Server is now listening on port %d\n", port);
            } else {
                System.out.printf("Failed to bind to port %d\n", port);
                System.exit(1);
            }
        });
    }

    private void handler(HttpServerRequest serverRequest) {
        if (serverRequest.method() == HttpMethod.GET) {
            fetchingHandler(serverRequest);
        } else if (serverRequest.method() == HttpMethod.POST) {
            validationHandler(serverRequest);
        } else {
            serverRequest.response().setStatusCode(405).end("Method Not Allowed");
        }
    }

    private void fetchingHandler(HttpServerRequest serverRequest) {
        var uri = serverRequest.getParam("uri");

        if (uri == null) {
            serverRequest.response().setStatusCode(400).end("Missing uri or method query parameter");
            return;
        }

        var request = new HttpGet(URI.create(uri));

        try (var response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                serverRequest.response().setStatusCode(response.getCode()).end("Failed to fetch resource");
                return;
            }

            // Extract body
            var content = response.getEntity().getContent().readAllBytes();

            // Validate the signature
            var certificate = SignatureValidator.validate(new ByteArrayInputStream(content));

            // Send to client
            serverRequest.response().setStatusCode(response.getCode());
            for (var header : response.getHeaders())
                serverRequest.response().putHeader(header.getName(), header.getValue());
            serverRequest.response().putHeader("X-Signing-Certificate", Base64.getEncoder().encodeToString(certificate.getEncoded()));
            serverRequest.response().send(new String(content));
        } catch (ParserConfigurationException e) {
            serverRequest.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "text/plain")
                    .end("Failed to parse XML: " + e.getMessage());
        } catch (IOException e) {
            serverRequest.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "text/plain")
                    .end("Failed to send request: " + e.getMessage());
        } catch (Exception e) {
            serverRequest.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "text/plain")
                    .end("Validation failed: " + e.getMessage());
        }
    }

    private void validationHandler(HttpServerRequest serverRequest) {
        serverRequest.body().onSuccess((body) -> {
            var content = body.getBytes();

            try {
                // Validate the signature
                var certificate = SignatureValidator.validate(new ByteArrayInputStream(content));

                serverRequest.response()
                        .setStatusCode(200)
                        .putHeader("X-Signing-Certificate", Base64.getEncoder().encodeToString(certificate.getEncoded()))
                        .end("Validation successful");
            } catch (Exception e) {
                serverRequest.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "text/plain")
                        .end("Validation failed: " + e.getMessage());
            }
        }).onFailure((e) -> {
            serverRequest.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "text/plain")
                    .end("Failed to read request body: " + e.getMessage());
        });
    }
}