package com.example.appchat.widget.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import com.example.appchat.objectclass.Avatar;
import com.example.appchat.objectclass.Member;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class ScaleBitmap {

    public static Bitmap scaleBitmap(final Bitmap input, final long maxBytes) {
        final int currentWidth = input.getWidth();
        final int currentHeight = input.getHeight();
        final int currentPixels = currentWidth * currentHeight;
        // Get the amount of max pixels:
        // 1 pixel = 4 bytes (R, G, B, A)
        final long maxPixels = maxBytes / 4; // Floored
        if (currentPixels <= maxPixels) {
            // Already correct size:
            return input;
        }
        // Scaling factor when maintaining aspect ratio is the square root since x and y have a relation:
        final double scaleFactor = Math.sqrt(maxPixels / (double) currentPixels);
        final int newWidthPx = (int) Math.floor(currentWidth * scaleFactor);
        final int newHeightPx = (int) Math.floor(currentHeight * scaleFactor);
        final Bitmap output = Bitmap.createScaledBitmap(input, newWidthPx, newHeightPx, true);
        return output;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public static Avatar encodeBase64Avatar(Context context, Bitmap bitmap, Uri contentUri) {
        String realPath = getRealPathFromURI(contentUri, context);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        int index = realPath.lastIndexOf("/");
        String name1 = realPath.substring(++index);
        String[] split = name1.split("\\.");
        String name = split[0] + System.currentTimeMillis() + Member.getInstance(context).getPhone() + "." + split[1];
        Avatar avatar = new Avatar(encoded, name);
        return avatar;
    }

    public static Avatar encodeBase64Avatar(Context context, Bitmap bitmap, String realPath) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        int index = realPath.lastIndexOf("/");
        String name1 = realPath.substring(++index);
        String[] split = name1.split("\\.");
        Random random = new Random();
        int i = random.nextInt((999999 - 100000) + 100000);
        String name = split[0] + System.currentTimeMillis() + i + Member.getInstance(context).getPhone() + "." + split[1];
        Avatar avatar = new Avatar(encoded, name);
        return avatar;
    }
}
