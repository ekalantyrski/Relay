package com.relay;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by erickalantyrski on 2017-05-12.
 */

/*
This class is responsible for network communication. It has to find the server on the network, and setup a connection
 */

public class Network implements Runnable {

    private WifiManager wifiManager;
    private Socket socket;
    private DhcpInfo dhcp;
    private ObjectOutputStream out;
    private String password = "aIaZsoqSBk";
    private ObjectInputStream in;
    private SmsSender smsSender;
    private boolean connected;

    public Network(Context context, SmsSender smsSender) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        dhcp = wifiManager.getDhcpInfo();
        this.smsSender = smsSender;
        connected = false;
    }

    //Sends a contact over the network
    //Param is contact to sendInfo
    public void sendLocalMessage(final LocalMessage localMessage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.writeObject(localMessage);
                } catch (IOException e) {
                    Log.e("Output", "Cannot sendInfo object");
                }
            }
        }).start();

    }

    //This finds and connects to the desktop server
    public void connect()  {

        String[] addresses = getConnectableAddresses(); // gets all addresses on network


        for (int i = 0; i < addresses.length; i++) {
            try {
                InetAddress inet = InetAddress.getByName(addresses[i]);
                //InetAddress inet = InetAddress.getByName("192.168.0.105");

                socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(inet, 43567);
                //socket.bind(socketAddress);
                socket.connect(socketAddress, 100);

                System.out.println("Connection to: " + addresses[i]);
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); // checks if password is the same
                pw.println(password);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input = br.readLine();

                if (input.equals("true")) {
                    break;
                }
            } catch (UnknownHostException uhe) {
                Log.e("UNKNOWN HOST", "Unknown Host Exception");
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("Cannot connect", "Could not connect to: " + addresses[i]);

            }
        }
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); // creates objects for i/o once password is accepted
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Conntected to: " + socket.getInetAddress());
        } catch (IOException e) {

        }

    }


    @Override
    //starts the network thread
    public void run() {
        while(true) {
            connect();
            listenNetwork();
        }

    }
    //This method gets all connectable addresses by going through all combinations allowed by the subnetmask
    //Returns an array of all addresses that you can connect to.
    private String[] getConnectableAddresses() {
        ArrayList<String> addresses = new ArrayList<>();
        byte[] ip = BigInteger.valueOf(dhcp.ipAddress).toByteArray(); // gets local ip
        byte[] mask = BigInteger.valueOf(dhcp.netmask).toByteArray(); // gets netmask

        byte[] staticIP = new byte[4];
        for (int i = 0; i < 4; i++) {
            staticIP[i] = (byte) (ip[i] & mask[i]);
        }


        staticIP = flipArray(staticIP); // flip for easier byte->string conversion later
        mask = flipArray(mask);

        int[] ipOctet = new int[4];
        for (int i = 0; i < 4; i++) {
            ipOctet[i] = mask[i] & 0xFF;
        }

        byte[] values;
        byte[] dif;
        byte[] newIP = new byte[4];

        //this code generates all the allowed values by the netmask
        for (int m0 = (int) ipOctet[0]; m0 < 256; m0++) //once it hits 256, then all possibilities have been tried
        {
            for (int m1 = (int) ipOctet[1]; m1 < 256; m1++) {
                for (int m2 = (int) ipOctet[2]; m2 < 256; m2++) {
                    for (int m3 = (int) mask[3]; m3 < 256; m3++) {
                        //for everyvalue, the difference between the mask and the value is taken
                        //and difference is added to the static ip, staticip is then converted to string
                        //mask:     111000
                        //value:    111010
                        //dif:      000010

                        values = new byte[]{(byte) m0, (byte) m1, (byte) m2, (byte) m3};
                        dif = getDifference(mask, values);

                        for (int i = 0; i < 4; i++) {
                            newIP[i] = (byte) (staticIP[i] | dif[i]);
                        }

                        addresses.add(getIpFromByteArray(newIP));
                    }
                }
            }
        }
        return addresses.toArray(new String[addresses.size()]);
    }
    //Flips an array of bytes
    //Param is the array to flip
    //Returns flipped array
    private byte[] flipArray(byte[] array) {
        byte[] newArray = new byte[array.length];

        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = array[array.length - 1 - i];
        }
        return newArray;
    }

    //Gets the difference between two byte arrays
    //Param is the two byte arrays to test
    //Returns an array of bytes that contains only the difference
    private byte[] getDifference(byte[] mask, byte[] value) {
        byte[] dif = new byte[4];
        for (int i = 0; i < 4; i++) {
            dif[i] = (byte) (mask[i] ^ value[i]);
        }
        return dif;
    }


    //Gets a String ip from an array of bytes
    //Param is the byte array
    //Returns a IP string
    private String getIpFromByteArray(byte[] array) {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 4; i++) {
            sb.append(((int) (array[i]) & 0xFF) + ".");
            if (i == 3)
                sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();

    }


    //This method listens the network for an input from desktop
    private void listenNetwork()
    {
        LocalMessage input = null;
        boolean running = true;
        while(running)
        {
            try
            {
                input = (LocalMessage) in.readObject(); // input
                if(input.getMessageType() == MessageType.SMS) {
                    if (input != null) // input is null if connection is closed
                    {
                        if (input.getMessageType() == MessageType.SMS) {
                            smsSender.sendSms((SmsMessage) input); //sendInfo input over sms
                        }
                    } else {
                        running = false;
                    }
                }
            }catch(ClassNotFoundException cnfe)
            {
                System.out.println("Something is wrong with class");
                running = false;
                break;
            }
            catch(IOException e)
            {
                System.out.println("Connection Lost.");
                running = false;
                break;
            }
        }
        connected = false; // if the loop ends, then connection ended somehow
    }

    /**
     * Returns connection status of the network.
     * @return
     */
    public boolean isConnected()
    {
        return connected;
    }
}
