package com.example.s3rekognition.results;

import java.util.List;

public class ImageAnalysisResponse {
    private String imageName;
    private List<String> detectedLabels;

    public ImageAnalysisResponse(
        String imageName,
        List<String> detectedLabels) {
        this.imageName = imageName;
        this.detectedLabels = detectedLabels;
    }

    public String getImageName() {
        return imageName;
    }

    public List<String> getDetectedLabels() {
        return detectedLabels;
    }
}
