package com.example.checkout_with_yolo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PaymentActivity extends AppCompatActivity {
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();
        String totalAmount = intent.getStringExtra("key");

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        Button cancelButton = findViewById(R.id.button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 액티비티를 종료
                finish();
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

        new ImageLoadTask().execute(totalAmount);
    }

    private class ImageLoadTask extends AsyncTask<String, Void, String> {
        private String imageUrl;
        @Override
        protected String doInBackground(String... params) {
            imageUrl = "https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=" + params[0];
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText("Total: " + result + " won");

            // 이미지를 Glide 라이브러리를 사용하여 로드 및 표시
            Glide.with(PaymentActivity.this)
                    .load(imageUrl)
                    .into(imageView);
        }
    }
}