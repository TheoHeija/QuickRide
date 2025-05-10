package com.quickride.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.quickride.model.Location;
import com.quickride.model.Taxi;

import javafx.application.Platform;

/**
 * Class to animate taxis in real-time on the map
 */
public class TaxiAnimator {
    private static final Logger LOGGER = Logger.getLogger(TaxiAnimator.class.getName());
    
    // Swiss cities coordinates (Zurich, Geneva, Bern, Basel, Lausanne)
    private static final double[][] SWISS_CITIES = {
        {47.3769, 8.5417},   // 0: Zurich
        {46.2044, 6.1432},   // 1: Geneva
        {46.9480, 7.4474},   // 2: Bern
        {47.5596, 7.5886},   // 3: Basel
        {46.5197, 6.6323},   // 4: Lausanne
        {47.0502, 8.3093},   // 5: Lucerne
        {46.2018, 6.8785},   // 6: Montreux
        {46.3834, 6.2347},   // 7: Nyon
        {47.4245, 9.3767},   // 8: St. Gallen
        {46.6864, 7.8632},   // 9: Interlaken
    };
    
    // Route waypoints to avoid lakes and mountains - each defined as a series of points along realistic roads
    private static final Map<String, double[][]> ROUTE_WAYPOINTS = new HashMap<>();
    
    static {
        // Initialize realistic road waypoints for common routes
        
        // Zurich to Bern (0->2) - avoiding lakes
        ROUTE_WAYPOINTS.put("0-2", new double[][] {
            {47.3769, 8.5417},   // Zurich
            {47.2931, 8.4424},   // Near Thalwil
            {47.1814, 8.4638},   // Zug
            {47.0784, 8.3065},   // Near Lucerne
            {46.8987, 7.9868},   // Near Thun
            {46.9480, 7.4474}    // Bern
        });
        
        // Bern to Geneva (2->1) - following A1
        ROUTE_WAYPOINTS.put("2-1", new double[][] {
            {46.9480, 7.4474},   // Bern
            {46.8030, 7.1511},   // Fribourg
            {46.5180, 6.6326},   // Lausanne
            {46.3834, 6.2347},   // Nyon
            {46.2044, 6.1432}    // Geneva
        });
        
        // Zurich to Basel (0->3) - following A3/A1
        ROUTE_WAYPOINTS.put("0-3", new double[][] {
            {47.3769, 8.5417},   // Zurich
            {47.4789, 8.3093},   // Baden
            {47.5128, 7.9877},   // Near Rheinfelden
            {47.5596, 7.5886}    // Basel
        });
        
        // Bern to Lausanne (2->4) - following A1
        ROUTE_WAYPOINTS.put("2-4", new double[][] {
            {46.9480, 7.4474},   // Bern
            {46.8030, 7.1511},   // Fribourg
            {46.6784, 6.9002},   // Near Vevey
            {46.5197, 6.6323}    // Lausanne
        });
        
        // Geneva to Lausanne (1->4) - following lake shore
        ROUTE_WAYPOINTS.put("1-4", new double[][] {
            {46.2044, 6.1432},   // Geneva
            {46.3080, 6.2280},   // Versoix
            {46.3834, 6.2347},   // Nyon
            {46.4670, 6.3410},   // Rolle
            {46.5197, 6.6323}    // Lausanne
        });
        
        // Zurich to Lucerne (0->5) - avoiding lake obstacles
        ROUTE_WAYPOINTS.put("0-5", new double[][] {
            {47.3769, 8.5417},   // Zurich
            {47.2780, 8.5220},   // Thalwil
            {47.1814, 8.4638},   // Zug
            {47.0502, 8.3093}    // Lucerne
        });
        
        // Lausanne to Montreux (4->6) - following lake shore
        ROUTE_WAYPOINTS.put("4-6", new double[][] {
            {46.5197, 6.6323},   // Lausanne
            {46.4560, 6.8510},   // Vevey
            {46.2018, 6.8785}    // Montreux
        });
        
        // Geneva to Nyon (1->7) - straight route, but following the lake
        ROUTE_WAYPOINTS.put("1-7", new double[][] {
            {46.2044, 6.1432},   // Geneva
            {46.2910, 6.1700},   // Versoix
            {46.3834, 6.2347}    // Nyon
        });
        
        // Zurich to St. Gallen (0->8) - following A1
        ROUTE_WAYPOINTS.put("0-8", new double[][] {
            {47.3769, 8.5417},   // Zurich
            {47.4500, 8.8500},   // Winterthur
            {47.4800, 9.0500},   // Wil
            {47.4245, 9.3767}    // St. Gallen
        });
        
        // Bern to Interlaken (2->9) - following realistic roads
        ROUTE_WAYPOINTS.put("2-9", new double[][] {
            {46.9480, 7.4474},   // Bern
            {46.7550, 7.6303},   // Thun
            {46.6864, 7.8632}    // Interlaken
        });
    }
    
