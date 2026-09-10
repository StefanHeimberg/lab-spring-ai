package ch.stefanheimberg.lab_spring_ai;

import org.springframework.ai.tool.annotation.ToolParam;

public class WeatherTools {

    public String getWeather(final String city, final String at) {
        return String.format(
                "Weather forecast for %s%s: Sunny, 22°C",
                city,
                at != null ? " at " + at : ""
        );
    }

    public String getCurrentWeather(final String city) {
        return String.format(
                "Current Weather for %s: Sunny, 19°C",
                city
        );
    }

}
