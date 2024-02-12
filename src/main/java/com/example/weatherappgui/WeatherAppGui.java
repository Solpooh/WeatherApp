package com.example.weatherappgui;

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    public WeatherAppGui() {
        // set up our gui and add a title
        // GUI 제목 추가
        super("Weather App");

        // configure gui to end the program's process once it has been closed
        // 프로그램이 닫히면 프로그램 프로세스 종료
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of our gui (in pixels)
        // GUI 사아즈 설정
        setSize(450, 650);

        // load our gui at the center of the screen
        // 화면 중앙에 GUI 위치
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within the gui
        // 구성요소를 수동으로 배치
        setLayout(null);

        // prevent any resize of our gui
        // 크기 조정 방지
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        // search field
        // 검색 필드
        JTextField searchTextField = new JTextField();

        // set the location and size of our component
        // 검색 필드 위치/크기 지정
        searchTextField.setBounds(15, 15, 351, 45);

        // change the font style and size
        // 폰트 스타일/크기 변경
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // weather image
        // 받아온 날씨이미지
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // temperature text
        // 기온
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // center the text
        // text 중앙에 위치
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // weather condition description
        // 초기화면 날씨
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // humidity image
        // 습도 이미지
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // humidity text
        // 습도 텍스트
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // windspeed image
        // 바람 이미지
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // windspeed text
        // 바람 텍스트
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // search button
        // 검색 버튼
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // change the cursor to a hand cursor when hovering over this button
        // 커서 hover css 적용
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get location from user
                // 사용자로부터 위치 정보 얻기
                String userInput = searchTextField.getText();

                // validate input - remove whitespace to ensure non-empty text
                // 입력 유효성 검증 - 비어있지 않은 text를 보장하기 위해 공백 제거
                if(userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                // retrieve weather data
                // 날씨 데이터 검색
                weatherData = WeatherApp.getWeatherData(userInput);

                // update gui

                // update weather image
                // 날씨 이미지 update
                String weatherCondition = (String) weatherData.get("weather_condition");

                // depending on the condition, we will update the weather image that corresponds with the condition
                // 조건에 맞는 날씨 이미지 업데이트
                switch (weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                // update temperature text
                // 기온 업데이트
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // update weather condition text
                // 날씨 조건 업데이트
                weatherConditionDesc.setText(weatherCondition);

                // update humidity text
                // 습도 업데이트
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                // update windspeed text
                // 풍속 업데이트
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    // used to create images in our gui components
    // gui 구성요소에서 이미지 만들기
    private ImageIcon loadImage(String resourcePath) {
        try {
            // read the image file from the path given
            // 주어진 경로에서 이미지 파일 읽기
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // returns an image icon so that our component can render it
            // 구성 요소가 렌더링할 수 있도록 이미지 아이콘 반환
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}
