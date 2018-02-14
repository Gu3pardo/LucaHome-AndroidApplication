package guepardoapps.lucahome.common.utils;

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

import guepardoapps.lucahome.common.R;

@SuppressWarnings({"WeakerAccess"})
public class BitmapHelper {

    /**
     * @param bitmap the original bitmap
     * @return returns a rounded bitmap
     */
    public static Bitmap GetCircleBitmap(@NonNull Bitmap bitmap) {
        return GetCircleBitmap(bitmap, bitmap.getHeight(), bitmap.getWidth());
    }

    /**
     * @param bitmap the original bitmap
     * @param height height of the bitmap to return
     * @param width  width of the bitmap to return
     * @return returns a rounded bitmap
     */
    public static Bitmap GetCircleBitmap(@NonNull Bitmap bitmap, int height, int width) {
        final Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.BLACK;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
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
            return GetCircleBitmap(photo.copy(Bitmap.Config.ARGB_8888, true), height, width);
        }

        return photo;
    }
}
