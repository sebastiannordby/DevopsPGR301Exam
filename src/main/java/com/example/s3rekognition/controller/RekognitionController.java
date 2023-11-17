package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = 
        Logger.getLogger(RekognitionController.class.getName());

    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.s3Client = AmazonS3ClientBuilder
            .standard()
            .build();
        this.rekognitionClient = AmazonRekognitionClientBuilder
            .standard()
            .build();
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument and returns the content
     * of the given bucket.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(
        value = "/list-images",
        produces = "application/json")
    @ResponseBody
    public List<String> listImages(
        @RequestParam String bucketName
    ) {
        logger.info("Listing contents of bucketName=" + bucketName);
        var timer = meterRegistry.timer("s3.list.images.timer");

        // Metrics: Record time used to execute the listing of content in bucket.
        return timer.record(() -> {
            var objects = s3Client
                .listObjects(bucketName)
                .getObjectSummaries();

            return objects
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
        });
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument and an image from the bucket,
     * then returns the content image.
     * <p>
     *
     * @param bucketName
     * @param imageName
     * @return
     */
    @GetMapping(value = "/download-image")
    public ResponseEntity<StreamingResponseBody> downloadImage(
        @RequestParam String bucketName,
        @RequestParam String imageName
    ) throws IOException {
        logger.info(
        "downloadImage bucketName=" + bucketName +
            " imageName= " + imageName);

        var mimeType = getMimeType(imageName);
        var distributionSummary = meterRegistry
            .summary("s3.download.image.size");

        var s3Object = s3Client
            .getObject(bucketName, imageName);

        // Metrics: Register the size of the file.
        distributionSummary.record(s3Object
            .getObjectMetadata()
            .getContentLength());

        var content = s3Object
            .getObjectContent()
            .readAllBytes();

        logger.info(
            "bucketName=" + bucketName +
            " imageName= " + imageName +
            " mineType= " + mimeType +
            " size=" + content.length);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream inputStream = s3Object.getObjectContent()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        };

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mimeType))
            .body(stream);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(
        value = "/scan-ppe",
        consumes = "*/*",
        produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForPPE(
        @RequestParam String bucketName
    ) {
        // Metrics: Count how many times this endpoint is called.
        meterRegistry.counter("scan_ppe").increment();

        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                .withImage(new Image()
                    .withS3Object(new S3Object()
                            .withBucket(bucketName)
                            .withName(image.getKey()))
                )
                .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                    .withMinConfidence(80f)
                    .withRequiredEquipmentTypes("FACE_COVER")
                );

            DetectProtectiveEquipmentResult result = rekognitionClient
                .detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);

            logger.info(
            "scanning " + image.getKey() +
                ", violation result " + violation);

            // Categorize the current image as a violation or not.
            PPEClassificationResponse classification = new PPEClassificationResponse(
                image.getKey(),
                result.getPersons().size(),
                violation);

            classificationResponses.add(classification);
        }

        return ResponseEntity.ok(
            new PPEResponse(bucketName, classificationResponses));
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("FACE")
                        && bodyPart.getEquipmentDetections().isEmpty());
    }


    /**
     * Takes a fileName and returns the MimeType
     * corresponding to the extension of the fileName.
     *
     * jpg | jpeg = "image/jpeg"
     * png = "image/png"
     * gif = "image/gif"
     * */
    private String getMimeType(String fileName) {
        var extension = fileName
            .substring(fileName.lastIndexOf(".") + 1)
            .toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

    }
}
