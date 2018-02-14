package guepardoapps.lucahome.common.utils;

import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@SuppressWarnings({"WeakerAccess"})
public class Tools {
    private static final String Tag = Tools.class.getSimpleName();

    /**
     * @param text Text which should be compressed
     * @return returns a compressed byte array of the string
     */
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

    /**
     * @param bytes       is the compressed byte array
     * @param charsetName char set which should be used for the string
     * @return returns a decompressed string
     */
    public static String DecompressByteArrayToString(byte[] bytes, @NonNull String charsetName) {
        InputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            return new String(byteArrayOutputStream.toByteArray(), charsetName);
        } catch (IOException exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return "";
        }
    }

    /**
     * @param bytes is the compressed byte array
     * @return returns a decompressed string
     */
    public static String DecompressByteArrayToString(byte[] bytes) {
        return DecompressByteArrayToString(bytes, "UTF-8");
    }
}
