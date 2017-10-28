#include <ESP8266WiFi.h>
#include "DHT.h"

#define DHTPIN 4     // what digital pin we're connected to

// Uncomment whatever type you're using!
#define DHTTYPE DHT11   // DHT 11

const char* ssid     = "ssid"; // Your ssid
const char* password = "password"; // Your Password

int pin = 2;

WiFiServer server(80);
DHT dht(DHTPIN, DHTTYPE);

void setup() {
    Serial.begin(115200);
    delay(10);
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
}

void loop() {
    float humi = dht.readHumidity();
    // Read temperature as Celsius (the default)
    float temp = dht.readTemperature();

    // Check if any reads failed and exit early (to try again).
    if (isnan(humi) || isnan(temp)) {
        Serial.println("Failed to read from DHT sensor!");
    } else {
        Serial.print("temperature:");
        Serial.print(temp);
        Serial.print(" humidity:");
        Serial.print(humi);
        Serial.println();
    }

    WiFiClient client = server.available();
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println("Connection: close");  // the connection will be closed after completion of the response
    client.println();
    client.print("{\"humidity\":");
    client.println((float)humi, 2);
    client.print(", \"temperature\":");
    client.print((float)temp, 2);
    client.println("}");
    delay(2000); //delay for reread
}
