package dpkc.gopigo_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    public BluetoothSocket socket = null;
    ArrayList<HashMap<String,String>> aList_db;
    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseManager = new DatabaseManager( this);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() throws IOException
    {
        initListView();
        initSocket();
    }

    private void initListView()
    {
        ListView lv_db = (ListView) findViewById(R.id.lv_db);
        aList_db = new ArrayList<>();
        databaseManager.open();

        int nbDataInDb = databaseManager.getNbDataInDb();
        if( nbDataInDb > 0)
        {
            int nbMaxToPrint = nbDataInDb > 5 ? 5 : nbDataInDb;

            for( int i = 0; i < nbMaxToPrint; i++ )
            {
                HashMap<String, String> map = new HashMap<String, String>();
                int currentId = nbDataInDb-i;
                String currentNbCircles = databaseManager.getNbCirclesWithIndex(currentId);
                map.put("id", String.valueOf(currentId));
                map.put("nb", currentNbCircles + " cercle(s)");
                aList_db.add(map);
            }
        }

        final SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(), aList_db, R.layout.lv_layout,
                new String[] {"id", "nb"},
                new int[]{R.id.tv_dbId, R.id.tv_dbNb});

        lv_db.setAdapter( mSchedule );
        databaseManager.close();
    }

    private void initSocket() throws IOException
    {
        String macAddress = "B8:27:EB:49:FA:1E";
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if( !bluetoothAdapter.isEnabled() )
        {
            bluetoothAdapter.enable();
            SystemClock.sleep(5000);
            if(!bluetoothAdapter.isEnabled()) throw new IOException("Bluetooth is off");
        }

        if(!BluetoothAdapter.checkBluetoothAddress(macAddress)) throw new IOException("Error MAC address");
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        ParcelUuid[] uuids = device.getUuids();

        try {
            socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
            socket = (BluetoothSocket)m.invoke(device,1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        socket.connect();
    }

    public void clickOnStartBtn(View v)
    {
        String message = "sendNbCircles";

        try {
            sendData(message);
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveData() throws IOException
    {
        new Thread(new Runnable() {
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;

            @Override
            public void run() {

                try{
                    bytes = socket.getInputStream().read(buffer, bytes, BUFFER_SIZE - bytes);
                    String newData = new String(buffer, "UTF-8");
                    newData = newData.substring(0,bytes);

                    if(!newData.equals("0")) {
                        databaseManager.open();
                        databaseManager.insertNbCircles(newData);
                        String message = newData + " receptionné";
                        System.out.println(message);
                        databaseManager.close();
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendData(String message) throws IOException
    {
        socket.getOutputStream().write(message.getBytes(), 0, message.getBytes().length);
        socket.getOutputStream().flush();
        System.out.println("message envoyé");
    }
}