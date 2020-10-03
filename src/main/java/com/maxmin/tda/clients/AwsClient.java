package com.maxmin.tda.clients;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final String bucketName="trade-users";

    @PostConstruct
    public void init(){
        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey, secretKey
        );

        s3client  = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    private void uploadFile(String fileName, InputStream inputStream){
        s3client.putObject(
                bucketName,
                fileName,
                inputStream,new ObjectMetadata()
        );
    }

    @Cacheable(value = "files")
    public List<String> getFiles(){
        ObjectListing objectListing =  s3client.listObjects(bucketName);
        return objectListing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    @CachePut(value = "files")
    public void refreshFiles(List<String> files){
    }

    private S3ObjectInputStream getFileByName(String fileName){
        S3Object s3Object= s3client.getObject(bucketName, fileName);
        return s3Object.getObjectContent();
    }
    @Cacheable(key = "#fileName",value = "csv")
    public List<String[]> getCSV(String fileName) throws IOException,CsvException{
        S3ObjectInputStream inputStream=getFileByName(fileName);
        InputStreamReader reader=new InputStreamReader(inputStream);
        CSVReader csvReader=new CSVReader(reader);
        List<String[]> list = csvReader.readAll();
        reader.close();
        csvReader.close();
        return list;
    }

    @CachePut(key = "#fileName",value = "csv")
    public void writeCSV(List<String[]> in, String fileName) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        CSVWriter writer= new CSVWriter(streamWriter);
        writer.writeAll(in);
        writer.close();
        this.uploadFile(fileName, new ByteArrayInputStream(stream.toByteArray()));
    }
}
