package com.example.katny.mamorygame;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

import static android.graphics.BitmapFactory.decodeStream;

public class Game extends AppCompatActivity {


    boolean[] isClicked = {false, false, false, false,
            false, false,false, false,
            false, false, false, false,
            false, false, false, false};
    ImageView[] availableThumbnails;
    Bitmap[] takenPhotos;
    DatabaseHelper myDb;
    int points=0;



    int ids[]={ R.id.pic00,R.id.pic01,R.id.pic02,R.id.pic03,
            R.id.pic10,R.id.pic11,R.id.pic12,R.id.pic13,
            R.id.pic20,R.id.pic21,R.id.pic22,R.id.pic23,
            R.id.pic30,R.id.pic31,R.id.pic32,R.id.pic33};





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        myDb = new DatabaseHelper(this);

        takenPhotos = new Bitmap[8];



        Cursor data = myDb.getAllData();



        availableThumbnails = new ImageView[16];
        for(int i=0;i<16;i++){
            availableThumbnails[i] = findViewById(ids[i]);
        }
        for(int i=0;i<16;i++) {
            availableThumbnails[i].setTag(""+i);
        }
        shuffleArray(availableThumbnails);
        setupListeners(availableThumbnails);



        if (data.getCount() != 0) {
            int i = 0;
            while (data.moveToNext() && i<8) {
                takenPhotos[i++] = loadImageFromStorage(data.getString(1), data.getString(2));
            }
        }



        }



    public void setupListeners(final ImageView[] arr) {
        for (final ImageView imgView : arr) {
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for(int i = 0, j = 0; i <= 15; i++) {
                        if(i % 2 == 0 && i != 0) j++;
                        if (v == availableThumbnails[i]) {
                            imgView.setImageBitmap(takenPhotos[j]);
                            isClicked[i] = true;
                        }
                    }
                    if(!Match()) {
                        if(userPickedTwoImgs(isClicked)) {
                            int i = 0;
                            for (final ImageView img : arr) {
                                if(isClicked[i++])
                                    restoreQuestionMarks(img);
                            }
                            Arrays.fill(isClicked, false);
                        }
                    }
                }
            });
        }
    }

    private Bitmap loadImageFromStorage(String path, String fileName) {
        try {
            File fileToLoad = new File(path, fileName);
            return decodeStream(new FileInputStream(fileToLoad));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean userPickedTwoImgs(boolean[] arr) {
        int clickedImgs = 0;
        for (boolean arrayItem : arr) {
            if(arrayItem)
                clickedImgs++;
        }
        return clickedImgs >= 2;
    }

    public boolean Match() {
        for(int i = 0; i < 15; i+=2) {
            if(isClicked[i] && isClicked[i+1]) {
                fadeOutAndHideImage(availableThumbnails[i]);
                fadeOutAndHideImage(availableThumbnails[i+1]);
                isClicked[i] = false;
                isClicked[i+1] = false;
                points++;
                checkEnd();
                return true;
            }
        }
        return false;
    }

    private void checkEnd() {
        if (points==8) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Game.this);
            alertDialogBuilder
                    .setMessage("Gratuluje! Wygrałeś!")
                    .setCancelable(false)
                    .setPositiveButton("NEW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    })
                    .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    public void fadeOutAndHideImage(final ImageView img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.INVISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });
        img.startAnimation(fadeOut);
    }

    public void restoreQuestionMarks(final ImageView img) {
        Animation restoreQMark = new AlphaAnimation(1, 0);
        restoreQMark.setInterpolator(new AccelerateInterpolator());
        restoreQMark.setDuration(500);
        restoreQMark.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setImageResource(R.drawable.qmark);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });
        img.startAnimation(restoreQMark);
    }




    public void restartGame(View view) {
        shuffleArray(availableThumbnails);
        setupListeners(availableThumbnails);
        for (final ImageView img : availableThumbnails) {
            restoreQuestionMarks(img);
            img.setVisibility(View.VISIBLE);
        }
        Arrays.fill(isClicked, false);
        points=0;
    }

    public void shuffleArray(ImageView[] arr) {
        Random rand = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            ImageView a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
    }

    public void Menu(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
