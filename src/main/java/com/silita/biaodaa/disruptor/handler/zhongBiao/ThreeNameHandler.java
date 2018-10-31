package com.silita.biaodaa.disruptor.handler.zhongBiao;

import com.silita.biaodaa.analysisRules.notice.zhongbiao.OtherThreeName;
import com.silita.biaodaa.disruptor.handler.BaseAnalysisHandler;
import com.snatch.model.EsNotice;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hujia on 18/3/20.
 * 第三中标人
 */
@Component
public class ThreeNameHandler extends BaseAnalysisHandler {

    Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    OtherThreeName otherThreeName;

    public ThreeNameHandler(){
        this.fieldDesc="第三中标人";
    }

    @Override
    protected Object currentFieldValues(EsNotice esNotice) {
        return esNotice.getDetailZhongBiao().getThreeName();
    }


    @Override
    protected String executeAnalysis(String stringPart,EsNotice esNotice)  throws Exception{
        return otherThreeName.analysis(stringPart,esNotice,null);
    }

    @Override
    protected void saveResult(EsNotice esNotice, Object analysisResult) {
        esNotice.getDetailZhongBiao().setThreeName((String) analysisResult);
    }

}