    // Major routes between cities - pairs of indices from SWISS_CITIES
    private static final int[][] ROUTES = {
        {0, 2}, // Zurich - Bern
        {2, 1}, // Bern - Geneva
        {0, 3}, // Zurich - Basel
        {2, 4}, // Bern - Lausanne
        {1, 4}, // Geneva - Lausanne
        {0, 5}, // Zurich - Lucerne
        {4, 6}, // Lausanne - Montreux
        {1, 7}, // Geneva - Nyon
        {0, 8}, // Zurich - St. Gallen
        {2, 9}  // Bern - Interlaken
    };
    
    private final List<Taxi> taxis;
    private final RealMapViewer mapViewer;
    private final Random random = new Random();
    private ScheduledExecutorService animationExecutor;
    private boolean isAnimating = false;
    
    // Store current taxi animations
    private final Map<String, TaxiAnimation> taxiAnimations = new HashMap<>();
    
    /**
     * Creates a new TaxiAnimator
     * @param taxis List of taxis to animate
     * @param mapViewer Map viewer to update
     */
    public TaxiAnimator(List<Taxi> taxis, RealMapViewer mapViewer) {
        this.taxis = new ArrayList<>(taxis);
        this.mapViewer = mapViewer;
    }
    
    /**
     * Start animating taxis
     */
    public void startAnimation() {
        if (isAnimating) {
            return;
        }
        
        isAnimating = true;
        
        // Initialize animations for each taxi
        for (Taxi taxi : taxis) {
            initializeRandomRoute(taxi);
        }
        
        // Create scheduled executor for animation
        animationExecutor = Executors.newSingleThreadScheduledExecutor();
        // Slow down the animation update interval to 2000ms (2 seconds) for smoother, slower movement
        animationExecutor.scheduleAtFixedRate(this::updateAnimations, 0, 2000, TimeUnit.MILLISECONDS);
        
        LOGGER.info(() -> "Started taxi animation for " + taxis.size() + " taxis");
    }
    
