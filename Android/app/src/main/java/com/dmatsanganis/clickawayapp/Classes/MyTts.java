package com.dmatsanganis.clickawayapp.Classes;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTts {
    private TextToSpeech tts;

    private TextToSpeech.OnInitListener initListener= new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {
            tts.setLanguage(Locale.forLanguageTag("EL"));
        }
    };

    public MyTts(Context context){
        tts = new TextToSpeech(context,initListener);
    }

    //Method that announces the string message in the arguments
    public void speak(String message){
        tts.speak(message,TextToSpeech.QUEUE_ADD,null,null);
    }

    //Method that stops the audio playback
    public void stop(){
        tts.stop();
    }
}