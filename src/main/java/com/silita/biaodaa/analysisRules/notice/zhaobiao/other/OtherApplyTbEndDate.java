package com.silita.biaodaa.analysisRules.notice.zhaobiao.other;

import com.silita.biaodaa.analysisRules.inter.SingleFieldAnalysis;
import com.silita.biaodaa.cache.GlobalCache;
import com.silita.biaodaa.service.CommonService;
import com.silita.biaodaa.utils.MyStringUtils;
import com.snatch.model.EsNotice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 其他站点投标结束时间
 * Created by 91567 on 2018/3/21.
 */
@Component
public class OtherApplyTbEndDate implements SingleFieldAnalysis {

    private Log logger = LogFactory.getLog(OtherApplyTbEndDate.class);

    @Autowired
    CommonService commonService;

    @Override
    public String analysis(String segment, EsNotice esNotice, String keyWord) {
        segment = segment.replaceAll("<[^>]+>", "").replaceAll(" style=\\\"(.*?)\\\"", "");
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
        String dateRegex = "(\\d{4}-\\d{1,2}-\\d{1,2})|(\\d{4}年\\d{1,2}月\\d{1,2})|(\\d{4}/\\d{1,2}/\\d{1,2})";//匹配日期格式1
        String timeRegex = "(\\d{1,2}:\\d{2})|(\\d{1,2}时\\d{0,2})|(\\d{1,2}：\\d{2})|(\\d{1,2}点\\d{0,2})";
        List<String> list = null;
        List<String> list2 = null;
        String bmEndDateAndTime = "";
        String rangeHtml = "";

        Map<String, List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyBidDate");
        if (arList == null) {
            arList = commonService.queryRegexByField("applyBidDate");
            analyzeRangeByFieldMap.put("applyBidDate", arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        } else {
//            System.out.println("=========applyDate=======走的缓存=======");
        }

        for (int i = 0; i < arList.size(); i++) {
            String rangeRegex = arList.get(i).get("regex").toString();
            Pattern rangePat = Pattern.compile(rangeRegex);
            Matcher rangeMat = rangePat.matcher(segment);
            if (rangeMat.find()) {
                list = new ArrayList<String>();
                list2 = new ArrayList<String>();
                rangeHtml = rangeMat.group();
                if(rangeHtml.length() > 300) {
                    continue;
                }
                //匹配日期
                Pattern datePat = Pattern.compile(dateRegex);
                Matcher dateMat = datePat.matcher(rangeHtml);
                while (dateMat.find()) {
                    try {
                        list.add(dfDate.format(dfDate.parse(dateMat.group().replaceAll("年", "-").replaceAll("月", "-").replaceAll("/", "-"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                //匹配时间
                Pattern timePat = Pattern.compile(timeRegex);
                Matcher timeMat = timePat.matcher(rangeHtml);
                while (timeMat.find()) {
                    try {
                        if (MyStringUtils.isNull(timeMat.group().replaceAll("\\d{0,2}时", ""))) {
                            list2.add(dfTime.format(dfTime.parse(timeMat.group().replaceAll("时", ":00"))));
                        } else {
                            list2.add(dfTime.format(dfTime.parse(timeMat.group().replaceAll("时", ":").replaceAll("：", ":").replaceAll("点", ":"))));
                        }
                    } catch (ParseException e) {
                        logger.error(e,e);
                        continue;
                    }
                }
                //拼凑日期+时间
                if (list2.size() > 0 && list.size() > 0) {
                    bmEndDateAndTime = list.get(list.size() - 1) + list2.get(list2.size() - 1);
                } else if (list.size() > 0) {
                    bmEndDateAndTime = list.get(list.size() - 1);
                }
                if (MyStringUtils.isNotNull(bmEndDateAndTime)) {
                    break;
                }
            }
        }
        return bmEndDateAndTime;
    }
}
