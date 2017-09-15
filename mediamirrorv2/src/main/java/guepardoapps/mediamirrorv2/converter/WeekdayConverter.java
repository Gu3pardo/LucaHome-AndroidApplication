package guepardoapps.mediamirrorv2.converter;

public class WeekdayConverter {
    private static final String TAG = WeekdayConverter.class.getSimpleName();

    public static String GetWeekday(int weekday) {
        String string;
        switch (weekday) {
            case 1:
                string = "Sunday";
                break;
            case 2:
                string = "Monday";
                break;
            case 3:
                string = "Tuesday";
                break;
            case 4:
                string = "Wednesday";
                break;
            case 5:
                string = "Thursday";
                break;
            case 6:
                string = "Friday";
                break;
            case 7:
                string = "Saturday";
                break;
            default:
                string = "n.a.";
                break;
        }

        return string;
    }

    public static String GetShortWeekday(int weekday) {
        String string;
        switch (weekday) {
            case 1:
                string = "Su";
                break;
            case 2:
                string = "Mo";
                break;
            case 3:
                string = "Tu";
                break;
            case 4:
                string = "We";
                break;
            case 5:
                string = "Th";
                break;
            case 6:
                string = "Fr";
                break;
            case 7:
                string = "Sa";
                break;
            default:
                string = "n.a.";
                break;
        }

        return string;
    }
}
