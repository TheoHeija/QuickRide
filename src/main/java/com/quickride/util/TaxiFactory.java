package com.quickride.util;

import java.util.Random;

import com.quickride.model.Location;
import com.quickride.model.Taxi;

/**
 * Factory class to create sample taxis
 */
public class TaxiFactory {
    
    private static final Random random = new Random();
    
    // Swiss area coordinates (around major cities)
    private static final double[][] SWISS_CITIES = {
        {47.3769, 8.5417},   // Zurich
        {46.2044, 6.1432},   // Geneva
        {46.9480, 7.4474},   // Bern
        {47.5596, 7.5886},   // Basel
        {46.5197, 6.6323},   // Lausanne
        {47.0502, 8.3093},   // Lucerne
        {46.2018, 6.8785},   // Montreux
        {46.3834, 6.2347},   // Nyon
        {47.4245, 9.3767},   // St. Gallen
        {46.6864, 7.8632}    // Interlaken
    };
    
    private static final String[] SWISS_CITY_NAMES = {
        "Zurich", "Geneva", "Bern", "Basel", "Lausanne",
        "Lucerne", "Montreux", "Nyon", "St. Gallen", "Interlaken"
    };

    private static final String[] DRIVER_NAMES = {
        "Thomas Müller", "Anna Schmidt", "Lukas Weber", "Sarah Keller", "Michael Brunner",
        "Sophie Meier", "Daniel Fischer", "Emma Schneider", "Nicolas Huber", "Laura Zimmermann"
    };

    private static final String[] CAR_MODELS = {
        "Mercedes E-Class", "BMW 5 Series", "Tesla Model 3", "Volvo V60", "Audi A6",
        "VW Passat", "Skoda Octavia", "Renault Megane", "Toyota Prius", "Hyundai Ioniq"
    };

    private static final String[] STREET_NAMES = {
        "Bahnhofstrasse", "Hauptstrasse", "Seestrasse", "Dorfstrasse", "Bergstrasse",
        "Rosenweg", "Kirchgasse", "Schulstrasse", "Gartenweg", "Seeblick",
        "Industriestrasse", "Marktgasse", "Mühleweg", "Birkenweg", "Lindenstrasse"
    };
    
    /**
     * Private constructor to prevent instantiation
     */
    private TaxiFactory() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Create random taxis for the demo
     * @param count number of taxis to create
     * @return array of taxis
     */
    public static Taxi[] createRandomTaxis(int count) {
        Taxi[] taxis = new Taxi[count];
        
        for (int i = 0; i < count; i++) {
            // Get random Swiss city
            int cityIndex = random.nextInt(SWISS_CITIES.length);
            double[] cityCoords = SWISS_CITIES[cityIndex];
            String cityName = SWISS_CITY_NAMES[cityIndex];
            
            // Generate coordinates with small random offset from city center
            double latOffset = (random.nextDouble() * 0.04) - 0.02; // +/- 0.02 degrees (about 2 km)
            double lonOffset = (random.nextDouble() * 0.04) - 0.02;
            double latitude = cityCoords[0] + latOffset;
            double longitude = cityCoords[1] + lonOffset;
            
            // Generate address
            String streetName = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
            String streetNumber = String.valueOf(1 + random.nextInt(100));
            String address = streetNumber + " " + streetName + ", " + cityName + ", Switzerland";
            
            // Create location
            Location location = new Location(latitude, longitude, address);
            
            // Create taxi
            String driverName = DRIVER_NAMES[random.nextInt(DRIVER_NAMES.length)];
            String carModel = CAR_MODELS[random.nextInt(CAR_MODELS.length)];
            String licensePlate = generateSwissLicensePlate(cityName);
            
            taxis[i] = new Taxi(driverName, licensePlate, carModel, location);
        }
        
        return taxis;
    }
    
    /**
     * Generate a realistic Swiss license plate
     */
    private static String generateSwissLicensePlate(String cityName) {
        // Map city to canton abbreviation
        String canton = switch (cityName) {
            case "Zurich" -> "ZH";
            case "Geneva" -> "GE";
            case "Bern" -> "BE";
            case "Basel" -> "BS";
            case "Lausanne" -> "VD";
            case "Lucerne" -> "LU";
            case "Montreux" -> "VD";
            case "Nyon" -> "VD";
            case "St. Gallen" -> "SG";
            case "Interlaken" -> "BE";
            default -> "ZH";
        };
        
        // Generate random number
        int number = 1000 + random.nextInt(9000);
        
        // Format as Swiss license plate: "ZH 1234"
        return canton + " " + number;
    }
} 