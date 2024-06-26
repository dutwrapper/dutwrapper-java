package io.dutwrapper.dutwrapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.dutwrapper.dutwrapper.model.accounts.LessonItem;
import io.dutwrapper.dutwrapper.model.accounts.SubjectCodeItem;
import io.dutwrapper.dutwrapper.model.enums.LessonStatus;
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType;
import io.dutwrapper.dutwrapper.model.enums.NewsType;
import io.dutwrapper.dutwrapper.model.news.LinkItem;
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem;
import io.dutwrapper.dutwrapper.model.news.NewsSubjectAffectedItem;
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem;

@SuppressWarnings("SpellCheckingInspection")
public class News {
    public static ArrayList<NewsGlobalItem> getNews(
            @Nullable NewsType newsType,
            @Nullable Integer page,
            @Nullable NewsSearchType searchType,
            @Nullable String searchQuery) throws Exception, IOException {
        String url = String.format(
                Variables.URL_NEWS,
                newsType == null ? NewsType.Global.toString() : newsType.toString(),
                page == null ? 1 : page,
                searchType == null ? NewsSearchType.ByTitle.toString() : searchType.toString(),
                URLEncoder.encode(searchQuery == null ? "" : searchQuery, StandardCharsets.UTF_8.toString()));

        HttpClientWrapper.Response response = HttpClientWrapper.get(url, null, 60);
        response.throwExceptionIfExist();

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new Exception("Server was returned with code " + response.getStatusCode() + ".");
        }

        // https://www.baeldung.com/java-with-jsoup
        if (response.getContent() == null) {
            return new ArrayList<>();
        }

        Document webData = Jsoup.parse(response.getContent());
        webData.outputSettings().charset(StandardCharsets.UTF_8);
        for (Element el : webData.getElementsByTag("br")) {
            el.remove();
        }

        // News General + News Subject
        Elements tbBox = webData.getElementsByClass("tbbox");

        ArrayList<NewsGlobalItem> newsList = new ArrayList<>();
        for (Element tb1 : tbBox) {
            NewsGlobalItem newsItem = new NewsGlobalItem();

            Element title = tb1.getElementsByClass("tbBoxCaption").get(0);
            String[] titleTemp = title.text().split(":", 2);
            Element content = tb1.getElementsByClass("tbBoxContent").get(0);

            if (titleTemp.length == 2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(titleTemp[0], formatter);
                LocalTime time = LocalTime.parse("00:00:00");
                LocalDateTime dateTime = date.atTime(time);
                newsItem.setDate(dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
                newsItem.setTitle(titleTemp[1].trim());
            } else
                newsItem.setTitle(title.text().trim());

            newsItem.setContent(content.html());
            newsItem.setContentString(content.wholeText());

            // Find links and set to item
            ArrayList<LinkItem> links = new ArrayList<>();
            int position = 0;
            String temp1 = content.wholeText();
            Elements temp2 = content.getElementsByTag("a");
            for (Element item : temp2) {
                if (temp1.contains(item.wholeText())) {
                    position += temp1.indexOf(item.wholeText());
                    LinkItem item1 = new LinkItem(
                            item.wholeText(),
                            item.attr("abs:href"),
                            position);
                    links.add(item1);
                    position += item.wholeText().length();

                    // https://stackoverflow.com/questions/24220509/exception-when-replacing-brackets
                    String[] temp3 = temp1.split(Pattern.quote(item.wholeText()), 2);
                    if (temp3.length > 1)
                        temp1 = temp3[1];
                }
            }
            newsItem.setLinks(links);

            newsList.add(newsItem);
        }

        return newsList;
    }

    public static ArrayList<NewsGlobalItem> getNewsGlobal(
            @Nullable Integer page,
            @Nullable NewsSearchType searchType,
            @Nullable String searchQuery) throws Exception {
        return getNews(
                NewsType.Global,
                page == null ? 1 : page,
                searchType,
                searchQuery);
    }

