package com.example.a2mb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONObject;

/// 네이버 아이디로 로그인 샘플앱

/**
 * <br/> OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제,
 * <br/> 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 *
 * @author naver
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private OAuthLoginButton naverLogInButton;
    private static OAuthLogin naverLoginInstance;
    Button btnGetApi, btnLogout;

    static final String CLIENT_ID = "XySODqf91mZd61IqIEcf";
    static final String CLIENT_SECRET = "jA_VKBMhLh";
    static final String CLIENT_NAME = "네이버 아이디로 로그인 테스트";

    TextView tv_mail;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        init_View();
    }



    //초기화
    private void init(){
        context = this;
        naverLoginInstance = OAuthLogin.getInstance();
        naverLoginInstance.init(this,CLIENT_ID,CLIENT_SECRET,CLIENT_NAME);
    }
    //변수 붙이기
    private void init_View(){
        naverLogInButton = (OAuthLoginButton)findViewById(R.id.buttonNaverLogin);

        //로그인 핸들러
        OAuthLoginHandler naverLoginHandler  = new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {//로그인 성공
                    Toast.makeText(context,"로그인 되었습니다",Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                } else {//로그인 실패
                    String errorCode = naverLoginInstance.getLastErrorCode(context).getCode();
                    String errorDesc = naverLoginInstance.getLastErrorDesc(context);
                    Toast.makeText(context, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                }
            }

        };;

        naverLogInButton.setOAuthLoginHandler(naverLoginHandler);
        tv_mail = (TextView)findViewById(R.id.tv_mailaddress);
        btnGetApi = (Button)findViewById(R.id.btngetapi);
        btnGetApi.setOnClickListener(this);
        btnLogout = (Button)findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btngetapi){
            new RequestApiTask().execute();//static이 아니므로 클래스 만들어서 시행.
        }
        if(v.getId() == R.id.btnlogout){
            naverLoginInstance.logout(context);
            tv_mail.setText((String) "");//메일 란 비우기
        }
    }


    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {//작업이 실행되기 전에 먼저 실행.
            tv_mail.setText((String) "");//메일 란 비우기
        }

        @Override
        protected String doInBackground(Void... params) {//네트워크에 연결하는 과정이 있으므로 다른 스레드에서 실행되어야 한다.
            String url = "https://openapi.naver.com/v1/nid/me";
            String at = naverLoginInstance.getAccessToken(context);
            return naverLoginInstance.requestApi(context, at, url);//url, 토큰을 넘겨서 값을 받아온다.json 타입으로 받아진다.
        }

        protected void onPostExecute(String content) {//doInBackground 에서 리턴된 값이 여기로 들어온다.
            try {
                JSONObject jsonObject = new JSONObject(content);
                JSONObject response = jsonObject.getJSONObject("response");
                String email = response.getString("email");
                tv_mail.setText(email);//메일 란 채우기
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}


