package realmrelay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import realmrelay.net.ListenSocket;
import realmrelay.packets.Packet;

public final class RealmRelay {
    public static final RealmRelay instance = new RealmRelay();

    // #settings
    public String listenHost = "localhost";
    public int listenPort = 2050;

    public boolean bUseProxy = false;
    public String proxyHost = "socks4or5.someproxy.net";
    public int proxyPort = 1080;

    public String remoteHost = "54.226.214.216";
    public int remotePort = 2050;

    public String key0 = "311F80691451C71B09A13A2A6E";
    public String key1 = "72C5583CAFB6818995CBD74B80";
    // #settings end

    private final ListenSocket listenSocket;
    private final List<User> users = new ArrayList<User>();
    private final List<User> newUsers = new Vector<User>();
    private final Map<Integer, InetSocketAddress> gameIdSocketAddressMap = new Hashtable<Integer, InetSocketAddress>();
    private final Map<String, Object> globalVarMap = new Hashtable<String, Object>();

    private RealmRelay() {
        Properties p = new Properties();
        p.setProperty("listenHost", this.listenHost);
        p.setProperty("listenPort", String.valueOf(this.listenPort));
        p.setProperty("bUseProxy", String.valueOf(this.bUseProxy));
        p.setProperty("proxyHost", this.proxyHost);
        p.setProperty("proxyPort", String.valueOf(this.proxyPort));
        p.setProperty("remoteHost", this.remoteHost);
        p.setProperty("remotePort", String.valueOf(this.remotePort));
        p.setProperty("key0", this.key0);
        p.setProperty("key1", this.key1);

        File file = new File("settings.properties");
        if (!file.isFile()) {
            try {
                OutputStream out = new FileOutputStream(file);
                p.store(out, null);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        p = new Properties(p);
        try {
            InputStream in = new FileInputStream(file);
            p.load(in);
            in.close();
            this.listenHost = p.getProperty("listenHost");
            this.listenPort = Integer.parseInt(p.getProperty("listenPort"));
            this.bUseProxy = Boolean.parseBoolean(p.getProperty("bUseProxy"));
            this.proxyHost = p.getProperty("proxyHost");
            this.proxyPort = Integer.parseInt(p.getProperty("proxyPort"));
            this.remoteHost = p.getProperty("remoteHost");
            this.remotePort = Integer.parseInt(p.getProperty("remotePort"));
            this.key0 = p.getProperty("key0");
            this.key1 = p.getProperty("key1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.listenSocket = new ListenSocket(this.listenHost, this.listenPort) {
            @Override
            public void socketAccepted(Socket localSocket) {
                try {
                    User user = new User(localSocket);
                    RealmRelay.instance.newUsers.add(user);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        localSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * Prints a timestamp-prefixed error to standard error.
     *
     * @param message
     */
    public static void error(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("MDT"));
        String timestamp = sdf.format(new Date());
        String raw = timestamp + " " + message;
        System.err.println(raw);
    }

    /**
     * Prints a timestamp-prefixed message to standard output.
     * 
     * @param message
     */
    public static void echo(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("MDT"));
        String timestamp = sdf.format(new Date());
        String raw = timestamp + " " + message;
        System.out.println(raw);
    }

    public Object getGlobal(String var) {
        return this.globalVarMap.get(var);
    }

    public InetSocketAddress getSocketAddress(int gameId) {
        InetSocketAddress socketAddress = this.gameIdSocketAddressMap.get(gameId);
        if (socketAddress != null)return socketAddress;
        return new InetSocketAddress(this.remoteHost, this.remotePort);
    }

    public void setGlobal(String var, Object value) {
        this.globalVarMap.put(var, value);
    }

    public void setSocketAddress(int gameId, String host, int port) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        this.gameIdSocketAddressMap.put(gameId, socketAddress);
    }

    public void removeGlobal(String var) {
        this.globalVarMap.remove(var);
    }

    public static void main(String[] args) {
        try {
            XmlParser.parseXMLData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Packet.init();

        if (!RealmRelay.instance.listenSocket.start()) {
            RealmRelay.error("Failed to start relay server. Is another instance still running?");
            return;
        }

        RealmRelay.echo("Realm Relay server started successfully.");

        while (!RealmRelay.instance.listenSocket.isClosed()) {
            while (!RealmRelay.instance.newUsers.isEmpty()) {
                User user = RealmRelay.instance.newUsers.remove(0);
                RealmRelay.instance.users.add(user);
                RealmRelay.echo("Connected " + user.localSocket);
                user.scriptManager.trigger("onEnable");
            }

            int cores = Runtime.getRuntime().availableProcessors();
            Thread[] threads = new Thread[cores];
            int core = 0;
            Iterator<User> i = RealmRelay.instance.users.iterator();
            while (i.hasNext()) {
                final User user = i.next();
                if (user.localSocket.isClosed()) {
                    i.remove();
                    continue;
                }
                if (threads[core] != null) {
                    try {
                        threads[core].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                (threads[core] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        user.process();
                    }
                })).start();
                core = (core + 1) % cores;
            }
            for (Thread thread : threads) {
                if (thread == null) continue;

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Thread.yield();
        }

        // if the server is no longer listening, kick all connected users
        Iterator<User> i = RealmRelay.instance.users.iterator();
        while (i.hasNext()) {
            User user = i.next();
            user.kick();
        }
    }
}
