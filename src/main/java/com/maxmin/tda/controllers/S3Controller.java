package com.maxmin.tda.controllers;

import com.maxmin.tda.clients.AwsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by wangjun on 2020/9/25.
 */
@RestController
public class S3Controller {
    private AwsClient awsClient;

    @Autowired
    public S3Controller(AwsClient awsClient) {
        this.awsClient = awsClient;
    }

    @GetMapping(value = "/files",produces = "application/json")
    public List<String> getFiles(){
        return awsClient.getFiles();
    }
}
