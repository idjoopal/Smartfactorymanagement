package com.example.administrator.smart_factory;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.administrator.smart_factory.MainActivity.handler;


public class MainActivity extends AppCompatActivity {
    public static Button btn1, btn3; // Button 클레스를 전역 함수로 선언
    public static TextView tv_red,tv_blue,tv_total,tv_accu,tv_timer,tv_motor, tv_motor_total, tv_msg; // TextView 클레스를 전역 함수로 선언
    public static ImageView iv_connect, iv_start;
    public static int red,blue, motor, motor_total, seconds; //int red,blue
    public static ProgressBar pb;

    private BluetoothAdapter mBtAdapter; //BluetoothAdapter 클레스를 전역 함수로 선언
    private BluetoothSocket mSocket; //BluetoothSocket 클레스를 전역 함수로 선언
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //블루투스 UUID 값
    public static OutputStream mOutStream; //OutputStream 클레스를 전역 함수로 선언
    public static Context mContext; //mContext 클레스를 전역 선언
    public static Handler handler = new Handler(); //핸들러 클레스를 전역 선언
    public static String Readmessage; //String 클레스를 전역 선언
    public static boolean isConnect = false;
    public static boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set Content View 앱에서 표시할 layout을 선택할 수 있는 함수 입니다.
        setContentView(R.layout.activity_main);

        // layout에서 배치한 Button과 textview를 각각 btn,tv 이라는 이름으로 선언 합니다. (findViewById : 메소드를 이용하여)
        // 화면에 배치한 컴포넌트의 고유 ID 값을 찾아 선언할 수 있습니다.)
        btn1 = (Button) findViewById(R.id.bt1); //연결
        //btn2 = (Button) findViewById(R.id.bt2); //연결끊기
        btn3 = (Button) findViewById(R.id.bt3); //물품분류시작
        //btn3.setEnabled(false);

        tv_red = (TextView) findViewById(R.id.tv_red); //양품
        tv_blue = (TextView) findViewById(R.id.tv_blue); //불량
        tv_total= (TextView) findViewById(R.id.tv_total); //총 개수
        tv_accu = (TextView) findViewById(R.id.tv_accu); //정확도
        tv_timer = (TextView) findViewById(R.id.tv_timer); //시간
        tv_motor = (TextView) findViewById(R.id.tv_motor_cnt); //모터 횟수
        tv_motor_total = (TextView) findViewById(R.id.tv_motor_total); //모터 누적 횟수

        tv_msg = (TextView) findViewById(R.id.tv7); //안내 메시지

        iv_connect = (ImageView) findViewById(R.id.iv_connect);
        iv_start = (ImageView)findViewById(R.id.iv_start);

        iv_connect.setImageDrawable(getResources().getDrawable(R.mipmap.deletemark));
        iv_start.setImageDrawable(getResources().getDrawable(R.mipmap.deletemark));

        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);

        //mBtAdapter 기본 설정으로 지정 후 BluetoothDevice 클레스를 mdevice로 선언 후
        // EV3의 주소를 입력합니다. (각 EV3는 기기별로 고유의 주소 값을 가지고 있습니다.)
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice mdevice = mBtAdapter.getRemoteDevice("00:16:53:45:03:07");

        this.mContext = this; //mContext에 화면을 지정

        //Button 클릭 Listener 작성
        //Button 을 클릭하면 아래와 같은 동작을 합니다.
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Button 을 클릭하면 EV3 와 연결을 합니다.
                if(!isConnect){
                    //TODO: 연결되어있지 않으면? 연결해야된다.

                    pb.setVisibility(View.VISIBLE);
                    isConnect = true;
                    btn1.setText("연결해제");
                    //iv_connect.setImageResource(R.mipmap.supply);
                    iv_connect.setImageDrawable(getResources().getDrawable(R.mipmap.supply));

                    try {
                        //블루투스 소켓 통신 활성화
                        mSocket = mdevice.createRfcommSocketToServiceRecord(MY_UUID);
                        mSocket.connect();
                        Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_SHORT).show();
                        //BluetoothSocket Read Class Thread 활성화
                        BluetoothSocketRead SocketRead = new BluetoothSocketRead(mSocket); //읽기 클래스 동작 활성화
                        mOutStream = mSocket.getOutputStream();
                        SocketRead.start();

                    } catch (Exception e) {
                        //연결 실패 시 메시지
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    //TODO: 연결되어있던 상태면? 연결을 끊는다.
                    pb.setVisibility(View.INVISIBLE);
                    isConnect = false;
                    btn1.setText("연결");
                    iv_connect.setImageDrawable(getResources().getDrawable(R.mipmap.deletemark));

                    MessageSend("exit1" + "\n");
                    isStart = false;
                    btn3.setText("물품 분류 시작");

                    try {
                        //블루투스 소켓 통신 활성화
                        mSocket = mdevice.createRfcommSocketToServiceRecord(MY_UUID);
                        mSocket.connect();
                        Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_SHORT).show();
                        //BluetoothSocket Read Class Thread 활성화
                        BluetoothSocketRead SocketRead = new BluetoothSocketRead(mSocket); //읽기 클래스 동작 활성화
                        mOutStream = mSocket.getOutputStream();
                        SocketRead.start();
                    } catch (Exception e) {
                        //연결 실패 시 메시지
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //Button 클릭 Listener 작성
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isStart) {
                    MainActivity.red=0;
                    MainActivity.blue=0;
                    MainActivity.motor=0;
                    MainActivity.seconds=0;

                    MainActivity.tv_msg.setText("ev3_log");
                    MainActivity.tv_red.setText("0");
                    MainActivity.tv_blue.setText("0");
                    MainActivity.tv_total.setText("0");
                    MainActivity.tv_accu.setText("0%");
                    MainActivity.tv_motor.setText("0");



                    //Button 을 클릭하면 아래 입력한 소스가 실행됩니다.
                    //버튼이 클릭되었습니다, 라는 Toast Message 가 화면에 표시 됩니다.
                    Toast.makeText(getApplicationContext(), "물품 분류를 시작 합니다.", Toast.LENGTH_SHORT).show();
                    MessageSend("start" + "\n");

                    isStart = true;
                    btn3.setText("물품 분류 중지");
                    iv_start.setImageDrawable(getResources().getDrawable(R.mipmap.checkmark));
                }
                else{
                    //Button 을 클릭하면 아래 입력한 소스가 실행됩니다.
                    //버튼이 클릭되었습니다, 라는 Toast Message 가 화면에 표시 됩니다.
                    Toast.makeText(getApplicationContext(), "물품 분류를 종료 합니다.", Toast.LENGTH_SHORT).show();
                    MessageSend("exit2" + "\n");

                    isStart = false;
                    btn3.setText("물품 분류 시작");

                    MainActivity.isConnect = false;


                    iv_start.setImageDrawable(getResources().getDrawable(R.mipmap.deletemark));
                }

            }
        });

        //Declare the timer
        Timer t = new Timer();

        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // TextView tv = (TextView) findViewById(R.id.tv_timer);
                        tv_timer.setText(String.valueOf(seconds));
                        if(isStart) {
                            seconds += 1;
                        }
                    }
                });
            }
        }, 0, 1000);

    }

    //메세지 전송 함수
    //메세지를 Buff 형태로 전송 합니다. (EV3와 APP 메시지 송수신은 홀수의 문자 형태로 전송하여야 합니다.)
    public void MessageSend(String message) {
        try {
            byte[] msgBuffer = message.getBytes();
            mOutStream.write(msgBuffer); //출발 문자 전송
            mOutStream.flush();
            Toast.makeText(getApplicationContext(), "메시지 전송 성공", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "명령 송신 실패", Toast.LENGTH_SHORT).show();
        }
    }
}

