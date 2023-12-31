package io.dutwrapper.dutwrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.dutwrapper.dutwrapper.customrequest.CustomRequest;
import io.dutwrapper.dutwrapper.customrequest.CustomResponse;
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem;

public class Utils {
    public static Long getCurrentTimeInUnix() {
        return System.currentTimeMillis();
    }

    public static String findFirstString(String test, String regex) {
        final Pattern patternDate = Pattern.compile(regex);
        final Matcher matcher = patternDate.matcher(test);
        if (matcher.find()) {
            return matcher.group(0);
        }
        else return null;
    }

    public static Long date2UnixTimestamp(String input) {
        // In format dd/MM/yyyy
        long date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            date = LocalDateTime.of(
                    LocalDate.parse(input, formatter),
                    LocalTime.of(0, 0, 0)
            ).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (Exception ignored) {
            date = -1L;
        }
        return date;
    }

    public static DutSchoolYearItem getCurrentSchoolWeek() throws Exception {
        CustomResponse response = CustomRequest.get(
            "",
            Variables.URL_SCHOOLCURRENTWEEK,
            60);

        if (response.getReturnCode() < 200 || response.getReturnCode() >= 300)
            throw new Exception(
                    "Server was return with code " + response.getReturnCode() + ". May be you haven't logged in?");

        Document webData = Jsoup.parse(response.getContentHtmlString());
        DutSchoolYearItem result = new DutSchoolYearItem();

        // Get all and set 'selected' tag in school year list.
        Elements yearList = webData.getElementById("dnn_ctr442_View_cboNamhoc").getElementsByTag("option");
        yearList.sort((s1, s2) -> {
            Integer v1 = Integer.parseInt(s1.val()), v2 = Integer.parseInt(s2.val());
            
            return (v1 < v2) ? 1 : (v1 > v2) ? -1 : 0;
        });
        for (Element yearItem: yearList) {
            if (yearItem.hasAttr("selected")) {
                result.setSchoolYear(yearItem.text());
                result.setSchoolYearVal(Integer.parseInt(yearItem.val()));
            }
            break;
        }

        // Get all and set 'selected' tag in week list.
        Elements weekList = webData.getElementById("dnn_ctr442_View_cboTuan").getElementsByTag("option");
        weekList.sort((s1, s2) -> {
            Integer v1 = Integer.parseInt(s1.val()), v2 = Integer.parseInt(s2.val());
            
            return (v1 < v2) ? 1 : (v1 > v2) ? -1 : 0;
        });
        for (Element weekItem: weekList) {
            if (weekItem.hasAttr("selected")) {
                Pattern pattern = Pattern.compile(
                    "Tuần thứ (\\d{1,2}): (\\d{1,2}\\/\\d{1,2}\\/\\d{4})",
                    Pattern.CASE_INSENSITIVE
                    );
                Matcher matcher = pattern.matcher(weekItem.text());
                if (matcher.find()) {
                    if (matcher.groupCount() == 2) {
                        result.setWeek(Integer.parseInt(matcher.group(1)));
                    }
                    break;
                }
            }
            break;
        }

        return result;
    }
}
