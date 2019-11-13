package com.mitac.battinfo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.os.UEventObserver;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BattInfoActivity extends Activity {

    private static String TAG = "BattInfoActivity";

    TextView txtBatt;
    TextView txtInfo;
    LinearLayout tipBack;
    private String strBatt = null;
/*
    private static int cntAttach = 0;
    private static int cntDettach = 0;

    private static final int MSG_REFRESH = 0x1245;

    private Handler hRefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REFRESH:
                if (txtBatt != null) {
                    txtBatt.setText("Attach: " + cntAttach);
                }
                if (txtInfo != null) {
                    txtInfo.setText("Dettach: " + cntDettach);
                }
                if (tipBack != null && cntDettach>0) {
                    tipBack.setBackgroundColor(Color.RED);
                }
                break;
            default:
                break;
            }
        }
    };

    private UEventObserver m_BattInfoObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEvent event) {
            Log.d(TAG, "Event: " + event);
            boolean bBattInfo = "dock".equals(event.get("SWITCH_NAME")) ? true:false;
            if (bBattInfo) {
                String status = event.get("SWITCH_STATE");
                if ("1".equals(status)) {
                    Log.d(TAG, "BattInfo is attached.");
                    cntAttach += 1;
                } else if ("0".equals(status)) {
                    Log.d(TAG, "BattInfo is dettached.");
                    cntDettach += 1;
                }
                hRefresh.sendEmptyMessage(MSG_REFRESH);
            }
        }
    };
*/
    public static int byteToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0|s1|s2|s3;
        return s;
    }

    public static String bytesToString(byte[] str) {
        String keyword = null;
        try {
            keyword = new String(str,"US-ASCII");
            //System.out.println(keyword.indexOf('\u0000'));
            keyword = keyword.substring(0, keyword.indexOf('\u0000'));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return keyword;
    }

/*
    public static void printHexString( byte[] b) {
        System.out.println(b.length);
        System.out.println(Arrays.toString(b));

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.println(hex.toUpperCase());
        }
    }
*/

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            sb.append("0x");
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex.toUpperCase());
            sb.append(" ");
        }
        return sb.toString();
    }

    private boolean parseBattData(byte[] buf) {
        //parse data
        int id = 0;
        String name = null;
        int len = 0;
        byte [] temp_id = new byte[4];
        byte [] temp_name = new byte[20];
        byte [] temp_len = new byte[4];
        byte [] temp = new byte[148];
        int pos = 0;
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<20; i++) {
            pos = 12+148*i;
            System.arraycopy(buf, pos, temp, 0, 148);
            // ID, 4 bytes
            System.arraycopy(temp, 0, temp_id, 0, 4);
            id = byteToInt(temp_id);
            // name, 20 bytes
            System.arraycopy(temp, 4, temp_name, 0, 20);
            name = bytesToString(temp_name);
            // length, 4 bytes
            System.arraycopy(temp, 24, temp_len, 0, 4);
            len = byteToInt(temp_len);
            // raw data, less than 120 bytes
            byte [] raw_data = new byte[len];
            String strHex = null;
            System.arraycopy(temp, 28, raw_data, 0, len);
            strHex = bytesToHex(raw_data);
            // wrap up one subclass data
            //strBatt = " "+id+", "+name+", "+len+":  "+ strHex;
            //Log.e(TAG, strBatt);
            builder.append(" ");
            builder.append(id);
            builder.append(", ");
            builder.append(name);
            builder.append(", ");
            builder.append(len);
            builder.append(":  ");
            builder.append("\r\n");
            builder.append(strHex);
            builder.append("\r\n\r\n");
        }
        strBatt = builder.toString();
        return true;
    }

    private boolean saveBattInfo() {
        boolean ret = false;
        byte [] buf = new byte[4*1024];
        int count = 0;
        FileInputStream reader = null;
        FileOutputStream writer = null;
        for(int i=0; i<4096; i++) {
            buf[i] = 0x0;
        }
        File inFile = new File("/sys/sys_info/battery_info");//Total size: 3264 bytes
        File outFile = new File("/data/data/com.mitac.battinfo/battinfo");

        try {
            if(!outFile.exists()) {
                outFile.createNewFile();
            }
            Log.e(TAG,"1111111111111111111111111111111111111111111111111 ");
            reader = new FileInputStream(inFile);
            writer = new FileOutputStream(outFile);
            //Copy data and save it
            while((count=reader.read(buf)) != -1 ) {
                Log.e(TAG, "read, count: "+count);
                writer.write(buf, 0, count);
            }
            Log.e(TAG,"2222222222222222222222222222222222222222222222222 ");

            //parse data
            parseBattData(buf);
        } catch (IOException ex) {
            Log.e(TAG,"Couldn't write the content: " + ex);
        } catch (NumberFormatException ex) {
            Log.e(TAG,"Couldn't write the content: " + ex);
        } finally {
            if (writer != null) {
                try {
                    reader.close();
                    writer.close();
                } catch (IOException ex) {
                }
            }
        }
        return ret;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txtBatt = (TextView) findViewById(R.id.batt);
        txtInfo = (TextView) findViewById(R.id.info);
        tipBack = (LinearLayout) findViewById(R.id.background);
        Log.d(TAG, "onCreate");
        //m_BattInfoObserver.startObserving("SUBSYSTEM=switch");
        saveBattInfo();
        txtBatt.setText(strBatt);
        txtInfo.setText("OK");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //hRefresh.sendEmptyMessage(MSG_REFRESH);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        //m_BattInfoObserver.stopObserving();
    }
}
