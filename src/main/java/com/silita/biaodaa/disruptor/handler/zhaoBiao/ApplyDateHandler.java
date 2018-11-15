package com.silita.biaodaa.disruptor.handler.zhaoBiao;

import com.silita.biaodaa.analysisRules.inter.DoubleFieldAnalysis;
import com.silita.biaodaa.analysisRules.notice.zhaobiao.other.OtherApplyBmEndDate;
import com.silita.biaodaa.disruptor.handler.BaseAnalysisHandler;
import com.silita.biaodaa.utils.MyStringUtils;
import com.snatch.model.EsNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析报名开始、结束时间
 * Created by gmy on 18/3/13.
 */
@Component
public class ApplyDateHandler extends BaseAnalysisHandler {

    @Autowired
    OtherApplyBmEndDate otherApplyBmEndDate;

    public ApplyDateHandler() {
        this.fieldDesc="报名开始、结束时间";
    }

    private DoubleFieldAnalysis routeRules(String source){
        switch (source){
            case "hunan": return otherApplyBmEndDate;
            default:return otherApplyBmEndDate;
        }
    }

    @Override
    protected Object currentFieldValues(EsNotice esNotice) {
        List<String> dateList = new ArrayList<>();
        if(MyStringUtils.isNotNull(esNotice.getDetail().getBmStartDate())) {
            dateList.add(esNotice.getDetail().getBmStartDate());
        }
        if(MyStringUtils.isNotNull(esNotice.getDetail().getBmEndDate())) {
            dateList.add(esNotice.getDetail().getBmEndDate());
        }
        return dateList;
    }

    @Override
    protected Object executeAnalysis(String stringPart, EsNotice esNotice) {
        DoubleFieldAnalysis analysis = routeRules(esNotice.getSource());
        return analysis.analysis(esNotice,stringPart);
    }

    @Override
    protected void saveResult(EsNotice esNotice, Object analysisResult) {
        List<String> dateList = ( List<String>)analysisResult;
        if(dateList.size() > 1) {
            if(MyStringUtils.isNotNull(dateList.get(0))) {
                if("openDate".equals(dateList.get(0))) {
                    esNotice.getDetail().setBmStartDate(esNotice.getDetail().getGsDate());
                } else {
                    esNotice.getDetail().setBmStartDate(dateList.get(0));
                }
            }
            if(MyStringUtils.isNotNull(dateList.get(1))) {
                String bmEndDateAndTime = dateList.get(1);
                if("tbEndDate".equals(bmEndDateAndTime)) {
                    esNotice.getDetail().setBmEndDate(esNotice.getDetail().getTbEndDate());
                } else {
                    if(bmEndDateAndTime.contains(":")) {
                        esNotice.getDetail().setBmEndDate(bmEndDateAndTime.substring(0,10));
                        esNotice.getDetail().setBmEndTime(bmEndDateAndTime.substring(10));
                    } else {
                        esNotice.getDetail().setBmEndDate(bmEndDateAndTime);
                    }
                }
            }
        }
    }
}
