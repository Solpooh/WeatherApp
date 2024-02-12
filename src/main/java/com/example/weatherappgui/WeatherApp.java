package com.example.weatherappgui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// retrieve weather data from API - this backend logic will fetch the latest weather
// API에서 날씨 데이터 검색 - 이 백엔드 로직은 최신 날씨를 가져옴
// data from the external API and return it. The GUI will
// display this data to the user
// 외부 API의 데이터를 가져와서 반환 - GUI는 이 데이터를 사용자에게 표시함

public class WeatherApp {
    // fetch weather data for given location
    // 특정 위치의 날씨 데이터를 가져옴
    public static JSONObject getWeatherData(String locationName) {
        // get location coordinates using the geolocation API
        // geolocation API 를 사용하여 위치 좌표 얻기
        JSONArray locationData = getLocationData(locationName);
        System.out.println(locationData);

        // extract latitude and longitude data
        // 위도와 경도 데이터 추출
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // build API request URL with location coordinates
        // 위치 좌표로 API 요청 URL 구축
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
        "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

        try {
            // call api and get response
            // api 호출하고 응답을 받음
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for response status
            // 200 - means that the connection was a success
            // 응답 상태 확인 - 200 (연결성공)
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // store resulting json data
            // 결과 json data 저장
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                // read and store into the string builder
                // 읽어서 string builder에 저장
                resultJson.append(scanner.nextLine());
            }

            // close scanner
            // scanner 닫기
            scanner.close();

            // close url connection
            // url connection 닫기
            conn.disconnect();

            // parse through our data
            // 데이터 변환
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // retrieve hourly data
            // 시간별 데이터 검색
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // we want to get the current hour's data
            // so we need to get the index of our current hour
            // 현재 시간의 데이터를 얻기 위해서 현재 시간 인덱스를 얻음
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // get temperature
            // 온도 얻기
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // get weather code
            // 날씨 코드 얻기
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // get humidity
            // 습도 얻기
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // get windspeed
            // 풍속 얻기
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // build the weather json data object that we are going to access in our frontend
            // 프론트에서 접근할 날씨 json data 객체 만들기
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // retrieves geographic coordinates for given location name
    // 주어진 위치 이름에 대한 지리 좌표 검색
    public static JSONArray getLocationData(String locationName) {
        // replace any whitespace in location name to + to adhere to API's request format
        // ex) https://open-meteo.com/en/docs/geocoding-api#name=new+york
        // API 요청 형식을 준수하기 위해 위치 이름 공백을 '+' 로 바꿈
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        // 위치 매개변수를 사용해 API url 구축
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            // call api and get a response
            // api 호출하고 응답받음
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check response status
            // 200 means successful connection
            // 응답상태 체크 - 200은 연결 성공
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }else {
                // store the API results
                // API 결과 저장
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store the resulting json data into our string builder
                // 결과 json 데이터를 읽고 string builder에 저장
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                // scanner 닫기
                scanner.close();

                // close url connect
                // url 연결 닫기
                conn.disconnect();

                // parse the JSON string into a JSON obj
                // json string 을 json 객체로 변환
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // get the list of location data the API generated from the location name
                // 위치 이름에서 API가 생성한 위치 데이터 목록을 가져옴.
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // couldn't find location
        // 위치를 찾지 못했을 때
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // attempt to create connection
            // connection 생성
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to get (GET, PUT, DELETE, POST)
            // 요청 메서드 설정 (여기서는 GET 만)
            conn.setRequestMethod("GET");

            // connect to our API
            // api 연결
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // could not make connection
        // connection을 만들지 못했을 때
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        // iterate through the time list and see which one matches our current time
        // 시간 목록에서 현재 시간과 일치하는 것 반복해서 찾기
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                // return the index
                return i;
            }
        }

        return 0;
    }
    public static String getCurrentTime() {
        // get current date and time
        // 현재 시간, 날짜 가져오기
        LocalDateTime currentDateTime = LocalDateTime.now();

        // format date to be 2024-02-09T00:00 (this is how is it read in the API)
        // 날짜데이터 형식화 (이것이 api에서 읽는 방법)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print the current date and time
        // 현재 날짜를 지정한 형식으로 형식화
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // convert the weather code to something more readable
    // 날씨 코드를 우리가 읽기 쉬운 형식으로 변환
    private static String convertWeatherCode(long weathercode) {
        String weatherCondition = "";
        if (weathercode == 0L) {
            // clear
            weatherCondition = "Clear";
        } else if (weathercode > 0L && weathercode <= 3L) {
            // cloudy
            weatherCondition = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L)
        || (weathercode >= 80L && weathercode <= 99L)) {
            // rain
            weatherCondition = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            // snow
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }


}
