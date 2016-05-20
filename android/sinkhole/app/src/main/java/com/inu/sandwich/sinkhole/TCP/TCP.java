package com.inu.sandwich.sinkhole.TCP;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import com.inu.sandwich.sinkhole.Utils.PersonData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by 0xFF00FF00 on 2016-04-01.
 */
public class TCP {

    private boolean streaming = false;

    public class ClientInfo{
        public Socket clientSocket;
        public DataInputStream readBuf;
        public PrintWriter writeBuf;

        public ClientInfo(Socket _client){
            try {
                clientSocket = _client;
                readBuf = new DataInputStream(new BufferedInputStream(_client.getInputStream()));
               // String str = in.readLine();
                writeBuf = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_client.getOutputStream())), true);
                //out.println("Server Received " + str);
            } catch (Exception e) {
            }
        }

        public void close(){
            if(clientSocket != null){
                try {
                    clientSocket.close();
                    readBuf.close();
                    writeBuf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static final int PORT = 27015;
    private static String TAG = "TCP";

    private ClientInfo clientInfo;

    private Thread ServerThread;
    private Thread ReadThread;

    private Handler handler = null;

    public void setHandler(Handler _handler){
        handler = _handler;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i(TAG, "***** IP=" + ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        Log.i(TAG, "***** IP=");
        return null;
    }

    public File getTempFile( File cachdir, String fileName){
        File file=null;
        try{
            file = File.createTempFile( fileName, ".bmp", cachdir);
        }catch( IOException e){

        }
        return file;
    }

    public void stopDrone(){
        streaming = false;
    }

    public void startDrone(){
        streaming = true;
        int count = 0;
        char[] buf = {0x42, 0x4D, 0x38, 0x4, 0x4, 0x0, 0x0, 0x0, 0x0, 0x0, 0x36, 0x4, 0x0, 0x0, 0x28, 0x0,
                0x0, 0x0, 0x0, 0x2, 0x0, 0x0, 0x0, 0x2, 0x0, 0x0, 0x1, 0x0, 0x8, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x2, 0x0, 0x4, 0x0, 0xC2, 0x0E, 0x0, 0x0, 0xC2, 0x0E, 0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, 0x1, 0x1, 0x0, 0x2, 0x2,
                0x2, 0x0, 0x3, 0x3, 0x3, 0x0, 0x4, 0x4, 0x4, 0x0, 0x5, 0x5, 0x5, 0x0, 0x6, 0x6,
                0x6, 0x0, 0x7, 0x7, 0x7, 0x0, 0x8, 0x8, 0x8, 0x0, 0x9, 0x9, 0x9, 0x0, 0x0A, 0x0A,
                0x0A, 0x0, 0x0B, 0x0B, 0x0B, 0x0, 0x0C, 0x0C, 0x0C, 0x0, 0x0D, 0x0D, 0x0D, 0x0, 0x0E, 0x0E,
                0x0E, 0x0, 0x0F, 0x0F, 0x0F, 0x0, 0x10, 0x10, 0x10, 0x0, 0x11, 0x11, 0x11, 0x0, 0x12, 0x12,
                0x12, 0x0, 0x13, 0x13, 0x13, 0x0, 0x14, 0x14, 0x14, 0x0, 0x15, 0x15, 0x15, 0x0, 0x16, 0x16,
                0x16, 0x0, 0x17, 0x17, 0x17, 0x0, 0x18, 0x18, 0x18, 0x0, 0x19, 0x19, 0x19, 0x0, 0x1A, 0x1A,
                0x1A, 0x0, 0x1B, 0x1B, 0x1B, 0x0, 0x1C, 0x1C, 0x1C, 0x0, 0x1D, 0x1D, 0x1D, 0x0, 0x1E, 0x1E,
                0x1E, 0x0, 0x1F, 0x1F, 0x1F, 0x0, 0x20, 0x20, 0x20, 0x0, 0x21, 0x21, 0x21, 0x0, 0x22, 0x22,
                0x22, 0x0, 0x23, 0x23, 0x23, 0x0, 0x24, 0x24, 0x24, 0x0, 0x25, 0x25, 0x25, 0x0, 0x26, 0x26,
                0x26, 0x0, 0x27, 0x27, 0x27, 0x0, 0x28, 0x28, 0x28, 0x0, 0x29, 0x29, 0x29, 0x0, 0x2A, 0x2A,
                0x2A, 0x0, 0x2B, 0x2B, 0x2B, 0x0, 0x2C, 0x2C, 0x2C, 0x0, 0x2D, 0x2D, 0x2D, 0x0, 0x2E, 0x2E,
                0x2E, 0x0, 0x2F, 0x2F, 0x2F, 0x0, 0x30, 0x30, 0x30, 0x0, 0x31, 0x31, 0x31, 0x0, 0x32, 0x32,
                0x32, 0x0, 0x33, 0x33, 0x33, 0x0, 0x34, 0x34, 0x34, 0x0, 0x35, 0x35, 0x35, 0x0, 0x36, 0x36,
                0x36, 0x0, 0x37, 0x37, 0x37, 0x0, 0x38, 0x38, 0x38, 0x0, 0x39, 0x39, 0x39, 0x0, 0x3A, 0x3A,
                0x3A, 0x0, 0x3B, 0x3B, 0x3B, 0x0, 0x3C, 0x3C, 0x3C, 0x0, 0x3D, 0x3D, 0x3D, 0x0, 0x3E, 0x3E,
                0x3E, 0x0, 0x3F, 0x3F, 0x3F, 0x0, 0x40, 0x40, 0x40, 0x0, 0x41, 0x41, 0x41, 0x0, 0x42, 0x42,
                0x42, 0x0, 0x43, 0x43, 0x43, 0x0, 0x44, 0x44, 0x44, 0x0, 0x45, 0x45, 0x45, 0x0, 0x46, 0x46,
                0x46, 0x0, 0x47, 0x47, 0x47, 0x0, 0x48, 0x48, 0x48, 0x0, 0x49, 0x49, 0x49, 0x0, 0x4A, 0x4A,
                0x4A, 0x0, 0x4B, 0x4B, 0x4B, 0x0, 0x4C, 0x4C, 0x4C, 0x0, 0x4D, 0x4D, 0x4D, 0x0, 0x4E, 0x4E,
                0x4E, 0x0, 0x4F, 0x4F, 0x4F, 0x0, 0x50, 0x50, 0x50, 0x0, 0x51, 0x51, 0x51, 0x0, 0x52, 0x52,
                0x52, 0x0, 0x53, 0x53, 0x53, 0x0, 0x54, 0x54, 0x54, 0x0, 0x55, 0x55, 0x55, 0x0, 0x56, 0x56,
                0x56, 0x0, 0x57, 0x57, 0x57, 0x0, 0x58, 0x58, 0x58, 0x0, 0x59, 0x59, 0x59, 0x0, 0x5A, 0x5A,
                0x5A, 0x0, 0x5B, 0x5B, 0x5B, 0x0, 0x5C, 0x5C, 0x5C, 0x0, 0x5D, 0x5D, 0x5D, 0x0, 0x5E, 0x5E,
                0x5E, 0x0, 0x5F, 0x5F, 0x5F, 0x0, 0x60, 0x60, 0x60, 0x0, 0x61, 0x61, 0x61, 0x0, 0x62, 0x62,
                0x62, 0x0, 0x63, 0x63, 0x63, 0x0, 0x64, 0x64, 0x64, 0x0, 0x65, 0x65, 0x65, 0x0, 0x66, 0x66,
                0x66, 0x0, 0x67, 0x67, 0x67, 0x0, 0x68, 0x68, 0x68, 0x0, 0x69, 0x69, 0x69, 0x0, 0x6A, 0x6A,
                0x6A, 0x0, 0x6B, 0x6B, 0x6B, 0x0, 0x6C, 0x6C, 0x6C, 0x0, 0x6D, 0x6D, 0x6D, 0x0, 0x6E, 0x6E,
                0x6E, 0x0, 0x6F, 0x6F, 0x6F, 0x0, 0x70, 0x70, 0x70, 0x0, 0x71, 0x71, 0x71, 0x0, 0x72, 0x72,
                0x72, 0x0, 0x73, 0x73, 0x73, 0x0, 0x74, 0x74, 0x74, 0x0, 0x75, 0x75, 0x75, 0x0, 0x76, 0x76,
                0x76, 0x0, 0x77, 0x77, 0x77, 0x0, 0x78, 0x78, 0x78, 0x0, 0x79, 0x79, 0x79, 0x0, 0x7A, 0x7A,
                0x7A, 0x0, 0x7B, 0x7B, 0x7B, 0x0, 0x7C, 0x7C, 0x7C, 0x0, 0x7D, 0x7D, 0x7D, 0x0, 0x7E, 0x7E,
                0x7E, 0x0, 0x7F, 0x7F, 0x7F, 0x0, 0x80, 0x80, 0x80, 0x0, 0x81, 0x81, 0x81, 0x0, 0x82, 0x82,
                0x82, 0x0, 0x83, 0x83, 0x83, 0x0, 0x84, 0x84, 0x84, 0x0, 0x85, 0x85, 0x85, 0x0, 0x86, 0x86,
                0x86, 0x0, 0x87, 0x87, 0x87, 0x0, 0x88, 0x88, 0x88, 0x0, 0x89, 0x89, 0x89, 0x0, 0x8A, 0x8A,
                0x8A, 0x0, 0x8B, 0x8B, 0x8B, 0x0, 0x8C, 0x8C, 0x8C, 0x0, 0x8D, 0x8D, 0x8D, 0x0, 0x8E, 0x8E,
                0x8E, 0x0, 0x8F, 0x8F, 0x8F, 0x0, 0x90, 0x90, 0x90, 0x0, 0x91, 0x91, 0x91, 0x0, 0x92, 0x92,
                0x92, 0x0, 0x93, 0x93, 0x93, 0x0, 0x94, 0x94, 0x94, 0x0, 0x95, 0x95, 0x95, 0x0, 0x96, 0x96,
                0x96, 0x0, 0x97, 0x97, 0x97, 0x0, 0x98, 0x98, 0x98, 0x0, 0x99, 0x99, 0x99, 0x0, 0x9A, 0x9A,
                0x9A, 0x0, 0x9B, 0x9B, 0x9B, 0x0, 0x9C, 0x9C, 0x9C, 0x0, 0x9D, 0x9D, 0x9D, 0x0, 0x9E, 0x9E,
                0x9E, 0x0, 0x9F, 0x9F, 0x9F, 0x0, 0xA0, 0xA0, 0xA0, 0x0, 0xA1, 0xA1, 0xA1, 0x0, 0xA2, 0xA2,
                0xA2, 0x0, 0xA3, 0xA3, 0xA3, 0x0, 0xA4, 0xA4, 0xA4, 0x0, 0xA5, 0xA5, 0xA5, 0x0, 0xA6, 0xA6,
                0xA6, 0x0, 0xA7, 0xA7, 0xA7, 0x0, 0xA8, 0xA8, 0xA8, 0x0, 0xA9, 0xA9, 0xA9, 0x0, 0xAA, 0xAA,
                0xAA, 0x0, 0xAB, 0xAB, 0xAB, 0x0, 0xAC, 0xAC, 0xAC, 0x0, 0xAD, 0xAD, 0xAD, 0x0, 0xAE, 0xAE,
                0xAE, 0x0, 0xAF, 0xAF, 0xAF, 0x0, 0xB0, 0xB0, 0xB0, 0x0, 0xB1, 0xB1, 0xB1, 0x0, 0xB2, 0xB2,
                0xB2, 0x0, 0xB3, 0xB3, 0xB3, 0x0, 0xB4, 0xB4, 0xB4, 0x0, 0xB5, 0xB5, 0xB5, 0x0, 0xB6, 0xB6,
                0xB6, 0x0, 0xB7, 0xB7, 0xB7, 0x0, 0xB8, 0xB8, 0xB8, 0x0, 0xB9, 0xB9, 0xB9, 0x0, 0xBA, 0xBA,
                0xBA, 0x0, 0xBB, 0xBB, 0xBB, 0x0, 0xBC, 0xBC, 0xBC, 0x0, 0xBD, 0xBD, 0xBD, 0x0, 0xBE, 0xBE,
                0xBE, 0x0, 0xBF, 0xBF, 0xBF, 0x0, 0xC0, 0xC0, 0xC0, 0x0, 0xC1, 0xC1, 0xC1, 0x0, 0xC2, 0xC2,
                0xC2, 0x0, 0xC3, 0xC3, 0xC3, 0x0, 0xC4, 0xC4, 0xC4, 0x0, 0xC5, 0xC5, 0xC5, 0x0, 0xC6, 0xC6,
                0xC6, 0x0, 0xC7, 0xC7, 0xC7, 0x0, 0xC8, 0xC8, 0xC8, 0x0, 0xC9, 0xC9, 0xC9, 0x0, 0xCA, 0xCA,
                0xCA, 0x0, 0xCB, 0xCB, 0xCB, 0x0, 0xCC, 0xCC, 0xCC, 0x0, 0xCD, 0xCD, 0xCD, 0x0, 0xCE, 0xCE,
                0xCE, 0x0, 0xCF, 0xCF, 0xCF, 0x0, 0xD0, 0xD0, 0xD0, 0x0, 0xD1, 0xD1, 0xD1, 0x0, 0xD2, 0xD2,
                0xD2, 0x0, 0xD3, 0xD3, 0xD3, 0x0, 0xD4, 0xD4, 0xD4, 0x0, 0xD5, 0xD5, 0xD5, 0x0, 0xD6, 0xD6,
                0xD6, 0x0, 0xD7, 0xD7, 0xD7, 0x0, 0xD8, 0xD8, 0xD8, 0x0, 0xD9, 0xD9, 0xD9, 0x0, 0xDA, 0xDA,
                0xDA, 0x0, 0xDB, 0xDB, 0xDB, 0x0, 0xDC, 0xDC, 0xDC, 0x0, 0xDD, 0xDD, 0xDD, 0x0, 0xDE, 0xDE,
                0xDE, 0x0, 0xDF, 0xDF, 0xDF, 0x0, 0xE0, 0xE0, 0xE0, 0x0, 0xE1, 0xE1, 0xE1, 0x0, 0xE2, 0xE2,
                0xE2, 0x0, 0xE3, 0xE3, 0xE3, 0x0, 0xE4, 0xE4, 0xE4, 0x0, 0xE5, 0xE5, 0xE5, 0x0, 0xE6, 0xE6,
                0xE6, 0x0, 0xE7, 0xE7, 0xE7, 0x0, 0xE8, 0xE8, 0xE8, 0x0, 0xE9, 0xE9, 0xE9, 0x0, 0xEA, 0xEA,
                0xEA, 0x0, 0xEB, 0xEB, 0xEB, 0x0, 0xEC, 0xEC, 0xEC, 0x0, 0xED, 0xED, 0xED, 0x0, 0xEE, 0xEE,
                0xEE, 0x0, 0xEF, 0xEF, 0xEF, 0x0, 0xF0, 0xF0, 0xF0, 0x0, 0xF1, 0xF1, 0xF1, 0x0, 0xF2, 0xF2,
                0xF2, 0x0, 0xF3, 0xF3, 0xF3, 0x0, 0xF4, 0xF4, 0xF4, 0x0, 0xF5, 0xF5, 0xF5, 0x0, 0xF6, 0xF6,
                0xF6, 0x0, 0xF7, 0xF7, 0xF7, 0x0, 0xF8, 0xF8, 0xF8, 0x0, 0xF9, 0xF9, 0xF9, 0x0, 0xFA, 0xFA,
                0xFA, 0x0, 0xFB, 0xFB, 0xFB, 0x0, 0xFC, 0xFC, 0xFC, 0x0, 0xFD, 0xFD, 0xFD, 0x0, 0xFE, 0xFE,
                0xFE, 0x0, 0xFF, 0xFF, 0xFF, 0x0};
        try{
            while(true)
            {
                byte[] bytes = new byte[0x40002];
                DataOutputStream dataOutputStream = null;
                int len;
                int offset = 0;
                int max = 0x40438;
                int temp=0;

                if(!streaming){
                    break;
                }

                do{
                    if( dataOutputStream == null ){
                        dataOutputStream = new DataOutputStream(new FileOutputStream( new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp_"+count+".bmp")));
                        for (int i = 0; i < 0x436; i++){
                            dataOutputStream.writeByte(buf[i]);
                        }
                        offset = 0x436;
                    }

                    int size = clientInfo.readBuf.read(bytes);

                    if( size < 0)
                        break;

                    temp = Math.min(size,max-offset);
                    dataOutputStream.write(bytes,0,temp);

                    offset += temp;
                    if( offset >= max){
                        dataOutputStream.close();
                        dataOutputStream = null;
                        handler.sendMessage(handler.obtainMessage(-1, count,0));
                        count = (count+1)%10;
                        offset = 0;
                    }
                }while(true);
            }
        } catch (Exception e) {
            clientInfo.close();
            clientInfo = null;
            return ;
        }
    }

    public boolean startTCPServer(final File cachdir){
        if(ServerThread != null ){
            ServerThread.interrupt();
            ServerThread = null;
        }

        ServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "S: Connecting...");
                    ServerSocket serverSocket = new ServerSocket(PORT);

                    while (true) {
                        final Socket client = serverSocket.accept();
                        if(clientInfo!= null) {
                            clientInfo.close();
                            if(ReadThread != null ){
                                ReadThread.interrupt();
                                ReadThread = null;
                            }
                        }
                        clientInfo = new ClientInfo(client);
                        ReadThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while(true){
                                    try {
                                        Byte type = clientInfo.readBuf.readByte();

                                        switch (type){
                                            case 0x01:          // Connect check
                                                break;
                                            case 0x02:          // get Tablet Name
                                                handler.sendMessage(handler.obtainMessage(1,"get;TabletName"));
                                                break;
                                            case 0x03:          // activity
                                                Byte activityCode = clientInfo.readBuf.readByte();
                                                switch (activityCode){
                                                    case 0x01:
                                                        handler.sendMessage(handler.obtainMessage(1,"act;WaitActivity"));
                                                        break;
                                                    case 0x02:
                                                        handler.sendMessage(handler.obtainMessage(1,"act;MainActivity"));
                                                        break;
                                                    case 0x03:
                                                        handler.sendMessage(handler.obtainMessage(1,"act;TeamActivity"));
                                                        break;
                                                    case 0x04:
                                                        handler.sendMessage(handler.obtainMessage(1,"act;TacticalActivity"));
                                                        break;
                                                    case 0x05:
                                                        handler.sendMessage(handler.obtainMessage(1,"act;DroneActivity"));
                                                        break;
                                                }
                                                break;
                                            case 0x04:          // start Drone Image
                                                handler.sendMessage(handler.obtainMessage(1,"startDroneImage"));
                                                startDrone();
                                                break;
                                            case 0x05:          // set Person Data
                                            case 0x07:          // per Person Data
                                            {
                                                int len = clientInfo.readBuf.readInt();
                                                int x,y,ox,oy;
                                                int hp;
                                                boolean order;
                                                String name;

                                                ArrayList<PersonData> data = new ArrayList<PersonData>();
                                                for(int i=0;i<len;i++){
                                                    hp = clientInfo.readBuf.readByte();
                                                    x  = clientInfo.readBuf.readByte();
                                                    if( x == 0 )
                                                        order = false;
                                                    else
                                                        order = true;

                                                    x = clientInfo.readBuf.readInt();
                                                    y = clientInfo.readBuf.readInt();

                                                    ox = clientInfo.readBuf.readInt();
                                                    oy = clientInfo.readBuf.readInt();


                                                    name = clientInfo.readBuf.readLine();
                                                    Log.d(TAG,name+"("+x+","+y+") ("+ox+","+oy+")");
                                                    data.add(new PersonData(hp,order,new Point(x,y),new Point(ox,oy),name));
                                                }
                                                handler.sendMessage(handler.obtainMessage(type,data));
                                            }
                                                break;
                                            case 0x06:          // ene Enemy Data
                                            {
                                                int len = clientInfo.readBuf.readInt();
                                                int x,y;
                                                ArrayList<Point> points = new ArrayList<Point>();
                                                for(int i=0;i<len;i++){
                                                    x = clientInfo.readBuf.readInt();
                                                    y = clientInfo.readBuf.readInt();
                                                    points.add(new Point(x,y));
                                                }
                                                handler.sendMessage(handler.obtainMessage(type,points));
                                            }
                                                break;
                                            case 0x08:          // gme; Paused
                                                handler.sendMessage(handler.obtainMessage(1,"gme;Paused"));
                                                break;
                                            case 0x09:          // gme;UnPaused
                                                handler.sendMessage(handler.obtainMessage(1,"gme;UnPaused"));
                                                break;
                                        }
                                    } catch (Exception e) {
                                        clientInfo.close();
                                        clientInfo = null;
                                        return ;
                                    }
                                }
                            }
                        });

                        ReadThread.start();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "S: Error");
                    e.printStackTrace();
                }
            }
        });

        ServerThread.start();

        return true;
    }

    public boolean sendMessage(String msg){
        if(clientInfo!= null) {
            clientInfo.writeBuf.println(msg);
            return true;
        }
        return false;
    }

}
