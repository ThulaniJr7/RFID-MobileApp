package demorfid.zebra.atnsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViewInventoryActivity extends AppCompatActivity {

    Connection connection, connection1, connection2;

    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    protected static Context context;
    private ViewInventoryActivity.EventHandler eventHandler;

    ListView listView;
    ArrayList<String> items;
    ArrayList<TagData> tagData;
    ArrayAdapter<String> adapter;

    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private int MAX_POWER = 270;

    private Button buttonReadTags;
    private Button buttonStopReadTags;
    private Button buttonAssetUpdateLocation;
    private EditText editTextAssetLocation;
    TextView textViewId, textView;

    String [] farNumbers = new String[200];
    String [] atnsIdNums = new String[200];
    String [] description = new String[200];
    String [] atnsTagNum = new String[200];
    String [] rfidTagId = new String[200];
    String [] rfidFarNum = new String[200];
    String [] rfidFAtnsIdNum = new String[200];

    @SuppressLint({"MissingInflatedId", "WrongViewCast", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewinventory);
        buttonAssetUpdateLocation = findViewById(R.id.buttonUpdateLocation);
        editTextAssetLocation = findViewById(R.id.editTextAssetLocation);
//        ListView mListView = (ListView) findViewById(R.id.listview);

        listView = findViewById(R.id.listview);
        items = new ArrayList<>();
        tagData = new ArrayList<>();
//        items.add("1st Tag");

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, items);
       // adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, tagData);
        listView.setAdapter(adapter);

        ConSQL c = new ConSQL();
        connection = c.conclass();

        ConSQL c1 = new ConSQL();
        connection1 = c1.conclass();

        if(c != null){
            try{

                // SQL Statement to fetch all stock
                String sqlstatement = "Select * from ATNStock";
                Statement smt = connection.createStatement();
                ResultSet set = smt.executeQuery(sqlstatement);

                // SQL Statement to fetch rfid tag data
                String sqlstatement1 = "Select * from RFidTag";
                Statement smt1 = connection1.createStatement();
                ResultSet set1 = smt1.executeQuery(sqlstatement1);

                while (set.next()){
                    int row = set.getRow() - 1;
                    farNumbers[row] = set.getString(1);
                    atnsIdNums[row] = set.getString(3);
                    description[row] = set.getString(4);
                    atnsTagNum[row] = set.getString(2);
                }

                while (set1.next()){
                    int row1 = set1.getRow() - 1;
                    rfidTagId[row1] = set1.getString(1);
                    rfidFAtnsIdNum[row1] = set1.getString(2);
                    rfidFarNum[row1] = set1.getString(3);
                }

                connection.close();
                connection1.close();
            }
            catch (Exception e){
                Log.e("Error: ", e.getMessage());
            }

        }

        textViewId = (TextView) findViewById(R.id.tagId);
        textView = (TextView) findViewById(R.id.text);

        buttonReadTags = findViewById(R.id.buttonReadTags);
        buttonStopReadTags = findViewById(R.id.buttonStopReadTags);

        readers = new Readers(this, ENUM_TRANSPORT.ALL);

        try {
            ArrayList readersListArray = readers.GetAvailableRFIDReaderList();
            ReaderDevice readerDevice = (ReaderDevice) readersListArray.get(0);
            reader = readerDevice.getRFIDReader();
        } catch (InvalidUsageException e) {
            throw new RuntimeException(e);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    reader.connect();
                    ConfigureReader();
                } catch (InvalidUsageException e) {
                    throw new RuntimeException(e);
                } catch (OperationFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        runnable.run();

        buttonAssetUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String assetLocation = editTextAssetLocation.getText().toString();
                int assetCheck = 0;

                    try{

                        for(int x = 0; x < items.size(); x++){
                            ConSQL c2 = new ConSQL();
                            connection2 = c2.conclass();
                            if(c2 != null)
                            {
                                String tagId = items.get(x);
                                for(int y = 0; y < rfidTagId.length; y++){

                                    String atnsIdNum = rfidTagId[y].toString();
                                    if(tagId.equals(atnsIdNum)){

                                        String rfidATNSIdNum = rfidFAtnsIdNum[y].toString();
                                        String sqlstatement2 = "Update ATNStock set RoomName = '" + assetLocation + "' where ID = '" + rfidATNSIdNum + "'";
                                        Statement smt2 = connection2.createStatement();
                                        smt2.executeUpdate(sqlstatement2);

                                        assetCheck = 1;
                                        break;
                                    }
                                    else{
                                        System.out.println("This Tag ID isn't associated with an asset. Continue...");
                                    }
                                }
                            }
                            connection2.close();
                        }

                    }
                    catch (Exception e){
                        Log.e("Error: ", e.getMessage());
                    }

                if (assetCheck == 1){
                    Toast.makeText(ViewInventoryActivity.this, "The assets have been updated in the database!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ViewInventoryActivity.this, "The assets weren't updated as they aren't linked to an ATNS asset", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void addItem(String item){

        int itemSize = items.size();
        int validation = 0;
        // item is the EPC ID
        String tagNumber = "null";
        String desc = "null";
        for(int num = 0; num < rfidTagId.length; num++){

            if(item.equals(rfidTagId[num])){

                int size = 0;
                if(items.isEmpty()){
                    items.add(item);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
//                    validation = 1;
                }
                else{

//                    System.out.println("Continue");
                    items.add(item);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

            }
            else{
                System.out.println("Continue...");
            }
        }

//        for(int z = 0; z < rfidFAtnsIdNum.length; z++){
//            if(tagNumber.equals(rfidFAtnsIdNum[z])){
//                desc = description[z];
//            }
//        }

    }

    public void stopReadTags(View view) {
        try {
            reader.Actions.Inventory.stop();
            System.out.println("Tag Read stopped");
        } catch (InvalidUsageException e) {
            throw new RuntimeException(e);
        } catch (OperationFailureException e) {
            throw new RuntimeException(e);
        }
    }

    public void readTags(View view) {
        System.out.println("Tag Read initiated");
        try {
            reader.Actions.Inventory.perform();
        } catch (InvalidUsageException e) {
            throw new RuntimeException(e);
        } catch (OperationFailureException e) {
            throw new RuntimeException(e);
        }
    }

    private void ConfigureReader() {
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new ViewInventoryActivity.EventHandler();
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true); // tag event with tag data
                reader.Events.setTagReadEvent(true);
                reader.Events.setInventoryStartEvent(true);
                reader.Events.setInventoryStopEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1; // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(MAX_POWER);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                reader.Config.Antennas.setAntennaRfConfig(1, config); // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.setTagPopulation((short) 30);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                reader.Config.setUniqueTagReport(false);
                reader.Config.setBatchMode(BATCH_MODE.DISABLE);
                reader.Actions.PreFilters.deleteAll();

            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
//                Below is where the Reader is reading the tags and should then be used to show on the screen
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID: " + myTags[index].getTagID());
                    String tagId = myTags[index].getTagID();
//                    Sets the last read Tag ID to the Text View
                    int tagTotal = myTags.length;
                    String tagCount = Integer.toString(tagTotal);
//                    textViewId.setText(tagId);
                    addItem(tagId);
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                }
            }
        }

        // Status Event Notification
//        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
//            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
//        }

        @SuppressLint({"StaticFieldLeak", "SuspiciousIndentation"})
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
                    Log.d(TAG, "The handheld was pressed");
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        handleTriggerPress(true);
                        return null;
                    }
                }.execute();
            }
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        handleTriggerPress(false);
                        return null;
                    }
                }.execute();
            }
        }

        public void handleTriggerPress(boolean pressed) {
//            if (pressed) {
//                System.out.println("Tag Read initiated");
//                try {
//                    reader.Actions.Inventory.perform();
//                } catch (InvalidUsageException e) {
//                    throw new RuntimeException(e);
//                } catch (OperationFailureException e) {
//                    throw new RuntimeException(e);
//                }
//            } else
//                stopInventory();
        }

        synchronized void performInventory() {
            // check reader connection
            if (!isReaderConnected())
                return;
            try {
                reader.Actions.Inventory.perform();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }

        synchronized void stopInventory() {
            try {
                reader.Actions.Inventory.stop();
                System.out.println("Tag Read stopped");
            } catch (InvalidUsageException e) {
                throw new RuntimeException(e);
            } catch (OperationFailureException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isReaderConnected() {
            if (reader != null && reader.isConnected())
                return true;
            else {
                Log.d(TAG, "reader is not connected");
                return false;
            }
        }

    }

}