    public static ArrayList<NewsSubjectItem> getNewsSubject(
            @Nullable Integer page,
            @Nullable NewsSearchType searchType,
            @Nullable String searchQuery) throws Exception {
        ArrayList<NewsSubjectItem> result = new ArrayList<>();
        ArrayList<NewsGlobalItem> listTemp = getNews(
                NewsType.Subject,
                page == null ? 1 : page,
                searchType,
                searchQuery);

        for (NewsGlobalItem item : listTemp) {
            NewsSubjectItem subjectItem = new NewsSubjectItem();

            // Add as like news global.
            subjectItem.setDate(item.getDate());
            subjectItem.setTitle(item.getTitle());
            subjectItem.setContent(item.getContent());
            subjectItem.setContentString(item.getContentString());
            subjectItem.setLinks(subjectItem.getLinks());

            // For title
            try {
                String lecturerProcessing = item.getTitle().split(" thông báo đến lớp:")[0].trim();
                String[] splitted = lecturerProcessing.split(" ", 2);
                subjectItem.setLecturerGender(splitted[0].toLowerCase(Locale.ROOT).equals("cô"));
                subjectItem.setLecturerName(splitted[1]);

                subjectItem.getAffectedClass().addAll(getAffectedClass(item.getTitle()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // For content. If found something, do work. If not, just ignore.
            if (subjectItem.getContent().contains("HỌC BÙ")) {
                subjectItem.setLessonStatus(LessonStatus.MakeUp);
                subjectItem.setAffectedDate(Utils.date2UnixTimestamp(
                        Utils.findFirstString(subjectItem.getContentString(), "\\d{2}[-|/]\\d{2}[-|/]\\d{4}")));
                try {
                    subjectItem.setAffectedLesson(getLessonItem(
                            Objects.requireNonNull(Utils.findFirstString(subjectItem.getContentString().toLowerCase(),
                                    "tiết: .*[0-9],")).replace("tiết:", "").replace(",", "").trim()));
                } catch (Exception ignored) {
                }
                try {
                    subjectItem.setAffectedRoom(Objects
                            .requireNonNull(
                                    Utils.findFirstString(subjectItem.getContentString().toLowerCase(), "phòng:.*"))
                            .replace("phòng:", "").replace(",", "").trim().toUpperCase());
                } catch (Exception ignored) {
                }
            } else if (subjectItem.getContent().contains("NGHỈ HỌC")) {
                subjectItem.setLessonStatus(LessonStatus.Leaving);
                subjectItem.setAffectedDate(Utils.date2UnixTimestamp(
                        Utils.findFirstString(subjectItem.getContentString(), "\\d{2}[-|/]\\d{2}[-|/]\\d{4}")));
                try {
                    subjectItem.setAffectedLesson(getLessonItem(
                            Objects.requireNonNull(Utils.findFirstString(subjectItem.getContentString().toLowerCase(),
                                    "\\(tiết:.*[0-9]\\)")).replace("(tiết:", "").replace(")", "").trim()));
                } catch (Exception ignored) {
                }
            } else {
                subjectItem.setLessonStatus(LessonStatus.Unknown);
            }

            // Add to item.
            result.add(subjectItem);
        }

        return result;
    }

    private static LessonItem getLessonItem(String input) {
        if (input.contains("-")) {
            LessonItem item = new LessonItem();
            item.setStart(Integer.parseInt(input.split("-")[0]));
            item.setEnd(Integer.parseInt(input.split("-")[1]));
            return item;
        } else
            return null;
    }

    private static ArrayList<NewsSubjectAffectedItem> getAffectedClass(String input) {
        String subjectProcessing = input.split(" thông báo đến lớp:")[1].trim();
        ArrayList<String> data1 = Arrays.stream(subjectProcessing.split(" , "))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<NewsSubjectAffectedItem> data2 = new ArrayList<>();

        for (String item : data1) {
            String itemSubjectName = item.substring(0, item.indexOf("[")).trim();
            String itemClass = item.substring(item.indexOf("[") + 1, item.indexOf("]")).toLowerCase();
            SubjectCodeItem codeItem;
            if (itemClass.contains(".nh")) {
                String[] data = itemClass.split(".nh");
                codeItem = new SubjectCodeItem(
                        data[0],
                        data[1]);
            } else {
                String[] data = itemClass.split("nh");
                codeItem = new SubjectCodeItem(
                        data[0],
                        data[1]);
            }

            if (data2.stream().noneMatch(p -> Objects.equals(p.getSubjectName(), itemSubjectName))) {
                NewsSubjectAffectedItem item2 = new NewsSubjectAffectedItem();
                item2.setSubjectName(itemSubjectName);
                item2.getCodeList().add(codeItem);
                data2.add(item2);
            } else {
                Optional<NewsSubjectAffectedItem> tempdata = data2.stream()
                        .filter(p -> Objects.equals(p.getSubjectName(), itemSubjectName)).findFirst();
                if (tempdata.isPresent()) {
                    NewsSubjectAffectedItem temp = tempdata.get();
                    temp.getCodeList().add(codeItem);
                }
            }
        }

        return data2;
    }
}