package com.maxmin.tda.clients;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.opencsv.exceptions.CsvException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangjun on 2020/9/25.
 */
@Component
@Data
@Slf4j
public class AwsClient {

    @Value("${access_key}")
    private String accessKey;

    @Value("${secret_key}")
    private String secretKey;

    private AmazonS3 s3client;
    private final String bucketName = "trade-users";

    @PostConstruct
    public void init() {
//        AWSCredentials credentials = new BasicAWSCredentials(
//                accessKey, secretKey
//        );
//
//        s3client = AmazonS3ClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withRegion(Regions.US_EAST_1)
//                .build();
    }

    private void uploadFile(String fileName, InputStream inputStream) {
//        s3client.putObject(
//                bucketName,
//                fileName,
//                inputStream, new ObjectMetadata()
//        );
    }

    @Cacheable(value = "files", condition = "#result != null")
    public List<String> getFiles() {
//        log.info("get files from s3");
//        ObjectListing objectListing = s3client.listObjects(bucketName);
//        return objectListing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @CachePut(value = "files")
    public void refreshFiles(List<String> files) {
        log.info("put files into s3");
    }

    private S3ObjectInputStream getFileByName(String fileName) {
        S3Object s3Object = s3client.getObject(bucketName, fileName);
        return s3Object.getObjectContent();
    }

    @Cacheable(key = "#fileName", value = "csv", condition = "#result != null")
    public List<String[]> getCSV(String fileName) throws IOException, CsvException {
//        log.info("get csv from s3 for: " + fileName);
//        S3ObjectInputStream inputStream = getFileByName(fileName);
//        InputStreamReader reader = new InputStreamReader(inputStream);
//        CSVReader csvReader = new CSVReader(reader);
//        List<String[]> list = csvReader.readAll();
//        reader.close();
//        csvReader.close();
//        return list;
        return Collections.emptyList();
    }

    @CachePut(key = "#fileName", value = "csv")
    public void writeCSV(List<String[]> in, String fileName) throws IOException {
//        log.info("put csv file into s3 for: " + fileName);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
//        CSVWriter writer = new CSVWriter(streamWriter);
//        writer.writeAll(in);
//        writer.close();
//        this.uploadFile(fileName, new ByteArrayInputStream(stream.toByteArray()));
    }
}
