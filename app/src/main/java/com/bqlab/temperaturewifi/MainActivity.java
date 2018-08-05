package com.bqlab.temperaturewifi;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button livingRoom;
    private Button innerRoom;

    private String server = null;
    private Socket socket = null;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thread;

    private int temp;
    private Boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        livingRoom = (Button) findViewById(R.id.living_room);
        innerRoom = (Button) findViewById(R.id.inner_room);

        inputIP();
    }

    public void inputIP() {
        final EditText e = new EditText(MainActivity.this);
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage("IP를 입력하세요.");
        b.setView(e);
        b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                server = e.getText().toString();
                Toast.makeText(MainActivity.this, server, Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        b.show();
    }

    public void disconected() {
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setMessage("연결이 끊어졌습니다.");
        b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "재접속합니다.", Toast.LENGTH_LONG).show();
            }
        });
        b.show();
    }

    private class ConnectThread implements Runnable {
        String ip;
        int port;

        ConnectThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

        }
    }
}