    /**
     * Stop animating taxis
     */
    public void stopAnimation() {
        if (!isAnimating) {
            return;
        }
        
        isAnimating = false;
        
        if (animationExecutor != null) {
            animationExecutor.shutdown();
            try {
                if (!animationExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    animationExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                animationExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        LOGGER.info("Stopped taxi animation");
    }
    
    /**
     * Initialize a random route for a taxi
     */
    private void initializeRandomRoute(Taxi taxi) {
        // Select a random route
        int routeIndex = random.nextInt(ROUTES.length);
        int[] route = ROUTES[routeIndex];
        
        // Get the waypoints for this route
        String routeKey = route[0] + "-" + route[1];
        double[][] waypoints = ROUTE_WAYPOINTS.get(routeKey);
        
        // If no waypoints defined, fall back to simple start/end
        if (waypoints == null) {
            double[] start = SWISS_CITIES[route[0]];
            double[] end = SWISS_CITIES[route[1]];
            
            waypoints = new double[][] {
                start,
                end
            };
        }
        
        // Create taxi animation with the waypoints
        TaxiAnimation animation = new TaxiAnimation(
            taxi.getId(),
            waypoints,
            // Make steps significantly higher (much slower animation) - about 15-20 minutes for full route
            1500 + random.nextInt(500)
        );
        
        // Update taxi location to start
        taxi.setCurrentLocation(new Location(
            waypoints[0][0], waypoints[0][1],
            getSwissCityName(route[0]) + ", Switzerland"
        ));
        
        // Store animation
        taxiAnimations.put(taxi.getId(), animation);
    }
    
    /**
     * Get a Swiss city name by index
     */
    private String getSwissCityName(int index) {
        return switch (index) {
            case 0 -> "Zurich";
            case 1 -> "Geneva";
            case 2 -> "Bern";
            case 3 -> "Basel";
            case 4 -> "Lausanne";
            case 5 -> "Lucerne";
            case 6 -> "Montreux";
            case 7 -> "Nyon";
            case 8 -> "St. Gallen";
            case 9 -> "Interlaken";
            default -> "Switzerland";
        };
    }
    
    /**
     * Update all taxi animations
     */
    private void updateAnimations() {
        if (!isAnimating) {
            return;
        }
        
        boolean taxisUpdated = false;
        
        for (Taxi taxi : taxis) {
            TaxiAnimation animation = taxiAnimations.get(taxi.getId());
            if (animation != null && animation.update()) {
                // Update taxi position
                double[] position = animation.getCurrentPosition();
                taxi.getCurrentLocation().setLatitude(position[0]);
                taxi.getCurrentLocation().setLongitude(position[1]);
                
                taxisUpdated = true;
                
                // If animation completed, start a new one
                if (animation.isComplete()) {
                    initializeRandomRoute(taxi);
                }
            }
        }
        
        // Only update the map if any taxi position changed
        if (taxisUpdated) {
            Platform.runLater(() -> mapViewer.updateTaxis(taxis));
        }
    }
    
    /**
     * Add a taxi to animate
     */
    public void addTaxi(Taxi taxi) {
        if (!taxis.contains(taxi)) {
            taxis.add(taxi);
            if (isAnimating) {
                initializeRandomRoute(taxi);
            }
        }
    }
    
    /**
     * Remove a taxi from animation
     */
    public void removeTaxi(Taxi taxi) {
        taxis.remove(taxi);
        taxiAnimations.remove(taxi.getId());
    }
    
    /**
     * Class to represent a single taxi animation along a route with multiple waypoints
     */
    private class TaxiAnimation {
        private final double[][] waypoints;
        private final int totalSteps;
        private int currentStep;
        private int currentSegment;
        private final int[] segmentSteps;
        
        /**
         * Create a new taxi animation
         * @param taxiId Taxi identifier (unused)
         * @param waypoints Array of waypoints [lat, lon]
         * @param totalSteps Total steps for the entire route
         */
        public TaxiAnimation(String taxiId, double[][] waypoints, int totalSteps) {
            // Don't store taxiId as it's not used
            this.waypoints = waypoints;
            this.totalSteps = totalSteps;
            this.currentStep = 0;
            this.currentSegment = 0;
            
            // Calculate steps for each segment based on segment distance
            double totalDistance = 0;
            double[] segmentDistances = new double[waypoints.length - 1];
            
            // First calculate the total distance and each segment's distance
            for (int i = 0; i < waypoints.length - 1; i++) {
                double[] start = waypoints[i];
                double[] end = waypoints[i + 1];
                
                // Calculate distance using Haversine formula
                double distance = calculateDistance(start[0], start[1], end[0], end[1]);
                segmentDistances[i] = distance;
                totalDistance += distance;
            }
            
            // Distribute steps proportionally to segment distance
            this.segmentSteps = new int[waypoints.length - 1];
            int remainingSteps = totalSteps;
            
            for (int i = 0; i < waypoints.length - 1; i++) {
                // For the last segment, use all remaining steps to avoid rounding errors
                if (i == waypoints.length - 2) {
                    segmentSteps[i] = remainingSteps;
                } else {
                    // Distribute steps based on distance percentage
                    segmentSteps[i] = (int) Math.round((segmentDistances[i] / totalDistance) * totalSteps);
                    remainingSteps -= segmentSteps[i];
                }
            }
        }
        
        /**
         * Calculate distance between two coordinates using Haversine formula
         */
        private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371; // Earth radius in kilometers
            
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                     + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                     * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            
            return R * c;
        }
        
        /**
         * Update the animation (move one step forward)
         * @return true if position changed
         */
        public boolean update() {
            if (currentStep >= totalSteps) {
                return false;
            }
            
            // Move only on certain frames for even smoother, slower animation
            // Only increment on every other update
            if (random.nextInt(3) > 0) {
                currentStep++;
            }
            
            // Check if we need to move to the next segment
            int stepsTaken = 0;
            for (int i = 0; i <= currentSegment; i++) {
                stepsTaken += segmentSteps[i];
                if (currentStep <= stepsTaken) {
                    // We're still in the current segment or just moved to a new one
                    break;
                }
                // Move to next segment if we've exceeded its steps
                if (i == currentSegment) {
                    currentSegment++;
                }
            }
            
            return true;
        }
        
        /**
         * Get the current position of the taxi
         * @return array with [lat, lon]
         */
        public double[] getCurrentPosition() {
            // Get the current segment
            double[] start = waypoints[currentSegment];
            double[] end = waypoints[currentSegment + 1];
            
            // Calculate the progress within the current segment
            int segmentStartStep = 0;
            for (int i = 0; i < currentSegment; i++) {
                segmentStartStep += segmentSteps[i];
            }
            
            int stepsInSegment = currentStep - segmentStartStep;
            double segmentProgress = (double) stepsInSegment / segmentSteps[currentSegment];
            
            // Calculate the position
            double lat = start[0] + (end[0] - start[0]) * segmentProgress;
            double lon = start[1] + (end[1] - start[1]) * segmentProgress;
            
            // Add extremely tiny random variations to make movement look more natural but not jumpy
            double jitter = 0.00005; // Further reduced jitter for even more realistic movement
            lat += (random.nextDouble() * jitter * 2) - jitter;
            lon += (random.nextDouble() * jitter * 2) - jitter;
            
            return new double[] {lat, lon};
        }
        
        /**
         * Check if animation is complete
         */
        public boolean isComplete() {
            return currentStep >= totalSteps;
        }
    }
} 