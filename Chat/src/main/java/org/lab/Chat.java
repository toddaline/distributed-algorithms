package main.java.org.lab;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.jmx.JmxConfigurator;
import org.jgroups.util.Util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Chat extends ReceiverAdapter {

    private static final String DOMAIN = "my_channel";
    private static final String CLUSTER_NAME = "Warlock-cluster";
    private static final String QUIT = "quit";
    private static final String CONFIG_PATH = "./config/config.xml";

    private JChannel channel;

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        System.out.printf("[%s]: %s\n", msg.getSrc(), msg.getObject());
    }


    private void start(String config, String name) throws Exception {
        channel = new JChannel(config).name(name).receiver(this);
        channel.connect(CLUSTER_NAME);
        JmxConfigurator.registerChannel(channel, Util.getMBeanServer(), DOMAIN, channel.getClusterName(), true);
        eventLoop();
        channel.close();
    }

    private void eventLoop() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
                System.out.flush();
                String line = in.readLine();
                if (line.toLowerCase().startsWith(QUIT)) {
                    break;
                }
                Message msg = new Message(null, line);
                channel.send(msg);
        }
    }


    public static void main(String[] args) throws Exception {
        String name = "user";
        System.out.print("Enter your name: ");
        Reader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            name = ((BufferedReader) reader).readLine();
        } catch (IOException e) {
        }
        new Chat().start(CONFIG_PATH, name);
    }
}
