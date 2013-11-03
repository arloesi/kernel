package kernel.runtime;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.vertx.java.core.*;
import org.vertx.java.core.http.*;

public class Runner {
    private final Vertx vertx;
    private final int port;
    private final HttpServer server;

    public Runner(Vertx vertx, int port, HttpServer server) {
        this.vertx = vertx;
        this.port = port;
        this.server = server;
    }

    public void run() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Runner.this.notify();
            }
        });

        server.listen(port);

        synchronized(this) {
            this.wait();
        }

        vertx.stop();
    }

    public static void main(String [] args) throws Exception {
        final Module module = new Module(8080, "dist");
        final Injector injector = Guice.createInjector(module);
        injector.getInstance(Runner.class).run();
    }
}
