package com.example.necklace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IGetMessageCallBack{
    private TextView m_static_pinao_message,m_static_pinao_mouth,m_static_pinao_eye,m_static_pinao_nod;
    private MyServiceConnection serviceConnection;
    private Button m_btn_test_tts;
    private MQTTService mqttService;
    TextToSpeech textToSpeech;
    private Timer timer;
    String receive_data="";
    String str_mouth="";
    String str_eye="";
    String str_nod="";
    String str_message="";
    int flag_cnt=0;
    int cnt=0;
    public String getstringfromdata(String source_str,String str_start,String str_end)//字符串提取函数
    {
        String getstring_temp="";
        if (!"".equals(source_str))
        {
            getstring_temp=source_str.substring(source_str.indexOf(str_start)+1,source_str.indexOf(str_end));
            return getstring_temp;
        }
        else return "";

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_static_pinao_message=findViewById(R.id.m_static_pinao_message);
        m_static_pinao_mouth=findViewById(R.id.m_static_pinao_mouth);
        m_static_pinao_eye=findViewById(R.id.m_static_pinao_eye);
        m_static_pinao_nod=findViewById(R.id.m_static_pinao_nod);
        m_btn_test_tts=findViewById(R.id.m_btn_test_tts);



        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack(MainActivity.this);
        //用Intent方式创建并启用Service
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        //初始哈TTS对象
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    //设置语言
                    textToSpeech.setLanguage(Locale.CHINA);
                }
            }
        });
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(flag_cnt==1)
                {
                    if(cnt==10)
                    {
                        flag_cnt=0;
                        m_static_pinao_message.setText("");
                    }
                    cnt+=1;
                }

            }
        },0,1000); //延迟500毫秒后，执行一次task

        //按下这个按钮就发布一条消息
        m_btn_test_tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toSpeak = "你好";
//                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
//                m_static_pinao_message.setText("疲劳驾驶");
            }
        });
    }
    //继承了这个接口就直接用这个借口的方法就好啦
    @Override
    public void setMessage(String message) {
        receive_data=message;
        if(receive_data.equals("clear"))
        {
            flag_cnt=1;
            cnt=0;
//            m_static_pinao_message.setText("");
        }
        else
        {

            str_message=getstringfromdata(receive_data,"a","b");
            str_mouth=getstringfromdata(receive_data,"b","c");
            str_eye=getstringfromdata(receive_data,"c","d");
            str_nod=getstringfromdata(receive_data,"d","e");
//            System.out.println(str_message+str_mouth);
            if (str_message.equals("1"))
            {
                m_static_pinao_message.setText("疲脑驾驶");
                String toSpeak = "疲脑驾驶";
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
            m_static_pinao_mouth.setText(str_mouth);
            m_static_pinao_eye.setText(str_eye);
            m_static_pinao_nod.setText(str_nod);
        }

//        m_static_pinao_no.setText(str_nod);
////        m_static_pinao_eye.setText(str_eye);
////        m_static_pinao_mouth.setText(str_mouth);d.setText(str_nod);
//        m_static_pinao_eye.setText(str_eye);
//        m_static_pinao_mouth.setText(str_mouth);

//        if(str_message.equals("1"))
//        {
//            m_static_pinao_message.setText("222");
//        }
        mqttService = serviceConnection.getMqttService();
        mqttService.toCreateNotification(message);
    }


    //这里最主要还是销毁掉服务，因为Activity销毁掉以后
    //Service并不会自动回收，而是会转入后台运行，这样会
    //影响到下一次的运行，所以这里必须做销毁处理
    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}