package guepardoapps.lucahome.common.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.support.annotation.NonNull
import guepardoapps.lucahome.common.extensions.common.circleBitmap
import guepardoapps.lucahome.common.R
import java.io.ByteArrayInputStream

class BitmapHelper {

    companion object {
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
        fun retrieveContactPhoto(@NonNull context: Context, @NonNull name: String, height: Int, width: Int, round: Boolean): Bitmap {
            var contactId = ""
            var photo = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_face_white_48dp)

            val contentResolver: ContentResolver = context.contentResolver
            val projection: Array<String> = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)

            val uri: Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(name))

            val cursor: Cursor = contentResolver.query(uri, projection, null, null, null)
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            }
            cursor.close()

            val contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
            val photoUri: Uri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)

            val photoCursor: Cursor = contentResolver.query(photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null)
            photoCursor.use { pCursor ->
                if (pCursor.moveToFirst()) {
                    val data = pCursor.getBlob(0)
                    if (data != null) {
                        photo = BitmapFactory.decodeStream(ByteArrayInputStream(data))
                    }
                }
            }

            if (round) {
                return photo.circleBitmap(height, width, Color.BLACK)
            }

            return photo
        }
    }
}
