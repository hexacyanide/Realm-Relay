package realmrelay.net;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public abstract class ListenSocket implements Runnable {
    private final SocketAddress endpoint;
    private ServerSocket serversocket;
    private Thread thread;

    public ListenSocket(int port) {
        this.endpoint = new InetSocketAddress(port);
    }

    public ListenSocket(String hostname, int port) {
        this.endpoint = new InetSocketAddress(hostname, port);
    }

    public boolean isClosed() {
        return this.serversocket == null || this.serversocket.isClosed();
    }

    public boolean start() {
        this.stop();

        try {
            this.serversocket = new ServerSocket();
            this.serversocket.bind(this.endpoint);
        } catch (Exception e) {
            this.stop();
            return false;
        }

        this.thread = new Thread(this);
        this.thread.start();
        return true;
    }

    public void stop() {
        this.thread = null;
        if (this.serversocket == null) return;

        try {
            this.serversocket.close();
            this.serversocket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!this.isClosed()) {
            try {
                final Socket socket = this.serversocket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketAccepted(socket);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void socketAccepted(Socket socket);

    @Override
    public String toString() {
        return "ListenSocket[endpoint=" + this.endpoint + "]";
    }
}
