package com.silita.biaodaa.disruptor.handler.zhaoBiao;

import com.silita.biaodaa.analysisRules.inter.SingleFieldAnalysis;
import com.silita.biaodaa.analysisRules.notice.zhaobiao.hunan.HunanProjSum;
import com.silita.biaodaa.common.Constant;
import com.silita.biaodaa.disruptor.handler.BaseAnalysisHandler;
import com.snatch.model.EsNotice;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangxiahui on 18/3/13.
 * 项目金额
 */
@Component
public class ApplyProjSumHandler extends BaseAnalysisHandler {

    Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    HunanProjSum hunanProjSum;

    public ApplyProjSumHandler(){
        this.fieldDesc="项目金额";
    }

    private SingleFieldAnalysis routeRules(String source){
        return hunanProjSum;
    }

    @Override
    protected Object currentFieldValues(EsNotice esNotice) {
        return null;
    }

    @Override
    protected Object executeAnalysis(String stringPart,EsNotice esNotice)  throws Exception{
        if(esNotice.getTitle().indexOf("设计")>0
                ||esNotice.getTitle().indexOf("监理")>0
                ||esNotice.getTitle().indexOf("勘察")>0
                ||esNotice.getTitle().indexOf("审计")>0
                ||esNotice.getTitle().indexOf("招标代理")>0){
            return null;
        }
        SingleFieldAnalysis analysis = routeRules(esNotice.getSource());
        String value =  analysis.analysis(stringPart,esNotice,this.keyWord);
        if(value!=null){
            if(value.indexOf(Constant.SPLIT_STRING+"-")==0){
                this.keyWord = value.replace(Constant.SPLIT_STRING+"-","");
                value = null;
            }else if(value.indexOf(Constant.SPLIT_STRING)==0){
                this.keyValue = value.replace(Constant.SPLIT_STRING,"");
                value = null;
                this.keyWord = null;
            }
        }
        return value;



    }

    @Override
    protected void saveResult(EsNotice esNotice, Object analysisResult) {
        if(analysisResult!=null){
            String proJSum = analysisResult.toString();
            if(!"".equals(proJSum)){
                esNotice.getDetail().setProjSum(analysisResult.toString());

            }
        }
    }


}
