package com.example.chatsales.scheduled;

import com.example.chatsales.service.DataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true", matchIfMissing = false)
public class DataSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncScheduler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired(required = false)
    private DataSyncService dataSyncService;

    /**
     * 定时执行数据同步任务，每30分钟执行一次
     * cron表达式：秒 分 时 日 月 周
     * 0 0/30 * * * ? 表示每30分钟执行一次，从每小时的0分开始
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduledDataSync() {
        if (dataSyncService == null) {
            logger.warn("数据同步服务未配置，跳过定时同步任务");
            return;
        }

        String startTime = LocalDateTime.now().format(formatter);
        logger.info("开始执行定时数据同步任务 - {}", startTime);

        try {
            dataSyncService.syncAllData();

            logger.info("定时数据同步任务执行完成 - 开始时间: {}, 结束时间: {}",
                    startTime, LocalDateTime.now().format(formatter));
        } catch (Exception e) {
            logger.error("定时数据同步任务执行异常", e);
        }
    }
} 