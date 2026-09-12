package ch.stefanheimberg.lab_spring_ai.toolcalling.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTools.class);
    private LocalDateTime alarm;

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        LOGGER.info("function getCurrentDateTime() called");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    public String getAlarm() {
        LOGGER.info("function getAlarm() called");
        if (null == alarm) {
            return null;
        } else {
            return alarm.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }
    }

    @Tool(description = "Set a user alarm for the given time")
    void setAlarm(@ToolParam(description = "Time in ISO-8601 format") final String time) {
        LOGGER.info("function setAlarm(String) called");
        alarm = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        LOGGER.info("Alarm set for {}", alarm);
    }

}
