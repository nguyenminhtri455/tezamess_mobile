package com.example.appchat.widget.cache;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TezamessCache {

    public static final String TAG = "TezamessLog";
    private static final String FORDER_NAME = "tezamess-cache";
    public static final String LIST_CONTACT = "list-contact";
    public static final String LIST_FRIEND = "list-friend";

    public static void saveCache(Context context, String fileName, Object object) {
        String pathCache = context.getCacheDir().getAbsolutePath();
        File file = new File(pathCache + File.separator + FORDER_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();

            byte[] bytes = byteArrayOutputStream.toByteArray();

            fileOutputStream = new FileOutputStream(new File(file, fileName));
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
        }
    }

    public static void updateCache(Context context, String fileName, Object object) {
        deleteCache(context, fileName);
        saveCache(context, fileName, object);
    }

    public static void deleteCache(Context context, String fileName) {
        String pathCache = context.getCacheDir().getAbsolutePath();
        File file = new File(pathCache + File.separator + FORDER_NAME);
        if (!file.exists()) {
            return;
        }
        String[] listFile = file.list();
        if (listFile.length <= 0) {
            return;
        }
        File f = new File(file, fileName);
        if (f.exists()) {
            f.delete();
        }
    }

    public static Object getCache(Context context, String fileName) {
        String pathCache = context.getCacheDir().getAbsolutePath();
        File file = new File(pathCache + File.separator + FORDER_NAME);
        if (!file.exists()) {
            return null;
        }
        File f = new File(file, fileName);
        if (!f.exists()) {
            return null;
        }
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(f);
            objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            return object;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
