package org.jenkinsci.plugins.testinprogress;

import hudson.remoting.*;
import hudson.remoting.forward.Forwarder;
import hudson.remoting.forward.ListeningPort;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

/**
 * Port forwarder over a remote channel.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.315
 */
public class PortForwarder extends Thread implements Closeable, ListeningPort, Serializable {
    private final Forwarder forwarder;
    private final transient ServerSocket socket;

    public PortForwarder(int localPort, Forwarder forwarder) throws IOException {
        super(String.format("Port forwarder %d",localPort));
        this.forwarder = forwarder;
        this.socket = new ServerSocket(localPort);
        // mark as a daemon thread by default.
        // the caller can explicitly cancel this by doing "setDaemon(false)"
        setDaemon(true);
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            try {
                while(true) {
                    final Socket s = socket.accept();
                    new Thread("Port forwarding session from "+s.getRemoteSocketAddress()) {
                        public void run() {
                            try {
                                final OutputStream out = forwarder.connect(new RemoteOutputStream(SocketChannelStream.out(s)));
                                new CopyThread("Copier for "+s.getRemoteSocketAddress(),
                                        SocketChannelStream.in(s), out).start();
                            } catch (IOException e) {
                                // this happens if the socket connection is terminated abruptly.
                                LOGGER.log(FINE,"Port forwarding session was shut down abnormally",e);
                            }
                        }
                    }.start();
                }
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(FINE,"Port forwarding was shut down abnormally",e);
        }
    }

    /**
     * Shuts down this port forwarder.
     */
    public void close() throws IOException {
        interrupt();
        socket.close();
    }

    /**
     * Starts a {@link hudson.remoting.forward.PortForwarder} accepting remotely at the given channel,
     * which connects by using the given connector.
     *
     * @return
     *      A {@link Closeable} that can be used to shut the port forwarding down.
     */
    public static ListeningPort create(VirtualChannel ch, final int acceptingPort, Forwarder forwarder) throws IOException, InterruptedException {
        // need a remotable reference
        final Forwarder proxy = ch.export(Forwarder.class, forwarder);

        return ch.call(new Callable<ListeningPort,IOException>() {
            public ListeningPort call() throws IOException {
                hudson.remoting.forward.PortForwarder t = new hudson.remoting.forward.PortForwarder(acceptingPort, proxy);
                t.start();
                return Channel.current().export(ListeningPort.class,t);
            }

            @Override
            public void checkRoles(RoleChecker checker) throws SecurityException {
                checker.check(this,ROLE);
            }
        });
    }

    private static final Logger LOGGER = Logger.getLogger(hudson.remoting.forward.PortForwarder.class.getName());

    /**
     * Role that's willing to listen on a socket and forward that to the other side.
     */
    public static final Role ROLE = new Role(hudson.remoting.forward.PortForwarder.class);
}
