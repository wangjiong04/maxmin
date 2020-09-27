package com.maxmin.tda.clients;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangjun on 2020/9/25.
 */
@Component
@Data
public class AwsClient {

    @Value("${access_key}")
    private String accessKey;

    @Value("${secret_key}")
    private String secretKey;

    private AmazonS3 s3client;
    private final String bucketName="trade-users";

    public AwsClient() {

    }

    public void uploadFile(String fileName, File file){
        s3client.putObject(
                bucketName,
                fileName,
                file
        );
    }

    public List<String> getFiles(){
        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey, secretKey
        );
        s3client  = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        ObjectListing objectListing =  s3client.listObjects(bucketName);
        return objectListing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    public S3Object getFileByName(String fileName){
        return s3client.getObject(bucketName, fileName);
    }
}
