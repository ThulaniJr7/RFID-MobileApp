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

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AssetCheckingActivity extends AppCompatActivity {

    String [] item = {"Check In", "Check Out"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    protected static Context context;
    private AssetCheckingActivity.EventHandler eventHandler;

    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private int MAX_POWER = 270;

    private Button buttonReadTags;
    private Button buttonSubmitAssetCheck;
    private Button buttonStopReadTags;
    private EditText editTextTagId, editTextTagNumber, editTextItemDesc, editTextResPerson, editTextFarNum, editTextAtnsID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_checking);


        buttonSubmitAssetCheck = findViewById(R.id.buttonSubmitForm);
        editTextTagId = findViewById(R.id.editTextTagId);
        editTextTagNumber = findViewById(R.id.editTextTagNumber);
        editTextItemDesc = findViewById(R.id.editTextDescription);
        editTextResPerson = findViewById(R.id.editTextResPerson);
        editTextAtnsID = findViewById(R.id.editTextAtnsID);
        editTextFarNum = findViewById(R.id.editTextFarNum);

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
//        String item = "in";

        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, item);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(AssetCheckingActivity.this, "Item: " + item, Toast.LENGTH_SHORT).show();
            }
        });

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

        // This is the Asset Check In Page

//        xml body which should be sent when doing an Asset check in/out

//        <?xml version="1.0" encoding="utf-8" ?>
//        <XML timestamp="2002-08-15T08:36:47-07:00">
//            <Asset>
//                <RFIDTagID>065650656</RFIDTagID>
//                <TagNumber>Far02445042</TagNumber>
//                <Description>Laptop Bag</Description>
//                <ResPerson>John Smith</ResPerson>
//                <ATNSID>16sfdf65v1dg6fbvdbvs</ATNSID>
//                <AssetCheck>Out</AssetCheck>
//            </Asset>
//        </XML>

//         The endpoint to be used:
//         10.1.21.185/Home/AssetCheckIn
//
//         The endpoint to be used to retrieve inventory data:
//         10.1.21.185/Home/RetrieveAssetList

        buttonSubmitAssetCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagNum = editTextTagNumber.getText().toString();
                String atnsId = editTextAtnsID.getText().toString();

                if (tagNum != null && atnsId != null && item != null) {

                    try{

//                        postData(tagNum, atnsId, item);

                    }catch (Exception ex){

                        // Implement code to submit information into
                        Toast.makeText(AssetCheckingActivity.this, "Error: Asset wasn't loaded. Please try again!" + ex.getMessage(), Toast.LENGTH_SHORT).show();

                    }
//                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                    startActivity(intent);
                } else {

                    Toast.makeText(AssetCheckingActivity.this, "Please ensure all fields are filled in", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void postData(String tagNumber, String atnsID, String assetCheck){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.1.21.185/Home/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
//        // below line is to create an instance for our retrofit api class.
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        // passing data from our text fields to our modal class.
        AssetCheck assetCheck1 = new AssetCheck(tagNumber, atnsID, assetCheck);

        // calling a method to create a post and passing our modal class.
        Call<AssetCheck> call = retrofitAPI.createPost(assetCheck1);

        call.enqueue(new Callback<AssetCheck>() {
            @Override
            public void onResponse(Call<AssetCheck> call, Response<AssetCheck> response) {
                // this method is called when we get response from our api.
                Toast.makeText(AssetCheckingActivity.this, "Data added to Asset", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<AssetCheck> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Toast.makeText(AssetCheckingActivity.this, "Error Adding Data", Toast.LENGTH_SHORT).show();

            }
        });

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
//                    textViewId.setText(tagId);
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