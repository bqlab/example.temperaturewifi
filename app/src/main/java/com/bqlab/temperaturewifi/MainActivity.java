package com.bqlab.temperaturewifi;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Room livingRoom;
    Room innerRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        livingRoom = new Room((Button) findViewById(R.id.living_room));
        innerRoom = new Room((Button) findViewById(R.id.inner_room));

        Toast.makeText(this, "버튼을 클릭하여 IP를 등록하세요.", Toast.LENGTH_LONG).show();
    }

    private class Room {
        private int temp;
        private Button view;
        private String ip;
        private Socket socket;
        private Thread thread;
        private Boolean isConnected;
        private BufferedReader reader;

        Room(Button view) {
            this.view = view;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setIP();
                }
            });
        }

        private boolean checkSetIP(String ip) {
            for (int i = 0; i < ip.length(); i++)
                if (!"0123456789.".contains(String.valueOf(ip.charAt(i)))) return false;
            return !ip.isEmpty() && ip.contains(".");
        }

        private void setIP() {
            final EditText e = new EditText(MainActivity.this);
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("IP를 입력하세요.");
            b.setView(e);
            b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (checkSetIP(e.getText().toString())) {
                        Toast.makeText(MainActivity.this, e.getText().toString() + "에 연결합니다.", Toast.LENGTH_SHORT).show();
                        Room.this.ip = e.getText().toString();
                        dialogInterface.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "입력을 다시 확인하세요.", Toast.LENGTH_SHORT).show();
                        setIP();
                    }
                }
            });
            b.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            b.show();
        }

        public void setColor(int temp) {
            if (temp <= 35)
                view.setBackground(getResources().getDrawable(R.color.colorGreen));
            else if (temp <= 80)
                view.setBackground(getResources().getDrawable(R.color.colorYellow));
            else if (temp <= 105) {
                view.setBackground(getResources().getDrawable(R.color.colorRed));
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setMessage("화재가 발생했습니다. 119에 전화를 겁니다.");
                b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.startActivity(new Intent("android.intent.action.CALL", Uri.parse("119")));
                    }
                });
            }
        }

        private class ThreadConnector implements Runnable {
            private String ip;
            private int port;

            ThreadConnector(String ip, int port) {
                this.ip = ip;
                this.port = port;
            }

            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    ip = socket.getRemoteSocketAddress().toString();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
                if (socket != null) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                        isConnected = true;
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            thread = new Thread(new ThreadReceiver());
                            thread.start();
                        } else
                            Toast.makeText(MainActivity.this, "연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private class ThreadReceiver implements Runnable {

            @Override
            public void run() {
                try {
                    while (isConnected) {
                        if (reader == null) break;
                        final int temp = Integer.parseInt(reader.readLine());

                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        }
    }
}
