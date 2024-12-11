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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AssetCheckingActivity extends AppCompatActivity {

    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    protected static Context context;
    private AssetCheckingActivity.EventHandler eventHandler;
    Connection connection, connection1, connection2;

    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private int MAX_POWER = 270;
    private RadioGroup radioGroup;
    ArrayList<String> items;
    private Button buttonReadTags;
    private Button buttonSubmitAssetCheck;
    private Button buttonStopReadTags;
    private EditText editTextTagId, editTextTagNumber, editTextItemDesc, editTextResPerson, editTextFarNum, editTextAtnsID;
    TextView textViewId;

    String [] farNumbers = new String[30000];
    String [] atnsIdNums = new String[30000];
    String [] description = new String[30000];
    String [] resPerson = new String[30000];
    String [] rfidTagId = new String[30000];
    String [] rfidFarNum = new String[30000];
    String [] rfidFAtnsIdNum = new String[30000];


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_checking);

        buttonReadTags = findViewById(R.id.buttonReadTag);
        buttonStopReadTags = findViewById(R.id.buttonStopReadTags);
        radioGroup = (RadioGroup)findViewById(R.id.groupradio);
        buttonSubmitAssetCheck = findViewById(R.id.buttonSubmitForm);
//        editTextTagId = findViewById(R.id.editTextTagId);
        editTextTagNumber = findViewById(R.id.editTextTagNumber);
        editTextItemDesc = findViewById(R.id.editTextDescription);
        editTextResPerson = findViewById(R.id.editTextResPerson);
        editTextAtnsID = findViewById(R.id.editTextAtnsID);
        editTextFarNum = findViewById(R.id.editTextFarNum);
        items = new ArrayList<>();
        textViewId = (TextView) findViewById(R.id.tagId);


        readers = new Readers(this, ENUM_TRANSPORT.ALL);

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
                    resPerson[row] = set.getString(16);
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

        radioGroup.setOnCheckedChangeListener(
                new RadioGroup
                        .OnCheckedChangeListener() {
                    @Override

                    // Check which radio button has been clicked
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {

                        // Get the selected Radio Button
                        RadioButton
                                radioButton
                                = (RadioButton)group
                                .findViewById(checkedId);
                    }
                });

        buttonSubmitAssetCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int assetCheck = 0;
                String astCheck = "In";

                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(AssetCheckingActivity.this,
                                    "No answer has been selected",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
                else {

                    RadioButton radioButton
                            = (RadioButton)radioGroup
                            .findViewById(selectedId);

                    astCheck = radioButton.getText().toString();
                    // Now display the value of selected item
                    // by the Toast message
                    Toast.makeText(AssetCheckingActivity.this,
                                    radioButton.getText(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }

                String tagNum = editTextTagNumber.getText().toString();
                String atnsId = editTextAtnsID.getText().toString();
                String rfidTagId = textViewId.getText().toString();

                if (tagNum != null && atnsId != null)
                {

                    ConSQL c2 = new ConSQL();
                    connection2 = c2.conclass();

                    try{
                        if(c2 != null){

                            String sqlstatement2 = "Update ATNStock set AssetCheck = " + astCheck + " where ID = " + atnsId;
                            Statement smt2 = connection2.createStatement();
                            smt2.executeUpdate(sqlstatement2);

                            connection2.close();
                            assetCheck = 1;

                        }
                    }
                    catch (Exception e){
                        Log.e("Error: ", e.getMessage());
                    }

                } else {

                    Toast.makeText(AssetCheckingActivity.this, "Please ensure all fields are filled in", Toast.LENGTH_SHORT).show();
                }

                if (assetCheck == 1){
                    Toast.makeText(AssetCheckingActivity.this, "The asset has been Checked In/Out!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(AssetCheckingActivity.this, "The assets weren't updated as they aren't linked to an ATNS asset", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void addItem(String item){
        for(int num = 0; num < rfidTagId.length; num++){
            if(item.equals(rfidTagId[num])){

                String desc = description[num].toString();
                String resP = resPerson[num].toString();

//                editTextTagId.setText(item);
                editTextItemDesc.setText(desc);
                editTextResPerson.setText(resP);

            }
            else{
                System.out.println("Continue...");
            }
        }
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
                    eventHandler = new AssetCheckingActivity.EventHandler();
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
                    textViewId.setText(tagId);
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