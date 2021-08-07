package com.example.wechat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public  class SpUtils {

    public static <T> void saveBean2Sp(Context context,T t ,String fileName,String key){
        SharedPreferences preferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        ByteArrayOutputStream bos;
        ObjectOutputStream oos =null;
        try{
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(t);
            byte[] bytes = bos.toByteArray();
            String objStr = Base64.encodeToString(bytes,Base64.DEFAULT);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key,objStr);
            editor.commit();
        } catch (IOException e) {
            Log.d("CTB_Sp",e.getMessage());
            e.printStackTrace();
        }finally {
            if(oos != null ){
                try{
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    Log.d("CTB_sp_flush",e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }


    public static <T extends  Object> T getBeanFromSp(Context context,String fileName,String key){
        SharedPreferences preferences = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        byte[] bytes = Base64.decode(preferences.getString(key,""),Base64.DEFAULT);
        ByteArrayInputStream bis;
        ObjectInputStream ois = null ;
        T obj = null;
        try{
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = (T) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(ois != null){
                try{
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }



}
