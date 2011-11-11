package org.ubif.goldfish.net;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.ConnectException;

public class LineSocket{
    private String host;
    private int port;
    private Socket sock;
    private BufferedWriter bWriter;
    private BufferedReader bReader;
    private InputStreamReader iReader;
    private LineSocketEventHandler handler;
    private boolean closer = false;

    public LineSocket(String host, int port){
        this.host = host;
        this.port = port;
    }

    public boolean isConnected(){
        return sock.isConnected();
    }
    
    public boolean connect(){
        this.closer = false;
        try{
            this.sock = new Socket(host, port);
            this.bWriter = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));
            this.iReader = new InputStreamReader(this.sock.getInputStream());
            this.bReader = new BufferedReader(this.iReader);
        }
        catch(ConnectException ex){
            if(handler != null) handler.onClose();
            return false;
        }
        catch(Exception ex){
            this.close();
            if(handler != null) handler.onClose();
            return false;
        }
        final LineSocket that = this;
        new Thread(){
            public void run(){
                while(!closer){
                    try{
                        String line = bReader.readLine();
                        if(line != null){
                            if(handler != null) handler.onMessage(line);
                        }
                        Thread.sleep(500);
                    }
                    catch(SocketException ex){
                        that.close();
                    }
                    catch(IOException ex){
                        that.close();
                    }
                    catch(Exception ex){
                        that.close();
                    }
                }
            }
        }.start();
        if(handler != null) handler.onOpen();
        return true;
    }

    public void close(){
        try{
            closer = true;
            bReader.close();
            bWriter.close();
            iReader.close();
            sock.close();
            if(handler != null) handler.onClose();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public boolean send(String line){
        try{
            bWriter.write(line);
            bWriter.flush();
        }
        catch(Exception ex){
            this.close();
            if(handler != null) handler.onClose();
            return false;
        }
        return true;
    }

    public void addEventHandler(LineSocketEventHandler handler){
        this.handler = handler;
    }

}