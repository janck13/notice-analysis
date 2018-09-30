package com.silita.biaodaa.controller;

import com.google.common.collect.ImmutableMap;
import com.silita.biaodaa.common.Constant;
import com.silita.biaodaa.disruptor.DisruptorOperator;
import com.silita.biaodaa.model.TUser;
import com.silita.biaodaa.service.CommonService;
import com.silita.biaodaa.service.TestService;
import com.silita.biaodaa.task.AnalysisTask;
import com.snatch.model.EsNotice;
import com.snatch.model.Notice;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by zhangxiahui on 17/5/26.
 * 提供测试导数等入口
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testService;

    @Autowired
    DisruptorOperator disruptorOperator;

    @Autowired
    @Qualifier("jedisTemplate")
    RedisTemplate redisTemplate;

    @Autowired
    AnalysisTask analysisTask;

    @Autowired
    CommonService commonService;

    private Lock lock = new ReentrantLock();//基于底层IO阻塞考虑

    private static final Logger logger = Logger.getLogger(TestController.class);



    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> getTestID(@PathVariable String id) {
        TUser user = testService.getTestName(id);
        if(user==null){
            user = new TUser();
        }
        return new ImmutableMap.Builder<String, Object>().put("status", 1)
                .put("msg", "成功").put("data",user).build();
    }

    private void runAnalysis(int count){
//        disruptorOperator.start();
        for(int i=0;i<count;i++){
            runOneAnalysis();
        }
    }

    private Notice runOneAnalysis(){
        Notice notice = null ;
        try {
            notice = (Notice) redisTemplate.opsForList().leftPop("liuqi",0, TimeUnit.SECONDS);
            EsNotice esNotice = analysisTask.noticeToEsNotice(notice);
            disruptorOperator.publish(esNotice);
        } catch (Exception e) {
            logger.error(e,e);
        }
        return notice;
    }

    @ResponseBody
    @RequestMapping(value = "/testTask", method = RequestMethod.GET)
    public Map<String, Object> testTask(int count) {
        runAnalysis(count);
//        disruptorOperator.start();
        Map resultMap = new HashMap();
        resultMap.put("msg","解析启动成功");
        resultMap.put("count",count);
        return resultMap;
    }



    @ResponseBody
    @RequestMapping(value = "/testOneTask", method = RequestMethod.GET)
    public Map<String, Object> testOneTask() {
//        disruptorOperator.start();
        Notice notice = runOneAnalysis();
        return new ImmutableMap.Builder<String, Object>().put("status", 1)
                .put("msg", "成功").put("data",notice.getTitle()+"##type:"+notice.getCatchType()).build();
    }


    @ResponseBody
    @RequestMapping(value = "/pushRedis", method = RequestMethod.GET)
    public Map<String, Object> pushRedis() {
        try {
            testService.pushRedisNotice();
        } catch (Exception e) {
            logger.error(e,e);
        }
        return new ImmutableMap.Builder<String, Object>().put("status", 1)
                .put("msg", "push到Redis成功!").build();
    }

    @ResponseBody
    @RequestMapping(value = "/pushCustomRedis", method = RequestMethod.GET)
    public Map<String, Object> pushCustomRedis(String tbName,String title,String source) {
        try {
            int total = testService.pushCustomRedisNotice(tbName,title,source);
            return new ImmutableMap.Builder<String, Object>().put("status", 1)
                    .put("msg", "custom push到Redis成功!").put("successCount",total).build();
        } catch (Exception e) {
            logger.error(e,e);
            return new ImmutableMap.Builder<String, Object>().put("status", 0)
                    .put("msg", e.getMessage()).build();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/pushCustomRedisSec", method = RequestMethod.GET)
    public Map<String, Object> pushCustomRedisSec(String tbName,int startNum,int totalCount,String title,String source) {
        try {
            int total = testService.pushCustomRedisSec(tbName,startNum,totalCount,title,source);
            return new ImmutableMap.Builder<String, Object>().put("status", 1)
                    .put("msg", "custom push到Redis成功!").put("successCount",total).build();
        } catch (Exception e) {
            logger.error(e,e);
            return new ImmutableMap.Builder<String, Object>().put("status", 0)
                    .put("msg", e.getMessage()).build();
        }

    }

    @ResponseBody
    @RequestMapping(value = "/cleanRegexCache", method = RequestMethod.GET)
    public Map cleanRegexCache(){
        commonService.cleanRegexCache();
        return new ImmutableMap.Builder<String, Object>().put("status", 0)
                .put("msg", "清理规则缓存成功！").build();
    }

    /**
     * 解析过程调试页
     * @param tbName
     * @param title
     * @param model
     * @return
     */
    @RequestMapping(value = "/analysisProbe", method = RequestMethod.GET)
    public String  analysisProbe(String tbName,String title,Model model){
        Constant.IS_DEBUG=true;
        List<Map> infoList = new ArrayList<Map>();
        try {
            List<Notice> list = testService.debugNoticeList(tbName,title);
            for (Notice n:list) {
                EsNotice esNotice = AnalysisTask.noticeToEsNotice(n);
                Map result = testService.analysisProbe(esNotice);
                infoList.add(result);
            }
            model.addAttribute("infoList",infoList);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return "pages/analysisInfo";
    }

}
