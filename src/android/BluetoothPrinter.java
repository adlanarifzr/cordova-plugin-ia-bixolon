package br.com.cordova.printer.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class BluetoothPrinter extends CordovaPlugin {

    private static final String LOG_TAG = "BluetoothPrinter";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private static final byte[] ESC_ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] ESC_ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    private static final byte[] ESC_ALIGN_CENTER = {0x1B, 0x61, 0x01};

    private static final byte[] CUT_PAPER = {0x1b, 0x69};

    private static String CURRENT_BRAND = "generic";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            switch (action) {
                case "status":
                    checkBTStatus(callbackContext);
                    return true;
                case "list":
                    listBT(callbackContext);
                    return true;
                case "connect":
                    String name = args.getString(0);
                    if (findBT(callbackContext, name)) {
                        connectBT(callbackContext);
                    } else {
                        callbackContext.error("BLUETOOTH DEVICE NOT FOUND: " + name);
                    }
                    return true;
                case "disconnect":
                    disconnectBT(callbackContext);
                    return true;
                case "printText":
                    String text = args.getString(0);
                    printText(callbackContext, text);
                    return true;
                case "printBase64":
                    Integer baseAlign = Integer.parseInt(args.getString(1));
                    printBase64(callbackContext, args.getString(0), baseAlign);
                    return true;
                case "printImageUrl":
                    String imageURL = args.getString(0);
                    Integer imageAlign = Integer.parseInt(args.getString(1));
                    printImageUrl(callbackContext, imageURL, imageAlign);
                    return true;
                case "printPOSCommand":
                    printPOSCommand(callbackContext, hexStringToBytes(args.getString(0)));
                    return true;
                case "cutPaper":
                    cutPaper(callbackContext);
                    return true;
                case "setBrand":
                    String b = args.getString(0);

                    if (b != null && !b.equals("")) {
                        CURRENT_BRAND = b;
                        callbackContext.success("Brand changed to " + b);
                    } else {
                        callbackContext.error("Invalid brand supplied " + b + ". Current brand remains " + CURRENT_BRAND);
                    }
                    return true;
                case "printQRCode":
                    String codeText = args.getString(0);
                    printQRCode(callbackContext, codeText);
                    return true;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("Command failed, bad JSON. Check your inputs.");
            e.printStackTrace();
        }
        return false;
    }

    private void checkBTStatus(CallbackContext callbackContext) {
        try {
            JSONArray json = new JSONArray();

            // Check if adapter is enabled.
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                json.put("enabled=true");

                // Check if connected
                if (mmSocket.isConnected()) {
                    json.put("connected=true");

                    BluetoothDevice connectedDevice = mmSocket.getRemoteDevice();

                    json.put("name=" + connectedDevice.getName());
                    json.put("address=" + connectedDevice.getAddress());
                } else {
                    json.put("connected=false");
                }
            } else {
                json.put("enabled=false");
                json.put("connected=false");
            }

            callbackContext.success(json);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    /**
     * This will return the array list of paired bluetooth printers
     */
    private void listBT(CallbackContext callbackContext) {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                String errorMessage = "NO BLUETOOTH ADAPTER AVAILABLE";
                Log.e(LOG_TAG, errorMessage);
                callbackContext.error(errorMessage);
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                JSONArray json = new JSONArray();
                for (BluetoothDevice device : pairedDevices) {
                    /*
                     * Hashtable map = new Hashtable(); map.put("type", device.getType());
                     * map.put("address", device.getAddress()); map.put("name", device.getName());
                     * JSONObject jObj = new JSONObject(map);
                     */
                    Log.v(LOG_TAG, "DEVICE getName-> " + device.getName());
                    Log.v(LOG_TAG, "DEVICE getAddress-> " + device.getAddress());
                    Log.v(LOG_TAG, "DEVICE getType-> " + device.getType());
                    json.put(device.getName());
                    json.put(device.getAddress());
                    json.put(device.getType());
                }
                callbackContext.success(json);
            } else {
                callbackContext.error("NO BLUETOOTH DEVICE FOUND");
            }
            // Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getName());
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    /**
     * Looks for the specified bluetooth device. If found, sets mmDevice to the device instance.
     *
     * @param callbackContext Cordova call context.
     * @param name            Device name
     * @return true if found, false otherwise.
     */
    private boolean findBT(CallbackContext callbackContext, String name) {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(LOG_TAG, "NO BLUETOOTH ADAPTER AVAILABLE");
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equalsIgnoreCase(name)) {
                        mmDevice = device;
                        return true;
                    }
                }
            }
            Log.d(LOG_TAG, "BLUETOOTH DEVICE FOUND: " + mmDevice.getName());
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    /**
     * Tries to open a connection to the bluetooth printer device.
     *
     * @param callbackContext Cordova call context.
     */
    private void connectBT(CallbackContext callbackContext) {
        try {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            beginListenForData();
            Log.d(LOG_TAG, "BLUETOOTH OPENED: " + mmDevice.getName());
            callbackContext.success("BLUETOOTH OPENED: " + mmDevice.getName());
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    // After opening a connection to bluetooth printer device,
    // we have to listen and check if a data were sent to be printed.
    private void beginListenForData() {
        try {
//            final Handler handler = new Handler();
            // This is the ASCII code for a newline character
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        /*
                                         * final String data = new String(encodedBytes, "US-ASCII"); readBufferPosition
                                         * = 0; handler.post(new Runnable() { public void run() { myLabel.setText(data);
                                         * } });
                                         */
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // align text
    private static byte[] selAlignTitle(int align) {
        switch (align) {
            case 0:
                return ESC_ALIGN_LEFT;
            case 1:
                return ESC_ALIGN_CENTER;
            case 2:
                return ESC_ALIGN_RIGHT;
        }
        return ESC_ALIGN_LEFT;
    }

    private void printText(CallbackContext callbackContext, String msg) {
        try {
            mmOutputStream.write(msg.getBytes());
            // mmOutputStream.write(msg.getBytes("iso-8859-1"));
            // tell the user data were sent
            Log.d(LOG_TAG, "PRINT TEXT SEND -> " + msg);
            callbackContext.success("PRINT TEXT SEND");

        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    private void printPOSCommand(CallbackContext callbackContext, byte[] buffer) {
        try {
            mmOutputStream.write(buffer);
            // tell the user data were sent
            Log.d(LOG_TAG, "POS COMMAND SENT");
            callbackContext.success("Data Sent");
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    private void cutPaper(CallbackContext callbackContext) {
        printPOSCommand(callbackContext, CUT_PAPER);
    }

    /**
     * Disconnect streams and socket from the connected bluetooth device.
     *
     * @param callbackContext Cordova callback context
     */
    private void disconnectBT(CallbackContext callbackContext) {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            callbackContext.success("BLUETOOTH DISCONNECT");
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    // send url from image
    private void printImageUrl(CallbackContext callbackContext, String msg, Integer align) {
        try {
            Log.d(LOG_TAG, "Preparar para impressao, passando o caminho da imagem -> " + msg);
            Log.d(LOG_TAG, "ALIGN -> " + align);
            Bitmap bmp = BitmapFactory.decodeFile(msg);
            if (bmp != null) {
                byte[] command = decodeBitmapUrl(bmp);
                Log.d(LOG_TAG, "SWITCH ALIGN -> " + align);
                switch (align) {
                    case 0:
                        printLeftImage(command);
                        break;
                    case 1:
                        printCenterImage(command);
                        break;
                    case 2:
                        printRightImage(command);
                        break;
                }
            } else {
                Log.d(LOG_TAG, "PRINT PHOTO ERROR THE FILE ISN'T EXISTS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "PRINT TOOLS THE FILE ISN'T EXISTS");
        }
    }

    // print image align left
    private void printLeftImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT LEFT IMAGE");
            mmOutputStream.write(ESC_ALIGN_LEFT);
            mmOutputStream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERROR PRINT LEFT IMAGE");
        }
    }

    // print image align center
    private void printCenterImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT CENTER IMAGE");
            mmOutputStream.write(ESC_ALIGN_CENTER);
            mmOutputStream.write(msg);
            // return to left position
            mmOutputStream.write(ESC_ALIGN_LEFT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERROR PRINT CENTER IMAGE");
        }
    }

    // print image align right
    private void printRightImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT RIGHT IMAGE");
            mmOutputStream.write(ESC_ALIGN_RIGHT);
            mmOutputStream.write(msg);
            // return to left position
            mmOutputStream.write(ESC_ALIGN_LEFT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERROR PRINT RIGHT IMAGE");
        }
    }

    // This will send data to bluetooth printer
    private void printBase64(CallbackContext callbackContext, String encodedString, Integer align) {
        try {

            Bitmap image = convertBase64ToBitmap(encodedString);

            int maxWidth = 512;

            Bitmap scaled = scaleBitmap(image, maxWidth, 512);

            // Set alignment - only on non-Bixolon printers
            if (!CURRENT_BRAND.equals("bixolon")) {
                mmOutputStream.write(selAlignTitle(align));
            }

            // Write image
            mmOutputStream.write(decodeBitmapBase64(scaled));

            Log.d(LOG_TAG, "PRINT BASE64 SEND");
            callbackContext.success("PRINT BASE64 SEND");

        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    private void printQRCode(CallbackContext callbackContext, String str) {
        try {
            // In development
            mmOutputStream.write(str.getBytes());
            Log.d(LOG_TAG, "PRINT QRCODE SENT");
            callbackContext.success("PRINT QRCODE SENT");
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    private static byte[] decodeBitmapBase64(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> bpmBinaryList = bitmapToBinaryList(bmp); // binaryString list
        List<String> bmpHexList = binaryListToHexStringList(bpmBinaryList);
        String commandHexString = "1D763000";

        // construct xL and xH
        // there are 8 pixels per byte. In case of modulo: add 1 to compensate.
        bmpWidth = bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1);
        int xL = bmpWidth % 256;
        int xH = (bmpWidth - xL) / 256;

        String xLHex = Integer.toHexString(xL);
        String xHHex = Integer.toHexString(xH);
        if (xLHex.length() == 1) {
            xLHex = "0" + xLHex;
        }
        if (xHHex.length() == 1) {
            xHHex = "0" + xHHex;
        }
        String widthHexString = xLHex + xHHex;

        // construct yL and yH
        int yL = bmpHeight % 256;
        int yH = (bmpHeight - yL) / 256;

        String yLHex = Integer.toHexString(yL);
        String yHHex = Integer.toHexString(yH);
        if (yLHex.length() == 1) {
            yLHex = "0" + yLHex;
        }
        if (yHHex.length() == 1) {
            yHHex = "0" + yHHex;
        }
        String heightHexString = yLHex + yHHex;

        List<String> commandList = new ArrayList<>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        try {
            Log.d(LOG_TAG, "BASE64 Raster command is: " + Arrays.toString(hexList2Byte(commandList)));
        } catch (Exception e) {
            Log.d(LOG_TAG, "I tried so hard, but go no far");
        }

        return hexList2Byte(commandList);
    }

    private static byte[] decodeBitmapUrl(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> bpmBinaryList = bitmapToBinaryList(bmp);
        List<String> bmpHexList = binaryListToHexStringList(bpmBinaryList);
        String commandHexString = "1D763000";
        String widthHexString = Integer.toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        try {
            Log.d(LOG_TAG, "URL Raster command is: " + Arrays.toString(hexList2Byte(commandList)));
        } catch (Exception e) {
            Log.d(LOG_TAG, "I tried so hard, but go no far");
        }

        return hexList2Byte(commandList);
    }

    private static List<String> bitmapToBinaryList(Bitmap bmp) {
        List<String> list = new ArrayList<>();
        StringBuffer sb;

        int zeroCount = bmp.getWidth() % 8;

        StringBuilder zeroStr = new StringBuilder();
        if (zeroCount > 0) {
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr.append("0");
            }
        }

        for (int i = 0; i < bmp.getHeight(); i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmp.getWidth(); j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
            if (zeroCount > 0) {
                sb.append(zeroStr.toString());
            }
            list.add(sb.toString());
        }
        return list;
    }

    private static List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<>();
        for (String binaryStr : list) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    private static String myBinaryStrToHexString(String binaryStr) {
        StringBuilder hex = new StringBuilder();
        String hexStr = "0123456789ABCDEF";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i])) {
                hex.append(hexStr.substring(i, i + 1));
            }
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i])) {
                hex.append(hexStr.substring(i, i + 1));
            }
        }
        return hex.toString();
    }

    private static String[] binaryArray = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
            "1001", "1010", "1011", "1100", "1101", "1110", "1111"};

    private static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<>();
        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        return sysCopy(commandList);
    }

    // New implementation, change old
    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        // Determine the constrained dimension, which determines both dimensions.
        int width;
        int height;
        float widthRatio = (float) bitmap.getWidth() / maxWidth;
        float heightRatio = (float) bitmap.getHeight() / maxHeight;
        // Width constrained.
        if (widthRatio >= heightRatio) {
            width = maxWidth;
            height = (int) (((float) width / bitmap.getWidth()) * bitmap.getHeight());
        }
        // Height constrained.
        else {
            height = maxHeight;
            width = (int) (((float) height / bitmap.getHeight()) * bitmap.getWidth());
        }
        Bitmap scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float ratioX = (float) width / bitmap.getWidth();
        float ratioY = (float) height / bitmap.getHeight();
        float middleX = width / 2.0f;
        float middleY = height / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

}
