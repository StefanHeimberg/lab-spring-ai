package ch.stefanheimberg.lab_spring_ai.toolcalling.tools;

public class WeatherTools {

    public record CurrentWeatherRequest(String city) {}

    public String getWeather(final String city, final String at) {
        return String.format(
                "Weather forecast for %s%s: Sunny, 22°C",
                city,
                at != null ? " at " + at : ""
        );
    }

    public String getCurrentWeather(final CurrentWeatherRequest currentWeatherRequest) {
        return String.format(
                "Current Weather for %s: Sunny, 19°C",
                currentWeatherRequest.city
        );
    }

}
