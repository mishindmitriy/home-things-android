#include <ESP8266WiFi.h>
#include <BME280I2C.h>
#include <Wire.h>

BME280I2C bme;    // Default : forced mode, standby time = 1000 ms
                  // Oversampling = pressure ×1, temperature ×1, humidity ×1, filter off,

const char* ssid     = "MURKA"; // Your ssid
const char* password = "awpl12345678"; // Your Password

WiFiServer server(80);

void setup() {
Serial.begin(115200);
while(!Serial) {} // Wait
Wire.begin(D3,D4);
Serial.println();

// Connect to WiFi network
WiFi.mode(WIFI_STA);
Serial.println();
Serial.println();
Serial.print("Connecting to ");
Serial.println(ssid);

WiFi.begin(ssid, password);

while (WiFi.status() != WL_CONNECTED) {
delay(500);
Serial.print(".");
}
Serial.println("");
Serial.println("WiFi connected");

// Start the server
server.begin();
Serial.println("Server started");

// Print the IP address
Serial.println(WiFi.localIP());

while(!bme.begin())  {
    Serial.println("Could not find BME280 sensor!");
    delay(1000);
  }
}

void loop() {
  float temp(NAN), hum(NAN), pres(NAN);
   BME280::TempUnit tempUnit(BME280::TempUnit_Celcius);
   BME280::PresUnit presUnit(BME280::PresUnit_Pa);
   bme.read(pres, temp, hum, tempUnit, presUnit);

  // Check if any reads failed and exit early (to try again).
  if (isnan(hum) || isnan(temp)|| isnan(temp)) {
    Serial.println("Failed to read from sensor!");
  } else {
    Serial.print("temperature:");
    Serial.print(temp);
    Serial.print("; humidity:");
    Serial.print(hum);
    Serial.print("; pressure:");
    Serial.print(pres);
    Serial.println();
  }

WiFiClient client = server.available();
client.println("HTTP/1.1 200 OK");
client.println("Content-Type: text/html");
client.println("Connection: close");  // the connection will be closed after completion of the response
client.println();
client.print("{\"humidity\":");
client.println((float)hum, 2);
client.print(", \"temperature\":");
client.print((float)temp, 2);
client.print(", \"pressure\":");
client.print((float)pres, 2);
client.println("}");
delay(1000); //delay for reread
}
