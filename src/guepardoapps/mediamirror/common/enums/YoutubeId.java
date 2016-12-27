package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum YoutubeId implements Serializable {

	DEFAULT(15, "The Good Life No.15", "MbOUOBQ6wEE"), 
	THE_GOOD_LIFE_NO_1(1, "The Good Life No.1", "02PzGpODB1E"), 
	THE_GOOD_LIFE_NO_2(2, "The Good Life No.2", "Zg0vgeSb2h0"), 
	THE_GOOD_LIFE_NO_3(3, "The Good Life No.3", "f_XO9_hhJUY"), 
	THE_GOOD_LIFE_NO_4(4, "The Good Life No.4", "trvZIlM__pY"), 
	THE_GOOD_LIFE_NO_5(5, "The Good Life No.5", "IPbRufJ54Ww"), 
	THE_GOOD_LIFE_NO_6(6, "The Good Life No.6", "rzpp2lFt4Ns"), 
	THE_GOOD_LIFE_NO_7(7, "The Good Life No.7", "KKfFYS1CKRg"), 
	THE_GOOD_LIFE_NO_8(8, "The Good Life No.8", "tbXZDA-z68E"), 
	THE_GOOD_LIFE_NO_9(9, "The Good Life No.9", "OvXdL7PwF0U"), 
	THE_GOOD_LIFE_NO_10(10, "The Good Life No.10", "cDT2Zlab7og"), 
	THE_GOOD_LIFE_NO_11(11, "The Good Life No.11", "jHDYrSr5LXc"), 
	THE_GOOD_LIFE_NO_12(12, "The Good Life No.12", "SHrIR2Z861s"), 
	THE_GOOD_LIFE_NO_13(13, "The Good Life No.13", "Ho_aTzAMZIc"), 
	THE_GOOD_LIFE_NO_14(14, "The Good Life No.14", "9iJlmnkG8qc"), 
	THE_GOOD_LIFE_NO_15(15, "The Good Life No.15", "MbOUOBQ6wEE"), 
	THE_GOOD_LIFE_NO_16(16, "The Good Life No.16", "E2ELhVH_RVM"), 
	THE_GOOD_LIFE_NO_17(17, "The Good Life No.17", "c00q-02KAyA"), 
	THE_GOOD_LIFE_NO_18(18, "The Good Life No.18", "ua_FLsIA4k4"), 
	THE_GOOD_LIFE_NO_19(19, "The Good Life No.19", "Y9ega3zIeZI"), 
	THE_GOOD_LIFE_NO_20(20, "The Good Life No.20", "47zZVnPuY5I"), 
	THE_GOOD_LIFE_NO_21(21, "The Good Life No.21", "Z9GntMcHH50"), 
	THE_GOOD_LIFE_NO_22(22, "The Good Life No.22", "DmDbAQxYzPA"), 
	THE_GOOD_LIFE_NO_23(23, "The Good Life No.23", "vdaPEJ5xvbw"), 
	THE_GOOD_LIFE_NO_24(24, "The Good Life No.24", "t0A13F7BH_U"), 
	THE_GOOD_LIFE_NO_25(25, "The Good Life No.25", "__xMBY290Ng"), 
	THE_GOOD_LIFE_NO_26(26, "The Good Life No.26", "Z8wZxgAd07s"), 
	THE_GOOD_LIFE_NO_27(27, "The Good Life No.27", "dGWsd8-BB84"), 
	THE_GOOD_LIFE_STREAM(28, "The Good Life Live Stream", "uNN6Pj06Cj8"), 
	LUCKY_CHOPS_LIVE_DANZA(29, "Lucky Chops - Danza 2016 (LIVE at Grand Central Station, NYC)", "ddijvwAMw0o"), 
	LUCKY_CHOPS_LIVE_BUYO(30, "Lucky Chops - Buyo (LIVE)", "gFrCyU_1zw0"), 
	LUCKY_CHOPS_LIVE_STAND_BY_ME(31, "Lucky Chops - Stand by me", "i5QWuTAUsfg"), 
	LUCKY_CHOPS_LIVE_FUNKYTOWN_I_FEEL_GOOD(32, "Lucky Chops - Funkytown/I Feel Good", "NlZ4PPTNqLc"), 
	LUCKY_CHOPS_LIVE_HELLO_ADELE_COVER(33, "Lucky Chops - Hello (ADELE COVER)", "9mOMmP_aKso"), 
	LUCKY_CHOPS_LIVE_HEY_SOUL_SISTER(34, "Lucky Chops NYC - Hey, Soul Sister", "2TprNmtvLdI");

	private int _id;
	private String _title;
	private String _youtubeId;

	private YoutubeId(int id, String title, String youtubeId) {
		_id = id;
		_title = title;
		_youtubeId = youtubeId;
	}

	public int GetId() {
		return _id;
	}
	
	public String GetTitle() {
		return _title;
	}
	
	public String GetYoutubeId() {
		return _youtubeId;
	}

	public static YoutubeId GetById(int id) {
		for (YoutubeId e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}

	public static YoutubeId GetByTitle(String title) {
		for (YoutubeId e : values()) {
			if (e._title.contains(title)) {
				return e;
			}
		}
		return null;
	}
}
