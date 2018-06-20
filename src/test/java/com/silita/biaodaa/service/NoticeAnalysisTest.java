package com.silita.biaodaa.service;

import com.silita.biaodaa.disruptor.DisruptorOperator;
import com.silita.biaodaa.task.AnalysisTask;
import com.snatch.model.EsNotice;
import com.snatch.model.Notice;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangxiahui on 18/3/20.
 */
public class NoticeAnalysisTest extends ConfigTest  {


    @Autowired
    TestService testService;

    @Autowired
    DisruptorOperator disruptorOperator;

    @Autowired
    AnalysisTask analysisTask;

    @Autowired
    @Qualifier("jedisTemplate")
    RedisTemplate redisTemplate;


    /**
     *加载自定义表中的解析任务
     *支持大批量数据，分批导入
     */
    @Test
    public void pushCustomRedisNotice() {
        testService.pushCustomRedisNotice("test.hunan","洪江市德坤矿业贸易有限公司污染场地修复EPC项目招标公告");
        analyzeHandler();
    }


    /**
     *加载test.notice中的解析任务
     *不支持大批量数据
     */
    @Test
    public void pushRedisNotice(){
        testService.pushRedisNotice();
    }

    /**
     *加载test.hunan_notice中的解析任务
     *支持大批量数据，分批导入
     */
    @Test
    public void pushHunanRedisNotice() {
        testService.pushHunanRedisNotice();
    }


    /**
     * 测试解析任务
     */
    @Test
    public void analyzeHandler(){
        disruptorOperator.start();
        Notice notice = null ;

        while (true){
            try {
                try{
                    notice = (Notice) redisTemplate.opsForList().leftPop("liuqi",0, TimeUnit.SECONDS);
                    EsNotice esNotice = analysisTask.noticeToEsNotice(notice);
                    disruptorOperator.publish(esNotice);
                }finally{
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}