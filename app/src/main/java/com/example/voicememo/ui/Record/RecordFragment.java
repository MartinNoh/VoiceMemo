package com.example.voicememo.ui.Record;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.voicememo.R;
import com.example.voicememo.db.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordFragment extends Fragment {

    private ImageButton sttBtn;
    private TextView sttResult;
    final int PERMISSION = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_record, container, false);


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 5);
            toast("앱의 오디오 권한을 허용해주세요.");
        }

        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        sttBtn = (ImageButton) root.findViewById(R.id.btn_record);
        sttResult = (TextView) root.findViewById(R.id.tv_sttResult);

        sttBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    // 마이크 버튼을 누르고 있을 때
                    case MotionEvent.ACTION_DOWN:
                        sttResult.setText("잠시만요.");
                        inputVoice(sttResult);
                        sttBtn.setBackgroundResource(R.drawable.record_pressed);
                        break;

                    // 마이크 버튼을 땠을 때
                    case MotionEvent.ACTION_UP:
                        sttResult.setText("");
                        sttBtn.setBackgroundResource(R.drawable.record_default);
                        break;
                }
                return false;
            }
        });

        return root;
    }

    private void inputVoice(final TextView sttResult){
        try {
            //사용자에게 음성을 요구하고 음성 인식기를 통해 전송하는 활동을 시작
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //음성 인식을위한 음성 인식기의 의도에 사용되는 여분의 키
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getActivity().getPackageName());
            //음성을 번역할 언어를 설정
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(getActivity());
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    // 사용자가 말하기 시작할 준비가되면 호출
                    sttResult.setText("듣고 있습니다.");
                }

                @Override
                public void onBeginningOfSpeech() {
                    // 사용자가 말하기 시작했을 때 호출
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // 입력받는 소리의 크기
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // 사용자가 말을 시작하고 인식이 된 단어를 buffer에 담기
                }

                @Override
                public void onEndOfSpeech() {
                    // 사용자가 말하기를 중지하면 호출
                }

                @Override
                public void onError(int error) {
                    // 네트워크 또는 인식 오류가 발생했을 때 호출
                    String message;

                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "ERROR_AUDIO";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "ERROR_CLIENT";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "ERROR_INSUFFICIENT_PERMISSIONS";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "ERROR_NETWORK";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "ERROR_NETWORK_TIMEOUT";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "ERROR_NO_MATCH";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "ERROR_RECOGNIZER_BUSY";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "ERROR_SERVER";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "ERROR_SPEECH_TIMEOUT";
                            break;
                        default:
                            message = "ERROR_UNKNOWN";
                            break;
                    }

                    Toast.makeText(getActivity().getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    alert_show(result.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });

            stt.startListening(intent);

        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void toast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    void alert_show(final String text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("\"" + text + "\"");
        builder.setMessage("캘린더로 기록하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {voiceConfiguration(text);}
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        toast("아니오를 선택했습니다.");
                    }
                });
        builder.show();
    }

    public void voiceConfiguration(String text){

        String date="", content="";
        text = " " + text;
        Date time = new Date();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/M/d");
        String formattedTime = mFormat.format(time);

        if (text.indexOf("년") == 5 & text.contains("년") & text.contains("월") & text.contains("일")){ // 시간을 말하는 단어에서 '일' 앞을 date, 그 뒤를 content로 저장
            date = date.concat(text.substring(text.indexOf("년")-4, text.indexOf("년")) + "/" + text.substring(text.indexOf("월")-2, text.indexOf("월")).trim() + "/" + text.substring(text.indexOf("일")-2, text.indexOf("일")).trim());
            content = content.concat(text.substring(text.indexOf("일")+2));
        }
        else if (text.indexOf("년") == 3 & text.contains("년") & text.contains("월") & text.contains("일")){ // 시간을 말하는 단어에서 '일' 앞을 date, 그 뒤를 content로 저장
            date = date.concat("20" + text.substring(text.indexOf("년")-2, text.indexOf("년")) + "/" + text.substring(text.indexOf("월")-2, text.indexOf("월")).trim() + "/" + text.substring(text.indexOf("일")-2, text.indexOf("일")).trim());
            content = content.concat(text.substring(text.indexOf("일")+2));
        }
        else if(text.contains("월") & text.contains("일")){
            date = date.concat(formattedTime.split("/")[0] + "/" + text.substring(text.indexOf("월")-2, text.indexOf("월")).trim() + "/" + text.substring(text.indexOf("일")-2, text.indexOf("일")).trim());
            content = content.concat(text.substring(text.indexOf("일")+2));
        }
        else if(text.contains("일")){
            date = date.concat(formattedTime.split("/")[0] + "/" + formattedTime.split("/")[1] + "/" + text.substring(text.indexOf("일")-2, text.indexOf("일")).trim());
            content = content.concat(text.substring(text.indexOf("일")+2));
        }
        else{ // 년, 월, 일을 말하지 않은 경우는 오늘을 date로하고, 단어를 content로 저장

            date = formattedTime;
            content = text;
        }
        DBHelper helper = new DBHelper(getActivity());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into tb_memo (date, content) values (?, ?)",
                new String[]{date, content});
        db.close();

    }
}