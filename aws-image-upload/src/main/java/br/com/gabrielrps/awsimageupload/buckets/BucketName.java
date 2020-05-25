package br.com.gabrielrps.awsimageupload.buckets;

import com.amazonaws.services.s3.model.Bucket;

public enum BucketName {

    PROFILE_IMAGE("gabrielrps-image-upload");

    private final String bucketName;

    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
