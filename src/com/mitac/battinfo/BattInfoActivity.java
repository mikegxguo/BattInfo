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

    public static String bytes2Hex(byte[] bytes, int start, int len) {
        StringBuffer sb = new StringBuffer();
        sb.append("    "); //TODO: the blank is friendly for user.
        for(int i = start; i < start+len; i++) {
            sb.append("0x");
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
                detail.append("  ot_dsg_recovery: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  ot_dsg_time: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  ot_dsg: ");
                detail.append(bytes2Hex(raw_data, 3, 2));
                detail.append("  ot_chg_recovery: ");
                detail.append(bytes2Hex(raw_data, 5, 2));
                detail.append("  ot_chg_time: ");
                detail.append(bytes2Hex(raw_data, 7, 1));
                detail.append("  ot_chg: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                strDetail = detail.toString();
                break;

                case 32: //inhibit cfg
                detail.append("  temp_hys: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  chg_inhibit_temp_h: ");
                detail.append(bytes2Hex(raw_data, 2, 2));
                detail.append("  chg_inhibit_temp_l: ");
                detail.append(bytes2Hex(raw_data, 4, 2));
                strDetail = detail.toString();
                break;

                case 34: //Battery charge data
                detail.append("  charging_vol: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                strDetail = detail.toString();
                break;

                case 36: //Battery charge termination data
                detail.append("  dod_eoc_delta_t: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  fc_clear: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  fc_set: ");
                detail.append(bytes2Hex(raw_data, 3, 1));
                detail.append("  tca_clear: ");
                detail.append(bytes2Hex(raw_data, 4, 1));
                detail.append("  tca_set:");
                detail.append(bytes2Hex(raw_data, 5, 1));
                detail.append("  cur_taper_window: ");
                detail.append(bytes2Hex(raw_data, 6, 1));
                detail.append("  taper_vol: ");
                detail.append(bytes2Hex(raw_data, 7, 2));
                detail.append("  min_taper_cap: ");
                detail.append(bytes2Hex(raw_data, 9, 2));
                detail.append("  taper_cur: ");
                detail.append(bytes2Hex(raw_data, 11, 2));
                strDetail = detail.toString();
                break;

                case 48: //Battery data FIXME: length:57, but read 38 bytes.
                detail.append("  design_energy_scale: ");
                detail.append(bytes2Hex(raw_data, 0, 1));
                detail.append("  min_isd_time: ");
                detail.append(bytes2Hex(raw_data, 1, 1));
                detail.append("  isd_i_filter: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  isd_cur: ");
                detail.append(bytes2Hex(raw_data, 3, 2));
                detail.append("  tdd_soh_per: ");
                detail.append(bytes2Hex(raw_data, 5, 1));
                detail.append("  soh_load_i: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("  design_energy: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  design_capacity: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                detail.append("  cc_threshold: ");
                detail.append(bytes2Hex(raw_data, 12, 2));
                detail.append("  cycle_count: ");
                detail.append(bytes2Hex(raw_data, 14, 2));
                detail.append("  reserved: ");
                detail.append(bytes2Hex(raw_data, 16, 6));
                detail.append("  initial_maxload: ");
                detail.append(bytes2Hex(raw_data, 22, 2));
                detail.append("  initial_standby: ");
                detail.append(bytes2Hex(raw_data, 24, 1));
                detail.append("  rem_cap_alarm: ");
                detail.append(bytes2Hex(raw_data, 25, 1));
                detail.append("  device_name: ");
                detail.append(bytes2Hex(raw_data, 26, 12));
                strDetail = detail.toString();
                break;

                case 49: //Battery discharge
                detail.append("  bh_clear_volt_threshold: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  bh_volt_time: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  bh_set_volt_threshold: ");
                detail.append(bytes2Hex(raw_data, 3, 2));
                detail.append("  bl_clear_volt_threshold: ");
                detail.append(bytes2Hex(raw_data, 5, 2));
                detail.append("  bl_set_volt_time: ");
                detail.append(bytes2Hex(raw_data, 7, 1));
                detail.append("  bl_set_volt_threshold: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  socf_clear_threshold: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                detail.append("  socf_set_threshold: ");
                detail.append(bytes2Hex(raw_data, 12, 2));
                detail.append("  soc1_clear_threshold: ");
                detail.append(bytes2Hex(raw_data, 14, 2));
                detail.append("  soc1_set_threshold: ");
                detail.append(bytes2Hex(raw_data, 16, 2));
                strDetail = detail.toString();
                break;

                case 56: //Battery manufacturer data
                detail.append("  df_cfg_ver: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  cell_revision: ");
                detail.append(bytes2Hex(raw_data, 2, 2));
                detail.append("  hardware_revision: ");
                detail.append(bytes2Hex(raw_data, 4, 2));
                detail.append("  firmware_ver: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("  pcb_lot_code: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  pack_lot_code: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                strDetail = detail.toString();
                break;

                case 57: //Battery integrity data
                detail.append("  chem_df_checksum: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                strDetail = detail.toString();
                break;

                case 59: //Battery lifetime data
                detail.append("  max_dsg_cur: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  max_chg_cur: ");
                detail.append(bytes2Hex(raw_data, 2, 2));
                detail.append("  min_pack_vol: ");
                detail.append(bytes2Hex(raw_data, 4, 2));
                detail.append("  max_pack_vol: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("  min_temp: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  max_temp: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                strDetail = detail.toString();
                break;

                case 60: // Battery lifetime temp samples data
                detail.append("  lt_flash_cnt: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                strDetail = detail.toString();
                break;

                case 64: // Battery registers data
                detail.append("  pack_cfg_c: ");
                detail.append(bytes2Hex(raw_data, 0, 1));
                detail.append("  pack_cfg_b: ");
                detail.append(bytes2Hex(raw_data, 1, 1));
                detail.append("  pack_cfg: ");
                detail.append(bytes2Hex(raw_data, 2, 2));
                strDetail = detail.toString();
                break;

                case 66: // Battery lifetime resolution data
                detail.append("  lt_update_time: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  lt_cur_res: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  lt_v_res: ");
                detail.append(bytes2Hex(raw_data, 3, 1));
                detail.append("  lt_temp_res: ");
                detail.append(bytes2Hex(raw_data, 4, 1));
                strDetail = detail.toString();
                break;

                case 68: // Battery power data
                detail.append("  fs_wait: ");
                detail.append(bytes2Hex(raw_data, 0, 1));
                detail.append("  hiber_v: ");
                detail.append(bytes2Hex(raw_data, 1, 2));
                detail.append("  hiber_i: ");
                detail.append(bytes2Hex(raw_data, 3, 2));
                detail.append("  reserved: ");
                detail.append(bytes2Hex(raw_data, 5, 7));
                detail.append("  sleep_cur: ");
                detail.append(bytes2Hex(raw_data, 12, 2));
                detail.append("  flash_update_ok_vol: ");
                detail.append(bytes2Hex(raw_data, 14, 2));
                strDetail = detail.toString();
                break;

                case 80: // Battery IT cfg data FIXME: 105, but read only 100 bytes
                detail.append("  chg_hys_v_shift: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  fast_scale_start_soc: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  reserved1: ");
                detail.append(bytes2Hex(raw_data, 3, 4));
                detail.append("  delta_v_max_delta: ");
                detail.append(bytes2Hex(raw_data, 7, 2));
                detail.append("  qmax_max_delta: ");
                detail.append(bytes2Hex(raw_data, 9, 1));
                detail.append("  ra_max_delta: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                detail.append("  min_sim_rate: ");
                detail.append(bytes2Hex(raw_data, 12, 1));
                detail.append("  max_sim_rate: ");
                detail.append(bytes2Hex(raw_data, 13, 1));
                detail.append("  min_delta_v: ");
                detail.append(bytes2Hex(raw_data, 14, 2));
                detail.append("  max_delta_v: ");
                detail.append(bytes2Hex(raw_data, 16, 2));
                detail.append("  max_scale_back_grid: ");
                detail.append(bytes2Hex(raw_data, 18, 1));
                detail.append("  reserve_energy: ");
                detail.append(bytes2Hex(raw_data, 19, 2));
                detail.append("  reserve_cap_mah: ");
                detail.append(bytes2Hex(raw_data, 21, 2));
                detail.append("  user_rate_pwr: ");
                detail.append(bytes2Hex(raw_data, 23, 2));
                detail.append("  user_rate_ma: ");
                detail.append(bytes2Hex(raw_data, 25, 2));
                detail.append("  restrelax_time: ");
                detail.append(bytes2Hex(raw_data, 27, 2));
                detail.append("  term_v_delta: ");
                detail.append(bytes2Hex(raw_data, 29, 2));
                detail.append("  term_vol: ");
                detail.append(bytes2Hex(raw_data, 31, 2));
                detail.append("  reserved2: ");
                detail.append(bytes2Hex(raw_data, 33, 40));
                detail.append("  ra_filter: ");
                detail.append(bytes2Hex(raw_data, 73, 2));
                detail.append("  reserved3: ");
                detail.append(bytes2Hex(raw_data, 75, 2));
                detail.append("  min_res_factor: ");
                detail.append(bytes2Hex(raw_data, 77, 1));
                detail.append("  max_res_factor: ");
                detail.append(bytes2Hex(raw_data, 78, 1));
                detail.append("  reserved4: ");
                detail.append(bytes2Hex(raw_data, 79, 19));
                detail.append("  load_mode: ");
                detail.append(bytes2Hex(raw_data, 98, 1));
                detail.append("  load_select: ");
                detail.append(bytes2Hex(raw_data, 99, 1));
                strDetail = detail.toString();
                break;

                case 81: // Battery current thresholds  data
                detail.append("  max_ir_correct: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  quit_relax_time: ");
                detail.append(bytes2Hex(raw_data, 2, 1));
                detail.append("  chg_relax_time: ");
                detail.append(bytes2Hex(raw_data, 3, 1));
                detail.append("  dsg_relax_time: ");
                detail.append(bytes2Hex(raw_data, 4, 2));
                detail.append("  quit_current: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("  chg_cur_threshold: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  dsg_cur_threshold: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                strDetail = detail.toString();
                break;

                case 82: // Battery state  data
                detail.append("  t_time_constant: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                detail.append("  t_rise: ");
                detail.append(bytes2Hex(raw_data, 2, 2));
                detail.append("  reserved: ");
                detail.append(bytes2Hex(raw_data, 4, 2));
                detail.append("  delta_vol: ");
                detail.append(bytes2Hex(raw_data, 6, 2));
                detail.append("  avg_p_last_run: ");
                detail.append(bytes2Hex(raw_data, 8, 2));
                detail.append("  avg_i_last_run: ");
                detail.append(bytes2Hex(raw_data, 10, 2));
                detail.append("  v_at_chg_term: ");
                detail.append(bytes2Hex(raw_data, 12, 2));
                detail.append("  update_status: ");
                detail.append(bytes2Hex(raw_data, 14, 1));
                detail.append("  cycle_count: ");
                detail.append(bytes2Hex(raw_data, 15, 2));
                detail.append("  qmax_cell_0: ");
                detail.append(bytes2Hex(raw_data, 17, 2));
                strDetail = detail.toString();
                break;

                case 83: // Battery OCV table data
                detail.append("  chem_id: ");
                detail.append(bytes2Hex(raw_data, 0, 2));
                strDetail = detail.toString();
                break;

                default:
                strDetail = bytes2Hex(raw_data);
                break;
            }
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
        //txtInfo.setText("OK");
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