class BluetoothSocketRead extends Thread {
    public static InputStream mInputStream; //InputStream 클레스 선언
    static BufferedReader br; //Buffered
    static String message; // String Message 클레스 선언
    public static BluetoothSocket mSocket; //소켓

    public BluetoothSocketRead(BluetoothSocket Socket) {
        mSocket = Socket;
    }

    //소켓 통신으로 날라온 문자를 Thread를 통하여 읽는다.

    public void run() {
        while (true) {
            try {
                mInputStream = mSocket.getInputStream();
                br = new BufferedReader(new InputStreamReader(mInputStream, "euc-kr"), 1);
                message = br.readLine();
                if (message == null) {
                    System.out.println("disConnect");
                    MainActivity.Readmessage = message;
                    execution("연결이 종료되었습니다.");
                    MainActivity.isConnect = false;

                    break;
                } else {
                    MainActivity.Readmessage = message;
                    execution(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static public void execution(final String message) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        switch (message) {
                            case "red": {
                                MainActivity.red++;
                                MainActivity.motor++;
                                MainActivity.motor_total++;
                                MainActivity.tv_msg.setText("분류진행중, 양품입니다.");
                                MainActivity.tv_red.setText("" + MainActivity.red);
                                MainActivity.tv_total.setText("" + (MainActivity.red+MainActivity.blue));
                                MainActivity.tv_motor.setText("" + MainActivity.motor);
                                MainActivity.tv_motor_total.setText("" + MainActivity.motor_total);
                                if(MainActivity.red+MainActivity.blue!=0) {
                                    MainActivity.tv_accu.setText("" + 100 * MainActivity.red / (MainActivity.red + MainActivity.blue) + "%");
                                }else{
                                    MainActivity.tv_accu.setText("0%");
                                }

                                break;
                            }

                            case "blu": {
                                MainActivity.blue++;
                                MainActivity.motor++;
                                MainActivity.motor_total++;
                                MainActivity.tv_msg.setText("분류진행중, 불량입니다.");
                                MainActivity.tv_blue.setText("" + MainActivity.blue);
                                MainActivity.tv_total.setText("" + (MainActivity.red+MainActivity.blue));
                                MainActivity.tv_motor.setText("" + MainActivity.motor);
                                MainActivity.tv_motor_total.setText("" + MainActivity.motor_total);

                                if(MainActivity.red+MainActivity.blue!=0) {
                                    MainActivity.tv_accu.setText("" + 100 * MainActivity.red / (MainActivity.red + MainActivity.blue) + "%");
                                }else{
                                    MainActivity.tv_accu.setText("0%");
                                }
                                break;
                            }

                            case "": {
                                MainActivity.isConnect = false;
                                MainActivity.isStart = false;

                                break;
                            }

                            case "ok": {
                                MainActivity.tv_msg.setText("공장이 분류작업을 진행중입니다.");
                                break;
                            }
                            case "hi1": {
                                MainActivity.tv_msg.setText("공장에서 페어링 확인 중입니다.");
                                break;
                            }
                            default: {
                                MainActivity.tv_msg.setText(message);
                                break;
                            }
                        }
                    }
                });
            }
        }).start();
    }
}

