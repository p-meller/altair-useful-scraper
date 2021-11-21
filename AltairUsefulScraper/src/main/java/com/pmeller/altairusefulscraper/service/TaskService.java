package com.pmeller.altairusefulscraper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskService {

    @Autowired
    ScrapperService scrapperService;

    @Scheduled(cron = "${cron.olx.expression}", zone = "UTC")
    void scrapOlxTask() {
        scrapperService.scrapOlx();
    }

    @Scheduled(cron = "${cron.otodom.expression}", zone = "UTC")
    void scrapOtodomTask() {
        scrapperService.scrapOtodom();
    }


    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    void test(){
        scrapOtodomTask();
    }

}
