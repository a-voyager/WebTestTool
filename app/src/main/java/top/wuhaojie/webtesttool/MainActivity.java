package top.wuhaojie.webtesttool;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CONN_SUCCEED = 1002;
    private EditText et_url_address;
    private TextView tv_result;
    private Button btn_conn;
    private static final int ERROR_CODE = 1001;
    public final int CONN_FAILED = 1000;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONN_FAILED:
                    Snackbar.make((View) findViewById(R.id.ctl_main), "访问出错!", Snackbar.LENGTH_SHORT).show();
                    break;
                case ERROR_CODE:
                    Snackbar.make((View) findViewById(R.id.ctl_main), "访问失败! 响应码: " + msg.arg1, Snackbar.LENGTH_SHORT).show();
                    break;
                case CONN_SUCCEED:
                    tv_result.setText(result);
                    result = "";
                    break;
            }
            pd.dismiss();
        }
    };
    private CheckBox cb_use_post;
    private String methord = "GET";
    private String result = "";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initVariables();
    }

    private void initVariables() {
        pd = new ProgressDialog(this);
        pd.setMessage("请稍候...");
    }


    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Web测试工具");
        setSupportActionBar(toolbar);
        et_url_address = (EditText) findViewById(R.id.et_url_address);
        tv_result = (TextView) findViewById(R.id.tv_result);
        btn_conn = (Button) findViewById(R.id.btn_conn);
        btn_conn.setOnClickListener(this);
        cb_use_post = (CheckBox) findViewById(R.id.cb_use_post);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_conn:
                final String addr = et_url_address.getText().toString().trim();
                if (addr.isEmpty() || !addr.startsWith("http")) {
                    Snackbar.make((View) findViewById(R.id.ctl_main), "URL地址不合法", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                tv_result.setText("等待更新数据");
                if (cb_use_post.isChecked()) methord = "POST";
                pd.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(addr);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod(methord);
                            urlConnection.connect();
                            int responseCode = urlConnection.getResponseCode();
                            if (responseCode == 200) {
                                InputStream inputStream = urlConnection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    result += line + "\n";
                                }
                                handler.sendEmptyMessage(CONN_SUCCEED);
                            } else {
                                Message message = new Message();
                                message.what = ERROR_CODE;
                                message.arg1 = responseCode;
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            handler.sendEmptyMessage(CONN_FAILED);
                        }

                    }
                }).start();
                break;
        }
    }


}
