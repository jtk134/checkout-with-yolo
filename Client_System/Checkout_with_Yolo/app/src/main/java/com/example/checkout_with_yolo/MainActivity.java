package com.example.checkout_with_yolo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    TextView textView2;
    ImageView imageView;

    // 주기적으로 크롤링을 수행하기 위한 핸들러
    private final Handler handler = new Handler();

    // 크롤링 주기 (예: 5초마다 크롤링)
    private static final long CRON_INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView); // activity_main.xml의 TextView와 연결
        textView2 = findViewById(R.id.textView2);
        imageView = findViewById(R.id.imageView);

        Button paymentButton = findViewById(R.id.button);

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업 메시지를 표시합니다.
                Toast.makeText(MainActivity.this, "결제 화면이 나타납니다", Toast.LENGTH_LONG).show();
            }
        });

        // 버튼을 찾아서 클릭 이벤트 핸들러를 설정
        Button closeButton = findViewById(R.id.button2);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 앱 종료
                finish();
            }
        });

        // 주기적으로 크롤링 작업을 수행
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                jsoupAsyncTask.execute();

                // 다음 크롤링을 예약
                handler.postDelayed(this, CRON_INTERVAL);
            }
        }, 0);
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String url = "http://jtk.pythonanywhere.com"; // 크롤링할 웹 사이트 URL
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select(".post"); // "class=post"를 가진 요소 선택
                StringBuilder result = new StringBuilder();

                for (Element element : elements) {
                    result.append(element.text()).append("\n"); // 선택한 요소의 텍스트 가져오기
                }

                // 이미지 URL 가져오기
                Elements imgElements = doc.select("img"); // 모든 이미지 태그 선택
                String imgUrl = null;

                for (Element imgElement : imgElements) {
                    imgUrl = imgElement.absUrl("src"); // 이미지 태그의 src 속성 가져오기
                }

                // 정규 표현식을 사용하여 "숫자 won" 패턴을 찾고 추출
                Pattern pattern = Pattern.compile("(\\d+) won");
                Matcher matcher = pattern.matcher(result.toString());
                int totalAmount = 0;

                while (matcher.find()) {
                    int amount = Integer.parseInt(matcher.group(1)); // 그룹 1에 해당하는 숫자를 추출하고 정수로 변환
                    totalAmount += amount;
                }

                return result.toString() + "\n" + imgUrl + "\n\n" + totalAmount;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 크롤링된 데이터를 TextView에 표시
            if (result != null) {
                String[] parts = result.split("\n\n"); // "\n\n"을 구분자로 사용하여 문자열 분리
                String textData = parts[0] + "\n"; // 첫 번째 부분은 텍스트 데이터
                String imageUrl = parts[1]; // 두 번째 부분은 이미지 URL
                String totalAmount = parts[2]; // 세 번째 부분은 total 금액
                textView.setText(textData);
                textView2.setText("Total: " + totalAmount + " won");

                // 이미지를 Glide를 사용하여 ImageView에 표시
                Glide.with(MainActivity.this)
                        .load(imageUrl)
                        .into(imageView);
            } else {
                textView.setText("데이터를 가져오는데 실패했습니다.");
            }
        }
    }
}
