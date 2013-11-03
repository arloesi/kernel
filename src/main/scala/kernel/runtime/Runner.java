package kernel.runtime;

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
        new Runner(new Server(8080)).run();
    }
}
