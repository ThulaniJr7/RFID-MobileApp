package demorfid.zebra.atnsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;

public class RFIDReadActivity extends AppCompatActivity {

    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    protected static Context context;
    private EventHandler eventHandler;

    private ArrayList<String> items;
    private ArrayAdapter<String> adapter;
//    private Listview listview;
    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private int MAX_POWER = 270;

    private Button buttonReadTags;
    private Button buttonStopReadTags;

    TextView textViewId, textView;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidread);

//        listview = findViewById(R.id.listview);
        textViewId = (TextView) findViewById(R.id.tagId);
        textView = (TextView) findViewById(R.id.text);

        buttonReadTags = findViewById(R.id.buttonReadTags);
        buttonStopReadTags = findViewById(R.id.buttonStopReadTags);

        readers = new Readers(this, ENUM_TRANSPORT.ALL);

        try {
            ArrayList readersListArray = readers.GetAvailableRFIDReaderList();
            ReaderDevice readerDevice = (ReaderDevice) readersListArray.get(0);
            reader = readerDevice.getRFIDReader();
            // It works until you have to connect to the reader. It should be done in a Main UI Thread
            //reader.connect();
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

    }

    // This is the endpoint to fetch the RFID List:
    // 10.1.21.185/Home/RetrieveRFIDList

    //
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
                    eventHandler = new EventHandler();
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