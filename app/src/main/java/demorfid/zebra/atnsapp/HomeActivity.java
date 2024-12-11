package demorfid.zebra.atnsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    private Button buttonRfid;

    private Button buttonAsset;

    private Button buttonIssue;

    private Button buttonInventory;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        buttonIssue = findViewById(R.id.buttonIssue);
        buttonAsset = findViewById(R.id.buttonAssetChecking);
        buttonRfid = findViewById(R.id.buttonReadTag);
        buttonIssue = findViewById(R.id.buttonIssueAsset);
        buttonInventory = findViewById(R.id.buttonViewInventory);

        // Set a click listener for the RFID Read button
        buttonRfid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, RFIDReadActivity.class);
                startActivity(intent);

            }
        });

        // Set a click listener for the Asset Checking button
        buttonAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, AssetCheckingActivity.class);
                startActivity(intent);

            }
        });

        // Set a click listener for the Issue Asset button
        buttonIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, IssueAssetActivity.class);
                startActivity(intent);

            }
        });

        // Set a click listener for the View Inventory button
        buttonInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, ViewInventoryActivity.class);
                startActivity(intent);

            }
        });


    }

}