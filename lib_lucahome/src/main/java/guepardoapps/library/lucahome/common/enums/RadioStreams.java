package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum RadioStreams implements Serializable {

    DEFAULT(0, "Mix Nation", "http://www.mixnation.de/listen.pls");

    private int _id;
    private String _title;
    private String _url;

    RadioStreams(int id, String title, String url) {
        _id = id;
        _title = title;
        _url = url;
    }

    public int GetId() {
        return _id;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetUrl() {
        return _url;
    }

    public static RadioStreams GetById(int id) {
        for (RadioStreams e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return DEFAULT;
    }

    public static RadioStreams GetByTitle(String title) {
        for (RadioStreams e : values()) {
            if (e._title.contains(title)) {
                return e;
            }
        }
        return DEFAULT;
    }

    public static RadioStreams GetByUrl(String url) {
        for (RadioStreams e : values()) {
            if (e._url.contains(url)) {
                return e;
            }
        }
        return DEFAULT;
    }
}
