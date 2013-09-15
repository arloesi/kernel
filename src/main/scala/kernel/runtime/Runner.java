package kernel.runtime;

import kernel.network.Server;

public class Runner {
    private final Server server;

    public Runner(Server server) {
        this.server = server;
    }

    public void run() {
        server.start();
    }

    public static void main(String [] args) {
        new Runner(new Server(8080)).run();
    }
}
