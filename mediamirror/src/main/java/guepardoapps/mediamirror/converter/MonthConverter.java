package guepardoapps.mediamirror.converter;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MonthConverter {
    private static final String TAG = MonthConverter.class.getSimpleName();

    public static String GetMonth(int month) {
        String string;
        switch (month) {
            case 0:
                string = "January";
                break;
            case 1:
                string = "February";
                break;
            case 2:
                string = "March";
                break;
            case 3:
                string = "April";
                break;
            case 4:
                string = "May";
                break;
            case 5:
                string = "June";
                break;
            case 6:
                string = "July";
                break;
            case 7:
                string = "August";
                break;
            case 8:
                string = "September";
                break;
            case 9:
                string = "October";
                break;
            case 10:
                string = "November";
                break;
            case 11:
                string = "December";
                break;
            default:
                string = "n.a.";
                break;
        }

        return string;
    }
}
