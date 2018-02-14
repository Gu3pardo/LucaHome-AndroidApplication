package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum RadioStreamType implements Serializable {

    PLANET_RADIO_LIVE(0, "Planet Radio Live", "http://mp3.planetradio.de/planetradio/hqlivestream.mp3"),
    PLANET_RADIO_BLACK_BEATS(1, "Planet Radio Black Beats", "http://mp3.planetradio.de/plrchannels/blackbeats.mp3"),
    PLANET_RADIO_NIGHT_WAX(2, "Planet Radio Night Wax", "http://mp3.planetradio.de/plrchannels/nightwax.mp3"),
    PLANET_RADIO_THE_CLUB(3, "Planet Radio The Club", "http://mp3.planetradio.de/plrchannels/theclub.mp3"),
    PLANET_RADIO_OLD_SCHOOL(4, "Planet Radio Old School", "http://mp3.planetradio.de/plrchannels/oldschool.mp3"),
    PLANET_RADIO_I_TUNES(5, "Planet Radio iTunes", "http://mp3.planetradio.de/plrchannels/itunes.mp3"),
    I_LOVE_RADIO(6, "I Love Radio", "http://stream01.iloveradio.de/iloveradio1.mp3"),
    I_LOVE_DANCE(7, "I Love Dance", "http://stream01.iloveradio.de/iloveradio2.mp3"),
    I_LOVE_THE_BATTLE(8, "I Love The Battle", "http://stream01.iloveradio.de/iloveradio3.mp3"),
    I_LOVE_BRAVO_CHARTS(9, "I Love Bravo Charts", "http://stream01.iloveradio.de/iloveradio9.mp3"),
    I_LOVE_MASH_UP(10, "I Love Mash Up", "http://stream01.iloveradio.de/iloveradio5.mp3"),
    I_LOVE_BASS(11, "I Love Bass", "http://stream01.iloveradio.de/iloveradio4.mp3"),
    I_LOVE_RADIO_N_CHILL(12, "I Love Radio & Chill", "http://stream01.iloveradio.de/iloveradio10.mp3"),
    BAYERN_3(13, "Bayern 3", "http://streams.br.de/bayern3_1.mp3"),
    TROPICAL_100_SALSA(14, "Tropical 100 Salsa", "http://tp100.dynv6.net:8008/listen.mp3"),
    TROPICAL_100_MIX(15, "Tropical 100 Mix", "http://tp100.dynv6.net:7996/listen.mp3"),
    TROPICAL_100_SUAVE(16, "Tropical 100 Suave", "http://tp100.dynv6.net:8004/listen.mp3"),
    TROPICAL_100_MERENGUE(17, "Tropical 100 Merengue", "http://tp100.dynv6.net:8012/listen.mp3"),
    TROPICAL_100_FIESTA(18, "Tropical 100 Fiesta", "http://tp100.dynv6.net:8020/listen.mp3"),
    TROPICAL_100_BACHARENGUE(19, "Tropical 100 Bacharengue", "http://tp100.dynv6.net:8016/listen.mp3"),
    TROPICAL_100_LIGHT_DANCE(20, "Tropical 100 Light Dance", "http://tp100a.dynv6.net:8050/listen.mp3");

    private int _id;
    private String _title;
    private String _url;

    RadioStreamType(int id, String title, String url) {
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

    public static RadioStreamType GetById(int id) {
        for (RadioStreamType e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return PLANET_RADIO_LIVE;
    }

    public static RadioStreamType GetByTitle(String title) {
        for (RadioStreamType e : values()) {
            if (e._title.contains(title)) {
                return e;
            }
        }
        return PLANET_RADIO_LIVE;
    }
}
