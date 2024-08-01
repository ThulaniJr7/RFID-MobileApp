package demorfid.zebra.atnsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zebra.rfid.*;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private static RFIDReader reader;
    private static String TAG = "DEMO";

    public Handler mEventHandler = new Handler(Looper.getMainLooper());
    private AsyncTask<Void, Void, String> AutoConnectDeviceTask;
    private static Readers readers;

//    private IEventHandler eventHandler = new IEventHandler();
//    private Function<String, Map<String, Object>> _emit;
//    private EventChannel.EventSink sink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Set a click listener for the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve entered username and password
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);

                // Implement authentication logic here
//                if (username.equals("user") && password.equals("123")) {
//                    // Successful login
//                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                    startActivity(intent);
//                } else {
//                    // Failed login
//                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
//                }
            }
        });

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

    }

}