package guepardoapps.lucahome.common.classes;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class MagazinDir implements Serializable {
    private static final long serialVersionUID = 1027488495037954860L;
    private static final String TAG = MagazinDir.class.getSimpleName();

    private String _dirName;
    private String[] _dirContent;
    private Bitmap _icon;

    public MagazinDir(
            @NonNull String dirName,
            @NonNull String[] dirContent,
            @NonNull Bitmap icon) {
        _dirName = dirName;
        _dirContent = dirContent;
        _icon = icon;
    }

    public String GetDirName() {
        return _dirName;
    }

    public String[] GetDirContent() {
        return _dirContent;
    }

    public void SetDirContent(@NonNull String[] dirContent) {
        _dirContent = dirContent;
    }

    public Bitmap GetIcon() {
        return _icon;
    }


    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {DirName: %s};{DirContent: %s};{Icon: %s}}", TAG, _dirName, _dirContent, _icon);
    }
}
