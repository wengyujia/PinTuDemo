package com.jia.pintu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import view.GamePinTuLayout;

public class MainActivity extends Activity {

    private GamePinTuLayout mGamePinTuLayout;
    private TextView mTextViewLevel;
    private TextView mTextViewTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mGamePinTuLayout = (GamePinTuLayout) findViewById(R.id.v_gamepintulayout);
        mTextViewLevel= (TextView) findViewById(R.id.tv_level);
        mTextViewTime= (TextView) findViewById(R.id.tv_time);
        /**
         * 13.调用接口，如果游戏成功，通过AlertDialog进入下一关卡
         */
        mGamePinTuLayout.setIsTiemEnabled(true);
        mGamePinTuLayout.setOnGamePintuListener(new GamePinTuLayout.GamePituListener() {
            @Override
            public void nextLevel(final int nextLevel) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("游戏过关").setMessage("恭喜过关")
                        .setPositiveButton("下一关", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGamePinTuLayout.nextLevel();
                                mTextViewLevel.setText(""+nextLevel);
                            }
                        }).show();
            }

            @Override
            public void timeChanged(int currentTime) {

                mTextViewTime.setText(""+currentTime);
            }

            @Override
            public void gameOver() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("游戏过关").setMessage("闯关失败")
                        .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mGamePinTuLayout.restart();
                            }
                        })
                        .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGamePinTuLayout.pause();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
       mGamePinTuLayout.resume();
    }


}
