package com.example.smartparking;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BLEActivity {
    private static EditText edit_cell_num;
    private EditText edit_set_num;

    private Button btn_query;
    private Button btn_save;
    private Button btn_learn;
    private Button btn_reset;

    private Button btn_model_a;
    private Button btn_model_b;

    private Button btn_train;
    private Button btn_test;

    private static TextView tv_message;
    private static LinearLayout linearlayout_message;
    private static Activity mainActivityContext;
    private static ScrollView scrollView;
    private static TextView tv_accuracy;

    private boolean toggle = false; // false: off, true: on
    private String cellNum = null;
    private static int urlSendCount = 0; // 쿼리전송시 전송한 URL 갯수
    private static int correctCount = 0; // 맞은 횟수 카운트
    private static String accuracyPercent = null; // 정확도 (확률, %)
    private static int responseCell = 0; // 응답으로 온 셀 번호

    // 버튼 상수화
    private static final int BTN_RESET = 1;
    private static final int BTN_QUERY = 2;
    private static final int BTN_SAVE = 3;
    private static final int BTN_LEARN = 4;
    private static final int BTN_MODEL_A = 5;
    private static final int BTN_MODEL_B = 6;
    private static final int BTN_TRAIN = 7;
    private static final int BTN_TEST = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initScreen();
    }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private void initScreen() {
        edit_cell_num = findViewById(R.id.edit_cell_num);
        edit_set_num = findViewById(R.id.edit_set_num);

        btn_reset = findViewById(R.id.reset);
        btn_query = findViewById(R.id.query);
        btn_save = findViewById(R.id.save);
        btn_learn = findViewById(R.id.learn);

        btn_model_a = findViewById(R.id.model_a);
        btn_model_b = findViewById(R.id.model_b);

        btn_train = findViewById(R.id.train);
        btn_test = findViewById(R.id.test);

        linearlayout_message = findViewById(R.id.message_val);
        scrollView = findViewById(R.id.scrollView);
        tv_accuracy = findViewById(R.id.accuracy_val);

        // clickListener
        btn_reset.setOnClickListener(clickListener);
        btn_query.setOnClickListener(clickListener);
        btn_save.setOnClickListener(clickListener);
        btn_learn.setOnClickListener(clickListener);
        btn_model_a.setOnClickListener(clickListener);
        btn_model_b.setOnClickListener(clickListener);
        btn_train.setOnClickListener(clickListener);
        btn_test.setOnClickListener(clickListener);

        defaultButtonColor();

        mainActivityContext = this;
    }

    // 버튼 타입에 따라 정수형으로 리턴
    private int returnButtonType(View v) {
        if (v == btn_reset) return BTN_RESET;
        else if (v == btn_query) return BTN_QUERY;
        else if (v == btn_save) return BTN_SAVE;
        else if (v == btn_learn) return BTN_LEARN;
        else if (v == btn_model_a) return BTN_MODEL_A;
        else if (v == btn_train) return BTN_TRAIN;
        else if (v == btn_test) return BTN_TEST;
        else return BTN_MODEL_B;
    }

    // button color default
    private void defaultButtonColor() {
        btn_reset.setBackgroundColor(Color.GRAY);
        btn_query.setBackgroundColor(Color.GRAY);
        btn_save.setBackgroundColor(Color.GRAY);
        btn_learn.setBackgroundColor(Color.GRAY);
        btn_model_a.setBackgroundColor(Color.GRAY);
        btn_model_b.setBackgroundColor(Color.GRAY);
        btn_train.setBackgroundColor(Color.GRAY);
        btn_test.setBackgroundColor(Color.GRAY);
    }

    // button color setting
    private void setButtonColor(View view) {
        defaultButtonColor();
        
        switch(getModelType()) {
            case BTN_MODEL_A:
                btn_model_a.setBackgroundColor(Color.RED);
                break;
            case BTN_MODEL_B:
                btn_model_b.setBackgroundColor(Color.RED);
                break;
        }
        switch(getMode()) {
            case BTN_TRAIN:
                btn_train.setBackgroundColor(Color.RED);
                break;
            case BTN_TEST:
                btn_test.setBackgroundColor(Color.RED);
                break;
        }
        switch(returnButtonType(view)) {
            case BTN_RESET:
                btn_reset.setBackgroundColor(Color.RED);
                break;
            case BTN_QUERY:
                btn_query.setBackgroundColor(Color.RED);
                break;
            case BTN_SAVE:
                btn_save.setBackgroundColor(Color.RED);
                break;
            case BTN_LEARN:
                btn_learn.setBackgroundColor(Color.RED);
                break;
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (returnButtonType(v)) {
                case BTN_RESET:
                    setButtonColor(v);
                    btn_reset.setBackgroundColor(Color.RED);
                    setButtonType(BTN_RESET);
                    urlSendCount = 0; // url 전송갯수 초기화
                    correctCount = 0; // 정확도 갯수 초기화
                    tv_accuracy.setText("0%");

                    if (linearlayout_message.getChildCount() > 0)
                        linearlayout_message.removeAllViews();
                    break;


                case BTN_QUERY:
                    setButtonColor(v);

                    if (toggle) {
                        Toast.makeText(MainActivity.this, "Query: Off", Toast.LENGTH_SHORT).show();
                        toggle = false;
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Query: On", Toast.LENGTH_SHORT).show();
                        toggle = true;
                        setButtonType(BTN_QUERY);
                        Log.i("BTN_QUERY", String.valueOf(getButtonType()));
                    }
                    scanLeDevice(toggle);
                    break;

                case BTN_SAVE:
                    setButtonColor(v);

                    if (toggle) {
                        Toast.makeText(MainActivity.this, "Save: Off", Toast.LENGTH_SHORT).show();
                        toggle = false;
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Save: On", Toast.LENGTH_SHORT).show();
                        toggle = true;
                        cellNum = edit_cell_num.getText().toString();
                        int setNum = Integer.parseInt(edit_set_num.getText().toString());

                        setButtonType(BTN_SAVE);
                        Log.i("BTN_SAVE", String.valueOf(getButtonType()));
                        setCellAndSetNumber(cellNum, setNum);
                    }

                    if(Integer.parseInt(cellNum) == 0) {
                        Log.i("cellNum", "cellNum: 0");
                        Toast.makeText(MainActivity.this, "0을 제외한 숫자를 입력해주세요!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // cell id가 0이 아닐때만 스캔 진행
                        scanLeDevice(toggle);
                    }
                    break;

                case BTN_LEARN:
                    setButtonColor(v);

                    setButtonType(BTN_LEARN);
                    Log.i("BTN_LEARN", String.valueOf(getButtonType()));
                    Toast.makeText(MainActivity.this, "Start Learning", Toast.LENGTH_SHORT).show();

                    int modelType = getModelType();

                    int setNumForLearning = Integer.parseInt(edit_set_num.getText().toString());
                    String URL = Utility.URL + "learn?modelType=" + modelType + "&setNumForLearning=" + setNumForLearning;
                    new NetworkTask(URL, null).execute();
                    break;

                case BTN_MODEL_A:
                    setModelType(BTN_MODEL_A);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "You're in the car!", Toast.LENGTH_SHORT).show();
                    break;

                case BTN_MODEL_B:
                    setModelType(BTN_MODEL_B);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "You're out of the car!", Toast.LENGTH_SHORT).show();
                    break;

                case BTN_TRAIN:
                    setMode(BTN_TRAIN);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "Train mode is selected!", Toast.LENGTH_SHORT).show();
                    break;

                case BTN_TEST:
                    setMode(BTN_TEST);
                    setButtonColor(v);
                    Toast.makeText(MainActivity.this, "Test mode is selected!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 자동스크롤 기능
    public static void AutoScrollBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // 백그라운드에서 스레드로 비콘신호 수신
    public static class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);
            return parser(result);
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);

            // save 버튼을 눌렀을때만 카운트와 'Success: save' 문구 출력
            if (getSetNumForLearning() != 0 && getButtonType() == BTN_SAVE) {
                int num = getSetNumForLearning() - getSetNum();
                Log.i("setNumForLearning", "setNumForLearning: " + getSetNumForLearning());
                Log.i("setNum", "setNum: " + getSetNum());

                tv_message = new TextView(mainActivityContext);
                tv_message.setText(String.format("%05d:%s\n", num, aString));
                linearlayout_message.addView(tv_message);
                AutoScrollBottom();
            }
            else {
                Log.i("setNumForLearning", "setNumForLearning: " + getSetNumForLearning());
                if (getButtonType() == BTN_QUERY) {
                    // query 버튼을 누를시 정확도 표기 목적

                    urlSendCount++;

                    responseCell = Integer.parseInt(aString);
                    int cellNum = Integer.parseInt(edit_cell_num.getText().toString());

                    if (responseCell == cellNum)
                        correctCount++;

                    double value = (double) correctCount / (double) urlSendCount;

                    accuracyPercent = String.valueOf(Double.parseDouble(String.format("%.2f", value)) * 100);

                    Log.i("LOG_COMM_SERVER", "accuracy: " + accuracyPercent + "%");
                    Log.i("LOG_COMM_SERVER", "urlSendCount: " + urlSendCount);
                    Log.i("LOG_COMM_SERVER", "correctCount: " + correctCount);
                    Log.i("LOG_COMM_SERVER", "responseCell: " + responseCell);
                    Log.i("LOG_COMM_SERVER", "cellNum: " + cellNum);

                    // 정확도 표기
                    tv_accuracy.setText(accuracyPercent + "%");
                }

                tv_message = new TextView(mainActivityContext);
                tv_message.setText(aString + "\n");
                linearlayout_message.addView(tv_message);
                AutoScrollBottom();
            }
        }

        private String parser(String aString) {
            Log.d("TAG", "Web: " + aString);
            return aString;
        }
    }

}
