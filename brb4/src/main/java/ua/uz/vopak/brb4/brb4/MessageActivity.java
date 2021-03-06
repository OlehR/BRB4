package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import ua.uz.vopak.brb4.lib.enums.ActionType;
import ua.uz.vopak.brb4.lib.enums.MessageType;


public class MessageActivity extends Activity implements View.OnClickListener {
    TextView messageHeaderView, messageView;
    ScrollView messageScrollView;
    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_layout);

        messageHeaderView = findViewById(R.id.messageHeader);
        messageView = findViewById(R.id.messageContent);
        messageScrollView = findViewById(R.id.message_scroll_view);
        btnOk = findViewById(R.id.messageOk);

        btnOk.setOnClickListener(this);

        Intent intent = getIntent();

        String messageHeader = intent.getStringExtra("messageHeader");
        String message = intent.getStringExtra("message");
        MessageType type = (MessageType)intent.getSerializableExtra("type");
        ActionType action = (ActionType)intent.getSerializableExtra("action");

        switch (type){
            case ErrorMessage:
                messageHeaderView.setTextColor(ContextCompat.getColor( this,R.color.messageError));
                messageView.setTextColor(ContextCompat.getColor( this,R.color.messageError));
                messageScrollView.setBackground(ContextCompat.getDrawable(this,R.drawable.message_border_error));
                break;
            case AlertMessage:
                messageHeaderView.setTextColor(ContextCompat.getColor( this,R.color.messageAlert));
                messageView.setTextColor(ContextCompat.getColor( this,R.color.messageAlert));
                messageScrollView.setBackground(ContextCompat.getDrawable(this,R.drawable.message_border_alert));
                break;
            case SuccessMesage:
                messageHeaderView.setTextColor(ContextCompat.getColor( this,R.color.messageSuccess));
                messageView.setTextColor(ContextCompat.getColor( this,R.color.messageSuccess));
                messageScrollView.setBackground(ContextCompat.getDrawable(this,R.drawable.message_border_success));
                break;
        }

        messageHeaderView.setText(messageHeader);
        messageView.setText(message);

        if(action != null){
            switch (action){
                case ConnectionNetwork:
                    // Додати виклик вікна підключення до мережі
                    break;
                case BluetoothConnection:
                    // Додати виклик вікна підключення по блютуз
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }
}
