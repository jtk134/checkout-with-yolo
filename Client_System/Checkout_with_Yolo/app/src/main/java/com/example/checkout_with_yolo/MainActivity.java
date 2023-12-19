package com.example.checkout_with_yolo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private String totalAmount = "0";
    private LinearLayout containerLayout;

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

        containerLayout = findViewById(R.id.containerLayout);

        Button paymentButton = findViewById(R.id.button);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업 메시지를 표시
                Toast.makeText(MainActivity.this, "결제 화면이 나타납니다", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                intent.putExtra("key", totalAmount);
                startActivity(intent);
            }
        });

        Button closeButton = findViewById(R.id.button2);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 앱 종료
                finishAffinity();
                System.runFinalization();
                System.exit(0);
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
                    Elements paragraph = element.select("p");

                    String[] items = paragraph.text().split(", ");

                    for (String item : items) {
                        if (item.endsWith(",")) {
                            item = item.substring(0, item.length() - 1);
                        }
                        result.append(item).append("\n");
                    }
                }

                // 이미지 URL 가져오기
                Elements imgElements = doc.select("img"); // 모든 이미지 태그 선택
                String imgUrl = null;

                for (Element imgElement : imgElements) {
                    imgUrl = imgElement.absUrl("src"); // 이미지 태그의 src 속성 가져오기
                }

                return result + "\n" + imgUrl + "\n\n";
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String data) {
            // 크롤링된 데이터를 TextView에 표시
            if (data != null) {
                String[] parts = data.split("\n\n"); // "\n\n"을 구분자로 사용하여 문자열 분리
                String textData = parts[0] + "\n"; // 첫 번째 부분은 텍스트 데이터
                String imageUrl = parts[1]; // 두 번째 부분은 이미지 URL

                // 정규 표현식을 사용하여 "숫자 won" 패턴을 찾고 추출
                Pattern pattern = Pattern.compile("(\\d+) won");
                Matcher matcher = pattern.matcher(textData);
                int sumOfAmounts = 0;

                while (matcher.find()) {
                    int amount = Integer.parseInt(matcher.group(1)); // 그룹 1에 해당하는 숫자를 추출하고 정수로 변환
                    sumOfAmounts += amount;
                }

                totalAmount = String.valueOf(sumOfAmounts); // 총 금액

                textView.setText(textData);
                textView2.setText("Total: " + totalAmount + " won");

                // 이미지를 Glide를 사용하여 ImageView에 표시
                Glide.with(MainActivity.this)
                        .load(imageUrl)
                        .into(imageView);

                if (containerLayout.getChildCount() == 1) {
                    Button newButton  = new Button(MainActivity.this);
                    newButton .setText("물품별 구매 수량 변경");
                    containerLayout.addView(newButton);

                    newButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("수량 변경");
                            builder.setMessage("물품별 구매할 수량을 입력하세요:");

                            // 다이얼로그에 EditText 추가
                            final EditText input = new EditText(MainActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);

                            builder.setView(input);

                            // 확인 버튼 클릭 시 이벤트 처리
                            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        // 입력된 수를 정수로 변환하여 현재 수에 대입
                                        int currentNumber = Integer.parseInt(input.getText().toString());
                                        // TODO: 현재 수에 대한 처리 추가
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            // 취소 버튼 클릭 시 이벤트 처리
                            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel(); // 다이얼로그를 닫음
                                }
                            });

                            builder.show(); // 다이얼로그를 표시
                        }
                    });
                }

            } else {
                textView.setText("데이터를 가져오는데 실패했습니다.");
            }
        }
    }
}
