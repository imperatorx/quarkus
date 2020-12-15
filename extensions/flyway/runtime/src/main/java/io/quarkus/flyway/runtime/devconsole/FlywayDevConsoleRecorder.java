package io.quarkus.flyway.runtime.devconsole;

import java.util.List;

import org.flywaydb.core.Flyway;

import io.quarkus.flyway.runtime.FlywayContainer;
import io.quarkus.flyway.runtime.FlywayContainerSupplier;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.devmode.devconsole.DevConsolePostHandler;
import io.quarkus.vertx.http.runtime.devmode.devconsole.FlashScopeUtil.FlashMessageStatus;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class FlywayDevConsoleRecorder {

    public Handler<RoutingContext> handler() {
        return new DevConsolePostHandler() {
            @Override
            protected void handlePost(RoutingContext event, MultiMap form) throws Exception {
                String datasource = form.get("datasource");
                String operation = form.get("operation");
                List<FlywayContainer> liquibaseContainers = new FlywayContainerSupplier().get();
                for (FlywayContainer flywayContainer : liquibaseContainers) {
                    if (flywayContainer.getDataSourceName().equals(datasource)) {
                        Flyway flyway = flywayContainer.getFlyway();
                        if ("clean".equals(operation)) {
                            flyway.clean();
                            flashMessage(event, "Database cleaned");
                            return;
                        } else if ("migrate".equals(operation)) {
                            flyway.migrate();
                            flashMessage(event, "Database migrated");
                            return;
                        } else {
                            flashMessage(event, "Invalid operation: " + operation, FlashMessageStatus.ERROR);
                            return;
                        }
                    }
                }
                flashMessage(event, "Data source not found: " + datasource, FlashMessageStatus.ERROR);
            }
        };
    }
}
