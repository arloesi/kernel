package kernel.runtime;

import kernel.handler.Server;

public class Runner {
    private final Server server;

    Runner(Server server) {
        this.server = server;
    }

    void run() {
        server.run();
    }

    public static void main(String [] args) {
        new Runner(new Server(8080)).run();
    }
}
