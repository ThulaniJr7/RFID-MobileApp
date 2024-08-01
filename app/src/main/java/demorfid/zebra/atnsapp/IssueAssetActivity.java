package demorfid.zebra.atnsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IssueAssetActivity extends AppCompatActivity {

    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    protected static Context context;
    private IssueAssetActivity.EventHandler eventHandler;

    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private int MAX_POWER = 270;
    private Button buttonReadTags;
    private Button buttonStopReadTag;
    private Button buttonSubmitAsset;
    private EditText editTextTagId, editTextTagNumber, editTextItemDesc, editTextResPerson, editTextAtnsID, editTextFarNum;
    TextView textViewId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_asset);

        buttonReadTags = findViewById(R.id.buttonReadTag);
        buttonStopReadTag = findViewById(R.id.buttonStopReadTag);
        buttonSubmitAsset = findViewById(R.id.buttonSubmitForm);
        textViewId = (TextView) findViewById(R.id.tagId);

        editTextTagId = findViewById(R.id.editTextTagId);
        editTextTagNumber = findViewById(R.id.editTextTagNumber);
        editTextItemDesc = findViewById(R.id.editTextDescription);
        editTextResPerson = findViewById(R.id.editTextResPerson);
        editTextAtnsID = findViewById(R.id.editTextAtnsID);
        editTextFarNum = findViewById(R.id.editTextFarNum);

        readers = new Readers(this, ENUM_TRANSPORT.ALL);

//        OkHttpClient client = new OkHttpClient();
        String url = "http://10.1.21.185/Home/IssueNewAsset";

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

//        Json body which should be sent to add a new asset

//    {
//        "rfidTagId" : "123456",
//        "rfidTagNum" : "000000000351665",
//        "rfidDesc" : "Laptop Bag",
//        "rfidResPerson" : "Thulani",
//        "rfidATNSId" : "ASF56V1D68G1SFD56162VHJ5",
//        "rfidFarNum" : "FAR1001-908789"
//    }

        // The endpoint to be used to add a new asset:
        // 10.1.21.185/Home/IssueNewAsset

        // Set a click listener for the Form Submission button
        buttonSubmitAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                 Retrieve relevant fields within the form
//                String tagId = editTextTagId.getText().toString();
//                String tagId = "123456";
                String tagNum = editTextTagNumber.getText().toString();
                String itemDescription = editTextItemDesc.getText().toString();
                String resPerson = editTextResPerson.getText().toString();
                String atnsId = editTextAtnsID.getText().toString();
                String farNum = editTextFarNum.getText().toString();
                String rfidTagId = textViewId.getText().toString();

                if (rfidTagId != null && tagNum != null && itemDescription != null && resPerson != null && atnsId != null && farNum != null) {

                    try{

                        postData(rfidTagId, tagNum, itemDescription, resPerson, atnsId, farNum);

                    }catch (Exception ex){

                        // Implement code to submit information into
                        Toast.makeText(IssueAssetActivity.this, "Error: Asset wasn't loaded. Please try again!" + ex.getMessage(), Toast.LENGTH_SHORT).show();

                    }
//                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                    startActivity(intent);
                } else {

                    Toast.makeText(IssueAssetActivity.this, "Please ensure all fields are filled in", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void postData(String tagID, String tagNumber, String description, String responsiblePeson, String atnsID, String fNum){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.1.21.185/Home/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
//        // below line is to create an instance for our retrofit api class.
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        // passing data from our text fields to our modal class.
        UserAsset userAsset = new UserAsset(tagID, tagNumber, description, responsiblePeson, atnsID, fNum);

        // calling a method to create a post and passing our modal class.
        Call<UserAsset> call = retrofitAPI.createPost(userAsset);

        call.enqueue(new Callback<UserAsset>() {
            @Override
            public void onResponse(Call<UserAsset> call, Response<UserAsset> response) {
                // this method is called when we get response from our api.
                Toast.makeText(IssueAssetActivity.this, "Data added to Asset", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<UserAsset> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Toast.makeText(IssueAssetActivity.this, "Error Adding Data", Toast.LENGTH_SHORT).show();

            }
        });

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
                    eventHandler = new IssueAssetActivity.EventHandler();
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
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                }
            }
        }

//         Status Event Notification
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
            if (pressed) {
                System.out.println("Tag Read initiated");
                try {
                    reader.Actions.Inventory.perform();
                } catch (InvalidUsageException e) {
                    throw new RuntimeException(e);
                } catch (OperationFailureException e) {
                    throw new RuntimeException(e);
                }
            } else
                stopInventory();
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


    }

}