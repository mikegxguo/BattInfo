package com.mitac.battinfo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    public static int byte2Int(byte[] b) {
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

    public static int byte2Short(byte[] bytes, int off) {
        int high = bytes[off];
        int low = bytes[off + 1];
        return (high << 8 & 0xFF00) | (low & 0xFF);
    }

    public static String bytes2Dec(byte[] bytes, int start, int len) {
        StringBuffer sb = new StringBuffer();
        //byte int8 = 0;
        short int16 = 0;
        int temp = 0;
        int temp0 = 0;
        int temp1 = 0;
        int temp2 = 0;
        int temp3 = 0;
        sb.append("    "); //TODO: the blank is friendly for user.
        if(len == 1) { //One byte
            temp0 = bytes[start] & 0xff;
            sb.append(temp0);
        } else if(len == 2) {//Short
            temp0 = bytes[start] & 0xff;
            temp1 = bytes[start+1] & 0xff;
            int16 = (short)((temp0<<8)|temp1);
            sb.append(int16);
        } else if(len == 4) {//Int
            temp0 = bytes[start] ;
            temp1 = bytes[start+1] ;
            temp2 = bytes[start+2];
            temp3 = bytes[start+3];
            temp = (temp0<<24)|(temp1<<16)|(temp2<<8)|temp3;
            sb.append(temp);
        } else {
            //TODO
        }
        //sb.append("\r\n");
        return sb.toString();
    }

    public static String bytes2String(byte[] str) {
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

     public static String bytes2String(byte[] bytes, int start, int len) {
        StringBuffer sb = new StringBuffer();
        String str = null;
        byte [] temp = new byte[20];
       // sb.append("    "); //TODO: the blank is friendly for user.
        //sb.append("");
        System.arraycopy(bytes, start, temp, 0, len);
            str = bytes2String(temp);
        //for(int i = start; i < start+len; i++) {
            //String hex = Integer.toHexString(bytes[i] & 0xFF);
            //if(hex.length() < 2){
                sb.append(str);
            //}
            //sb.append(hex.toUpperCase());
            //sb.append(" ");
        //}
        sb.append("\r\n");
        return sb.toString();
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

    public static String bytes2Hex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        sb.append("    "); //TODO: the blank is friendly for user.
        sb.append("0x");
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex.toUpperCase());
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String bytes2Hex(byte[] bytes, int start, int len) {
        StringBuffer sb = new StringBuffer();
        sb.append("    "); //TODO: the blank is friendly for user.
        sb.append("0x");
        for(int i = start; i < start+len; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex.toUpperCase());
            sb.append(" ");
        }
        sb.append("\r\n");
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
            id = byte2Int(temp_id);
            // name, 20 bytes
            System.arraycopy(temp, 4, temp_name, 0, 20);
            name = bytes2String(temp_name);
            // length, 4 bytes
            System.arraycopy(temp, 24, temp_len, 0, 4);
            len = byte2Int(temp_len);
            // raw data, less than 120 bytes
            byte [] raw_data = new byte[len];
            String strDetail = null;
            System.arraycopy(temp, 28, raw_data, 0, len);
            StringBuilder detail = new StringBuilder();
            switch(id) {
                case 2: //safety
                detail.append("  ot_chg: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" 0.1℃\r\n  ot_chg_time: ");
                detail.append(bytes2Dec(raw_data, 2, 1));
                detail.append(" s\r\n  ot_chg_recovery: ");
                detail.append(bytes2Dec(raw_data, 3, 2));
                detail.append(" 0.1℃\r\n  ot_dsg: ");
                detail.append(bytes2Dec(raw_data, 5, 2));
                detail.append(" 0.1℃\r\n  ot_dsg_time: ");
                detail.append(bytes2Dec(raw_data, 7, 1));
                detail.append(" s\r\n  ot_dsg_recovery: ");
                detail.append(bytes2Dec(raw_data, 8, 2));
                detail.append(" 0.1℃\r\n");
                strDetail = detail.toString();
                break;

                case 32: //inhibit cfg
                detail.append("  chg_inhibit_temp_l: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" 0.1℃\r\n  chg_inhibit_temp_h: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" 0.1℃\r\n  temp_hys: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append(" 0.1℃\r\n");
                strDetail = detail.toString();
                break;

                case 34: //Battery charge data
                detail.append("  charging voltage: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mV\r\n");
                strDetail = detail.toString();
                break;

                case 36: //Battery charge termination data
                detail.append("  taper_cur: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mA\r\n  min taper capacity: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" mAh\r\n  taper_vol: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append(" mV\r\n  cur_taper_window: ");
                detail.append(bytes2Dec(raw_data, 6, 1));
                detail.append(" s\r\n  tca_set:");
                detail.append(bytes2Dec(raw_data, 7, 1));
                detail.append(" %\r\n  tca_clear: ");
                detail.append(bytes2Dec(raw_data, 8, 1));
                detail.append(" %\r\n  fc_set: ");
                detail.append(bytes2Dec(raw_data, 9, 1));
                detail.append(" %\r\n  fc_clear: ");
                detail.append(bytes2Dec(raw_data, 10, 1));
                detail.append(" %\r\n  dod_eoc_delta_t: ");
                detail.append(bytes2Dec(raw_data, 11, 2));
                detail.append(" 0.1℃\r\n ");
                strDetail = detail.toString();
                break;

                case 48: //Battery data FIXME: length:57, but read 38 bytes.
                detail.append("  rem_cap_alarm: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mA\r\n  initial_standby: ");
                detail.append(bytes2Dec(raw_data, 8, 1));
                detail.append(" mA\r\n  initial_maxload: ");
                detail.append(bytes2Dec(raw_data, 9, 2));
                detail.append(" mA\r\n  cycle_count: ");
                detail.append(bytes2Dec(raw_data, 17, 2));
                detail.append("\r\n  cc_threshold: ");
                detail.append(bytes2Dec(raw_data, 19, 1));
                detail.append(" mAh\r\n  design_capacity: ");
                detail.append(bytes2Dec(raw_data, 23, 2));
                detail.append(" mAh\r\n  design_energy: ");
                detail.append(bytes2Dec(raw_data, 25, 2));
                detail.append(" mWh\r\n  soh_load_i: ");
                detail.append(bytes2Dec(raw_data, 27, 2));
                detail.append(" %\r\n  tdd_soh_per: ");
                detail.append(bytes2Dec(raw_data, 29, 1));
                detail.append("\r\n  isd_cur: ");
                detail.append(bytes2Dec(raw_data, 40, 2));
                //detail.append("  reserved: ");
               //detail.append(bytes2Hex(raw_data, 16, 6));
                detail.append("\r\n  isd_i_filter: ");
                detail.append(bytes2Dec(raw_data, 42, 1));
                detail.append("\r\n  min_isd_time: ");
                detail.append(bytes2Dec(raw_data, 43, 1));
                detail.append(" h\r\n  design_energy_scale: ");
                detail.append(bytes2Dec(raw_data, 44, 1));
                detail.append("\r\n  device_name: ");
                detail.append(bytes2String(raw_data, 45, 12));
                strDetail = detail.toString();
                break;

                case 49: //Battery discharge
                detail.append("  soc1_set_threshold: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mAh\r\n  soc1_clear_threshold: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" mAh\r\n  socf_set_threshold: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append(" mAh\r\n  socf_clear_threshold: ");
                detail.append(bytes2Dec(raw_data, 6, 2));
                detail.append(" mAh\r\n  bl_set_volt_threshold: ");
                detail.append(bytes2Dec(raw_data, 9, 1));
                detail.append(" mV\r\n  bl_set_volt_time: ");
                detail.append(bytes2Dec(raw_data, 11, 1));
                detail.append(" s\r\n  bl_clear_volt_threshold: ");
                detail.append(bytes2Dec(raw_data, 12, 2));
                detail.append(" mV\r\n  bh_set_volt_threshold: ");
                detail.append(bytes2Dec(raw_data, 14, 2));
                detail.append(" mV\r\n  bh_volt_time: ");
                detail.append(bytes2Dec(raw_data, 16, 1));
                detail.append(" s\r\n  bh_clear_volt_threshold: ");
                detail.append(bytes2Dec(raw_data, 17, 2));
                detail.append(" mV\r\n ");
                strDetail = detail.toString();
                break;

                case 56: //Battery manufacturer data
                detail.append("  pack_lot_code: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append("\r\n  pcb_lot_code: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append("\r\n  firmware_ver: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append("\r\n  hardware_revision: ");
                detail.append(bytes2Dec(raw_data, 6, 2));
                detail.append("\r\n  cell_revision: ");
                detail.append(bytes2Dec(raw_data, 8, 2));
                detail.append("\r\n  df_cfg_ver: ");
                detail.append(bytes2Dec(raw_data, 10, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 57: //Battery integrity data
                detail.append("  chem_df_checksum: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 59: //Battery lifetime data
                detail.append("  max_temp:   ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" 0.1℃\r\n  min_temp:   ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" 0.1℃\r\n  max_pack_vol: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append(" mV\r\n  min_pack_vol: ");
                detail.append(bytes2Dec(raw_data, 6, 2));
                detail.append(" mV\r\n  max_chg_cur:  ");
                detail.append(bytes2Dec(raw_data, 8, 2));
                detail.append(" mA\r\n  max_dsg_cur:  ");
                detail.append(bytes2Dec(raw_data, 10, 2));
                detail.append(" mA\r\n");
                strDetail = detail.toString();
                break;

                case 60: // Battery lifetime temp samples data
                detail.append("  lt_flash_cnt: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 64: // Battery registers data
                detail.append("  pack_cfg: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append("\r\n  pack_cfg_b: ");
                detail.append(bytes2Dec(raw_data, 2, 1));
                detail.append("\r\n  pack_cfg_c: ");
                detail.append(bytes2Dec(raw_data, 3, 1));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 66: // Battery lifetime resolution data
                detail.append("\r\n  lt_temp_res: ");
                detail.append(bytes2Dec(raw_data, 0, 1));
                detail.append("\r\n  lt_v_res: ");
                detail.append(bytes2Dec(raw_data, 1, 1));
                detail.append("\r\n  lt_cur_res: ");
                detail.append(bytes2Dec(raw_data, 2, 1));
                detail.append("\r\n  lt_update_time: ");
                detail.append(bytes2Dec(raw_data, 3, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 68: // Battery power data
                detail.append("  flash_update_ok_vol: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mV\r\n  sleep_cur: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" mA\r\n  hiber_i: ");
                detail.append(bytes2Dec(raw_data, 11, 2));
                //detail.append("  reserved: ");
                //detail.append(bytes2Hex(raw_data, 5, 7));
                detail.append(" mA\r\n  hiber_v: ");
                detail.append(bytes2Dec(raw_data, 13, 2));
                detail.append(" mV\r\n  fs_wait: ");
                detail.append(bytes2Dec(raw_data, 15, 1));
                detail.append(" s\r\n");
                strDetail = detail.toString();
                break;

                case 80: // Battery IT cfg data FIXME: 105, but read only 100 bytes
                detail.append("  load_select: ");
                detail.append(bytes2Dec(raw_data, 0, 1));
                detail.append("\r\n  load_mode: ");
                detail.append(bytes2Dec(raw_data, 1, 1));
                //detail.append("  reserved1: ");
                //detail.append(bytes2Hex(raw_data, 3, 4));
                detail.append("\r\n  max_res_factor: ");
                detail.append(bytes2Dec(raw_data, 21, 1));
                detail.append("\r\n  min_res_factor: ");
                detail.append(bytes2Dec(raw_data, 22, 1));
                detail.append("\r\n  ra_filter: ");
                detail.append(bytes2Dec(raw_data, 25, 2));
                detail.append("\r\n  term_vol: ");
                detail.append(bytes2Dec(raw_data, 67, 2));
                detail.append(" mV\r\n  term_v_delta: ");
                detail.append(bytes2Dec(raw_data, 69, 2));
                detail.append(" mV\r\n  restrelax_time: ");
                detail.append(bytes2Dec(raw_data, 72, 2));
                detail.append(" s\r\n  user_rate_ma: ");
                detail.append(bytes2Dec(raw_data, 76, 2));
                detail.append(" mA\r\n  user_rate_pwr: ");
                detail.append(bytes2Dec(raw_data, 78, 2));
                detail.append(" mW\r\n  reserve_cap_mah: ");
                detail.append(bytes2Dec(raw_data, 80, 2));
                detail.append(" mA\r\n  reserve_energy: ");
                detail.append(bytes2Dec(raw_data, 82, 2));
                detail.append(" mAh\r\n  max_scale_back_grid: ");
                detail.append(bytes2Dec(raw_data, 86, 1));
                detail.append("\r\n  max_delta_v: ");
                detail.append(bytes2Dec(raw_data, 87, 2));
                detail.append("\r\n  min_delta_v: ");
                detail.append(bytes2Dec(raw_data, 89, 2));
                detail.append("\r\n  max_sim_rate: ");
                detail.append(bytes2Dec(raw_data, 91, 1));
                detail.append("\r\n  min_sim_rate: ");
                detail.append(bytes2Dec(raw_data, 92, 1));
                //detail.append("\r\n  reserved2: ");
                //detail.append(bytes2Hex(raw_data, 33, 40));
                detail.append("\r\n  ra_max_delta: ");
                detail.append(bytes2Dec(raw_data, 93, 2));
               // detail.append("\r\n  reserved3: ");
                //detail.append(bytes2Dec(raw_data, 75, 2));
                detail.append("\r\n  qmax_max_delta: ");
                detail.append(bytes2Dec(raw_data, 95, 1));
                detail.append("\r\n  delta_v_max_delta: ");
                detail.append(bytes2Dec(raw_data, 96, 1));
                //detail.append("\r\n  reserved4: ");
                //detail.append(bytes2Hex(raw_data, 79, 19));
                detail.append("\r\n  fast_scale_start_soc: ");
                detail.append(bytes2Dec(raw_data, 102, 1));
                detail.append(" %\r\n  chg_hys_v_shift: ");
                detail.append(bytes2Dec(raw_data, 103, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 81: // Battery current thresholds  data
                detail.append("  dsg_cur_threshold: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mA\r\n  chg_cur_threshold: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append(" mA\r\n  quit_current: ");
                detail.append(bytes2Dec(raw_data, 4, 2));
                detail.append(" mA\r\n  dsg_relax_time: ");
                detail.append(bytes2Dec(raw_data, 6, 2));
                detail.append(" s\r\n  chg_relax_time: ");
                detail.append(bytes2Dec(raw_data, 8, 1));
                detail.append(" s\r\n  quit_relax_time: ");
                detail.append(bytes2Dec(raw_data, 9, 1));
                detail.append(" s\r\n  max_ir_correct: ");
                detail.append(bytes2Dec(raw_data, 10, 2));
                detail.append(" mV\r\n");
                strDetail = detail.toString();
                break;

                case 82: // Battery state  data
                detail.append("  qmax_cell_0: ");
                detail.append(bytes2Dec(raw_data, 0, 2));
                detail.append(" mAh\r\n  cycle_count: ");
                detail.append(bytes2Dec(raw_data, 2, 2));
                detail.append("\r\n  update_status: ");
                detail.append(bytes2Hex(raw_data, 4, 1));
                detail.append("  v_at_chg_term: ");
                detail.append(bytes2Dec(raw_data, 5, 2));
                detail.append(" mV\r\n  avg_i_last_run: ");
                detail.append(bytes2Dec(raw_data, 7, 2));
                detail.append(" mA\r\n  avg_p_last_run: ");
                detail.append(bytes2Dec(raw_data, 9, 2));
                detail.append(" mW\r\n  delta_vol: ");
                detail.append(bytes2Dec(raw_data, 11, 2));
                detail.append(" mV\r\n  t_rise: ");
                detail.append(bytes2Dec(raw_data, 15, 2));
                detail.append("\r\n  t_time_constant: ");
                detail.append(bytes2Dec(raw_data, 17, 2));
                detail.append("\r\n");
                strDetail = detail.toString();
                break;

                case 83: // Battery OCV table data
                detail.append("  chem_id: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                //detail.append("\r\n");
                strDetail = detail.toString();
                break;

                default:
                //strDetail = bytes2Hex(raw_data);
                break;
            }
            // wrap up one subclass data
            //strBatt = " "+id+", "+name+", "+len+":  "+ strHex;
            //Log.e(TAG, strBatt);
            builder.append(" ID: ");
            builder.append(id);
            builder.append(", ");
            builder.append(name);
            builder.append(", ");
            builder.append(len);
            builder.append(" bytes:  ");
            builder.append("\r\n");
            builder.append(strDetail);
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
            //Log.e(TAG,"1111111111111111111111111111111111111111111111111 ");
            reader = new FileInputStream(inFile);
            writer = new FileOutputStream(outFile);
            //Copy data and save it
            while((count=reader.read(buf)) != -1 ) {
                Log.e(TAG, "read, count: "+count);
                writer.write(buf, 0, count);
            }
            //Log.e(TAG,"2222222222222222222222222222222222222222222222222 ");

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
        saveBattInfo();
        txtBatt.setText(strBatt);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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
    }
}
