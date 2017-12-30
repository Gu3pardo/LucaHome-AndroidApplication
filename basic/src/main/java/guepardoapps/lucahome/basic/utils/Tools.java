package guepardoapps.lucahome.basic.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import guepardoapps.lucahome.basic.R;

public class Tools {
    private static final String TAG = Tools.class.getSimpleName();

    public static Bitmap GetCircleBitmap(@NonNull Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.BLACK;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getHeight(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    /**
     * Solution from
     * https://stackoverflow.com/questions/3509178/getting-a-photo-from-a-contact
     *
     * @param context the context of an activity or service
     * @param name    is the name of the contact you are looking for
     * @param height  height of the bitmap to return
     * @param width   width of the bitmap to return
     * @param round   boolean which defines if the returned bitmap shall be round
     * @return returns a bitmap of a contact if found or a dummy bitmap
     */
    public static Bitmap RetrieveContactPhoto(@NonNull Context context, @NonNull String name, int height, int width, boolean round) {
        String contactId = null;
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_face_white_48dp);

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(name));

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext() && contactId == null) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            }
            cursor.close();
        }

        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Cursor photoCursor = contentResolver.query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (photoCursor != null) {
            try {
                if (photoCursor.moveToFirst()) {
                    byte[] data = photoCursor.getBlob(0);
                    if (data != null) {
                        photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                    }
                }
            } finally {
                photoCursor.close();
            }
        }

        if (round) {
            return GetCircleBitmap(photo.copy(Bitmap.Config.ARGB_8888, true));
        }

        return photo;
    }

    public static byte[] CompressStringToByteArray(@NonNull String text) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            OutputStream outputStream = new DeflaterOutputStream(byteArrayOutputStream);
            outputStream.write(text.getBytes("UTF-8"));
            outputStream.close();
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static String DecompressByteArrayToString(byte[] bytes) {
        InputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        } catch (IOException exception) {
            Logger.getInstance().Error(TAG, exception.getMessage());
            return "";
        }
    }
}
