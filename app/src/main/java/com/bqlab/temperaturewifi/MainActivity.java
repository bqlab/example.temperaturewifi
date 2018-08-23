package com.bqlab.temperaturewifi;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    Room room1;
    Room room2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        room1 = new Room((Button) findViewById(R.id.room1));
        room2 = new Room((Button) findViewById(R.id.room2));

        Toast.makeText(this, "버튼을 클릭하여 이름과 IP를 등록하세요.", Toast.LENGTH_LONG).show();
    }

    private class Room {
        private String ip;
        private String name;
        private Button view;
        private Socket socket;
        private Thread thread;
        private BufferedReader reader;

        int temp = 0;
        boolean isConnected = false;

        Room(Button view) {
            this.view = view;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setRoom();
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
                        new Thread(new ThreadConnector(e.getText().toString(), 8090)).start();
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

        private void setRoom() {
            final EditText e = new EditText(MainActivity.this);
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("이름을 입력하세요.");
            b.setView(e);
            b.setPositiveButton("다음", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Room.this.name = e.getText().toString();
                    Room.this.view.setText(Room.this.name);
                    dialogInterface.dismiss();
                }
            });
            b.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            b.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    setIP();
                }
            });
            b.show();
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
                String TAG = "tcp";

                try {
                    socket = new Socket(ip, port);
                    ThreadConnector.this.ip = socket.getRemoteSocketAddress().toString();
                } catch (UnknownHostException e) {
                    Log.d(TAG, "호스트를 찾을 수 없습니다.");
                } catch (SocketTimeoutException e) {
                    Log.d(TAG, "연결 시간이 초과되었습니다.");
                } catch (Exception e) {
                    Log.e(TAG, ("에러가 발생했습니다."));
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
                            Toast.makeText(MainActivity.this, "서버와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private class ThreadReceiver implements Runnable {
            @Override
            public void run() {
                try {
                    while (isConnected) {
                        temp = Integer.parseInt(reader.readLine());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (temp <= 35) {
                                    view.setBackground(getResources().getDrawable(R.color.colorGreen));
                                    view.setText(getString(R.string.normal, Room.this.name, Room.this.temp));
                                } else if (temp <= 80) {
                                    view.setBackground(getResources().getDrawable(R.color.colorYellow));
                                    view.setText(getString(R.string.overheat, Room.this.name, Room.this.temp));
                                } else if (temp <= 105) {
                                    view.setBackground(getResources().getDrawable(R.color.colorRed));
                                    view.setText(getString(R.string.fire, Room.this.name, Room.this.temp));
                                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                                    b.setMessage("화재가 발생했습니다. 119에 전화를 겁니다.");
                                    b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            MainActivity.this.startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:119")));
                                        }
                                    });
                                }
                            }
                        });
                        reader = null;
                        socket.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            }
        }
    }
}
