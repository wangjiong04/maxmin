package com.maxmin.tda.service;

import com.maxmin.tda.clients.AwsClient;
import com.maxmin.tda.clients.TdaClient;
import com.maxmin.tda.dto.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by wangjun on 2020/10/3.
 */
@Service
@Slf4j
public class Scheduler {
    @Autowired
    private TdaClient tdaClient;

    @Autowired
    private AwsClient awsClient;

    @Scheduled(cron = "0 0 12 ? * SUN")
    public void refreshToken() {
        log.info("refresh token every week.");
        List<String> accounts = awsClient.getFiles();
        for (String accountId : accounts
        ) {
            try {
                List<String[]> csv = awsClient.getCSV(accountId);
                Token token = tdaClient.csvToToken(csv);
                token = tdaClient.getTokenByRefreshToken(token);
                csv = tdaClient.tokenToCSV(token);
                awsClient.writeCSV(csv, accountId);
            } catch (IOException ex) {
                log.error("get csv failed", ex);
            } catch (Exception ex) {
                log.error("refresh token error", ex);
            }
        }
    }
}
