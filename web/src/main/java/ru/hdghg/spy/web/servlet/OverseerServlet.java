package ru.hdghg.spy.web.servlet;

import lombok.extern.slf4j.Slf4j;
import ru.hdghg.spy.service.dao.Storage;
import ru.hdghg.spy.service.model.History;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(urlPatterns = {"/overseer"})
@Slf4j
public class OverseerServlet extends HttpServlet {

    @Inject
    private Storage storage;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (null == ipAddress) {
            ipAddress = req.getRemoteAddr();
        }
        log.debug("Overseer request from {}", ipAddress);
        List<History> allHistory = storage.all();
        List<History> messages = new LinkedList<>();
        for (History history : allHistory) {
            if (null == history.getStatus()) {
                messages.add(history);
            }
        }
        if (0 == messages.size()) {
            return;
        }
        Collections.sort(messages, new Comparator<History>() {
            @Override
            public int compare(History o1, History o2) {
                if (o1.getTimestamp().before(o2.getTimestamp())) {
                    return -1;
                }
                if (o1.getTimestamp().after(o2.getTimestamp())) {
                    return 1;
                }
                return 0;
            }
        });
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(messages.get(0).getTimestamp());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int counter = 0;
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Overseer control panel</title>");
            out.println("</head>");
            out.println("<body>");
            Map<String, Map<String, List<Integer>>> days = new TreeMap<>();
            String date = new SimpleDateFormat("yyyy MM dd").format(calendar.getTime());
            days.put(date, new TreeMap<String, List<Integer>>());
            for (History history : messages) {
                calendar.setTime(history.getTimestamp());
                int dayHistory = calendar.get(Calendar.DAY_OF_MONTH);
                Map<String, List<Integer>> dayReport;
                if (day != dayHistory) {
                    counter++;
                    day = dayHistory;
                    date = new SimpleDateFormat("yyyy MM dd").format(calendar.getTime());
                    days.put(date, new TreeMap<String, List<Integer>>());
                }
                dayReport = days.get(date);

                String jid = history.getJid();
                List<Integer> dayPart = dayReport.get(jid);
                if (dayPart == null) {
                    dayPart = new ArrayList<>();
                    while (dayPart.size() < 288) {
                        dayPart.add(0);
                    }
                    dayReport.put(jid, dayPart);
                }
                int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                dayPart.set(minutes / 5, dayPart.get(minutes / 5) + 1);
            }
            log.debug("Result has size: {}", counter);
            for (Map.Entry<String, Map<String, List<Integer>>> h : days.entrySet()) {
                out.println("<div>");
                out.println("<b>Day " + h.getKey() + " </b>");
                for (Map.Entry<String, List<Integer>> entry : h.getValue().entrySet()) {
                    out.println("<br /> User: " + entry.getKey() + "<br />");
                    out.println("<div style='border: 1px solid black; display:inline-block'>");
                    List<Integer> value = entry.getValue();
                    SimpleDateFormat formatStart = new SimpleDateFormat("HH:mm");
                    formatStart.setTimeZone(TimeZone.getTimeZone("UTC"));
                    for (int i = 0; i < value.size(); i++) {
                        Integer d = value.get(i);
                        int ret = d > 12 ? 12 : d;
                        String start = formatStart.format(i * 60 * 5 * 1000);
                        String end = formatStart.format((i + 1) * 60 * 5 * 1000);
                        out.print(String.format("<img class='h%d' src='img/%d.png' title='Time: %s-%s, messages: %d' />",
                                i, ret, start, end, d));
                    }
                    out.println("</div>");
                }
                out.println("</div>");
            }
            out.println("</body>");
        }
    }
}
