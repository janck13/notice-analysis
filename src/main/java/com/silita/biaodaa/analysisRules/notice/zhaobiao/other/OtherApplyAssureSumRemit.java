package com.silita.biaodaa.analysisRules.notice.zhaobiao.other;

import com.silita.biaodaa.analysisRules.inter.SingleFieldAnalysis;
import com.silita.biaodaa.cache.GlobalCache;
import com.silita.biaodaa.service.CommonService;
import com.silita.biaodaa.utils.MyStringUtils;
import com.snatch.model.EsNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 保证金汇款方式
 * Created by Administrator on 2018/3/21.
 */
@Component
public class OtherApplyAssureSumRemit implements SingleFieldAnalysis {

    @Autowired
    CommonService commonService;

    @Override
    public String analysis(String segment, EsNotice esNotice, String keyWork) {
        String assureSumRemit = "";
        Map<String,List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyAssureSumRemit");
        if (arList == null) {
            arList = commonService.queryRegexByField("applyAssureSumRemit");
            analyzeRangeByFieldMap.put("applyAssureSumRemit",arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        }

        for (int i = 0; i < arList.size(); i++) {
            String regex = String.valueOf(arList.get(i).get("regex")).replaceAll("\\\\","\\\\\\\\");
            Pattern pa = Pattern.compile(regex);
            Matcher ma = pa.matcher(segment);
            if (ma.find()) {
                String txt = ma.group();
                assureSumRemit = MyStringUtils.findAssureSumRemit(txt);
                break;
            }
        }
        return assureSumRemit;
    }
}
