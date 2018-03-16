package net.cognitics.navapp;

/**
 * Holds the entire scene to render, all geometry in UTM coordinates.
 * Special treatment for routes, where the route already traversed is not rendered, *
 */

public class Scene {
    private FeatureManager featureManager;

    public Scene(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }
}
