package com.speech.wing.speechapp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.nfc.Tag;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.SpeechError;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import se.talkamatic.frontend.IBackendStatusListener;
import se.talkamatic.frontend.IEventHandler;
import se.talkamatic.frontend.IEventListener;
import se.talkamatic.frontend.Language;
import se.talkamatic.frontend.TdmConnector;
import se.talkamatic.frontend.asr.AsrListenerAdapter;
import se.talkamatic.frontend.asr.AsrRecognitionHypothesis;
import se.talkamatic.frontend.asr.IAsrListener;
import se.talkamatic.frontend.integration.iflytek.IFlytekAsr;


public class MainActivity extends AppCompatActivity {

    private Button BtnSpeech;
    private Button BtnConn;
    private Button BtnDisconn;
    private ListView mListView;
    private ArrayAdapter mAdapter;

    private TdmConnector tdmConn;
    private Handler MainHandler;
    private IAsrListener RecognitionListener;

    private ArrayList<String> SpeechList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BtnSpeech=(Button)findViewById(R.id.BtnSpeech);
        BtnConn=(Button)findViewById(R.id.BtnConn);
        BtnDisconn=(Button)findViewById(R.id.BtnDisconn);
        mListView=(ListView)findViewById(R.id.mlistview);

        final Context AppContext=getApplicationContext();
        tdmConn=TdmConnector.createTdmConnector(AppContext);
        tdmConn.setLanguage(Language.CHINESE);
        // This if-statement is here to show how to use the
        // iFlyTek ASR integration in tdmIFlyTekIntegration-development-debug.aar
        boolean USE_IFLYTEK = false;
        if(USE_IFLYTEK) {
            tdmConn.setExternalAsrBrand(IFlytekAsr.class.getName());
        }
        MainHandler=new Handler(AppContext.getMainLooper());

        SpeechList=new ArrayList<String>();

        BackendStatusListener();
        SpeakingStatusListener();
        SpeechEventListener();

        BtnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable handlerClickRunnable=new Runnable() {
                    @Override
                    public void run() {
                        tdmConn.connect("ws://54.194.134.224:9090/maharani");
                    }
                };
                MainHandler.post(handlerClickRunnable);
            }
        });

        BtnDisconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable handlerClickRunnable=new Runnable() {
                    @Override
                    public void run() {
                        tdmConn.disconnect();
                    }
                };
                MainHandler.post(handlerClickRunnable);
            }
        });

        BtnSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable handlerClickRunnable=new Runnable() {
                    @Override
                    public void run() {
                        tdmConn.notifyPTTPushed();
                    }
                };
                MainHandler.post(handlerClickRunnable);
            }
        });
    }

    private  void BackendStatusListener(){
        IBackendStatusListener AppBackendStatusListerner=new IBackendStatusListener() {
            @Override
            public void onOpen() {
                Log.e("backendStatusListener","onOpen");
                DisplayBackendStatus("Opened");
            }

            @Override
            public void onClose(int i, String s) {
                Log.e("BackendStatusListener","onClose("+ i+","+s+")");
                DisplayBackendStatus("Closed with code:"+i+",reason:"+s);
            }

            @Override
            public void onError(String s) {
                Log.e("BackendStatusListener","onError("+s+")");
                DisplayBackendStatus("Error:"+s);
            }
        };
        tdmConn.registerBackendStatusListener(AppBackendStatusListerner);
    };

    private void DisplayBackendStatus(String status){
        Toast.makeText(this,status,Toast.LENGTH_SHORT).show();
    };

    private void SpeakingStatusListener(){
        RecognitionListener =new AsrListenerAdapter(){
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                BtnSpeech.setText("ready");
            }

            @Override
            public void onBeginningOfSpeech() {
                BtnSpeech.setText("begin speaking");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onEndOfSpeech() {
               BtnSpeech.setText("Finish Speaking");
            }

            @Override
            public void onSpeechTimeout() {
                BtnSpeech.setText("asr time out");
            }

            @Override
            public void onError(String s) {
                DisplayBackendStatus(s);
            }

            @Override
            public void onResults(List<AsrRecognitionHypothesis> list) {

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }
        };
        tdmConn.registerRecognitionListener(RecognitionListener);
    };

    private void SpeechEventListener(){
        IEventListener eventListener=new IEventListener() {
            @Override
            public void onShowPopup(String s, List<Map<String, String>> list) {
                Log.d("eventListerner","onShowPopup(title:"+s+",options:"+list+")");
            }

            @Override
            public void onAction(String s, String s1, Map<String, String> map) {
                Log.d("eventListener", "onPerformAction(DDD: " + s + ", name: " + s1 + ", args: " + map + ")");
                SpeechList.add(s + ": " + map.toString());
            }

            @Override
            public void onWhQuery(String s, String s1, Map<String, String> map) {
                Log.d("eventListener", "onPerformWHQuery(DDD: " + s + ", name: " + s1 + ", args: " + map + ")");
                SpeechList.add(s + ": " + map.toString());
            }

            @Override
            public void onValidity(String s, String s1, Map<String, String> map) {
                Log.d("eventListener", "onPerformValidity(DDD: " + s + ", name: " + s1 + ", args: " + map + ")");
                SpeechList.add(s + ": " + map.toString());
            }

            @Override
            public void onEntityRecognizer(String s, String s1, Map<String, String> map) {
                Log.d("eventListener", "onPerformEntityRecognition(DDD: " + s + ", name: " + s1 + ", args: " + map + ")");
                SpeechList.add(s + ": " + map.toString());
            }

            @Override
            public void onSystemUtteranceToSpeak(String s) {
                Log.d("eventListener", "onSystemUtteranceToSpeak(" + s + ")");
                SpeechList.add(s);
            }

            @Override
            public void onSelectedRecognition(String s) {
                Log.d("eventListener", "onSelectedRecognition(" + s + ")");
                SpeechList.add(s);
            }

            @Override
            public void onActiveDddChanged(String s, String s1) {
                Log.d("eventListener", "onActiveDddChanged(" + s + ", " + s1 + ")");
                SpeechList.add(s);
            }
        };
        tdmConn.registerEventListener(eventListener);
        mAdapter=new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,SpeechList);
        mListView.setAdapter(mAdapter);
    };
}
