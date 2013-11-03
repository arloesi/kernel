package kernel.runtime;

import com.google.inject.Guice;
import com.google.inject.Injector;

import kernel.network.Server;

public class Runner {
    private final Server server;

    public Runner(Server server) {
        this.server = server;
    }

    public void run() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Runner.this.notify();
            }
        });

        server.start();

        synchronized(this) {
            this.wait();
        }

        server.stop();
    }

    public static void main(String [] args) throws Exception {
        final Module module = new Module(8080);
        final Injector injector = Guice.createInjector(module);
        new Runner(injector.getInstance(Server.class)).run();
    }
}
