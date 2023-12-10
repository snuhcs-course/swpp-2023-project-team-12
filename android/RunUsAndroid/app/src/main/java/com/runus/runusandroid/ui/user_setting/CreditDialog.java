package com.runus.runusandroid.ui.user_setting;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.runus.runusandroid.R;

public class CreditDialog extends Dialog implements View.OnClickListener {

    public Context context;
    public ImageButton closeButton;

    public TextView creditText;

    public CreditDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_credit);
        creditText = findViewById(R.id.creditText);
        closeButton = findViewById(R.id.buttonCloseCreditDialog);
        closeButton.setOnClickListener(this);

        String htmlText =
                "<p>다섯번 완주\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/high-five\" title=\"high five icons\">High five icons created by Freepik - Flaticon</a></p>\n" +

                        "<p>첫번째 금메달 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/medal\" title=\"medal icons\">Medal icons created by Freepik - Flaticon</a></p>\n" +
                        "<p>미션 10번 성공 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/mission\" title=\"mission icons\">Mission icons created by ultimatearm - Flaticon</a></p>\n" +
                        "<p>꾸준함의 상징 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/clap\" title=\"clap icons\">Clap icons created by bukeicon - Flaticon</a></p>\n" +
                        "<p>열번째 금메달 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/medal\" title=\"medal icons\">Medal icons created by Handicon - Flaticon</a></p>\n" +
                        "<p>스피드 러너 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/trail-running\" title=\"trail running icons\">Trail running icons created by Flat Icons - Flaticon</a></p>\n" +
                        "<p>하프 마라토너 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/marathon\" title=\"marathon icons\">Marathon icons created by Flat Icons - Flaticon</a></p>\n" +
                        "<p>마라토너 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/marathon\" title=\"marathon icons\">Marathon icons created by Flat Icons - Flaticon</a></p>\n" +
                        "<p>마라톤 위너 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/cup\" title=\"cup icons\">Cup icons created by Freepik - Flaticon</a></p>\n" +
                        "<p>자물쇠 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/lock\" title=\"lock icons\">Lock icons created by Freepik - Flaticon</a></p>\n" +
                        "<p>시상대 이미지\n" +
                        "<a href=\"https://www.flaticon.com/free-icons/podium\" title=\"podium icons\">Podium icons created by Freepik - Flaticon</a></p>";

        // HTML 문자열을 TextView에 설정
        creditText.setText(HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        creditText.setMovementMethod(LinkMovementMethod.getInstance());
        creditText.setLinkTextColor(Color.BLACK);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonCloseCreditDialog) {
            dismiss();
        }
    }

}
