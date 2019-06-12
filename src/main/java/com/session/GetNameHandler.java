package com.session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.PojoRequestHandler;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

/**
 * Created by weili on 2018/8/2.
 *
 * @author weili
 * @date 2018/08/02
 */
public class GetNameHandler implements PojoRequestHandler<JSONObject, JSONObject> {
    @Override
    public JSONObject handleRequest(JSONObject eventObj, Context context) {
        /**
         *  eventObj structure definition
         *
         *  read-only variables
         *  "environment": "Object",
         *  "lastOutputForFunction": "String",
         *  "slotSummary": "Object",
         *
         *  read/write variables
         *  "global": "Object",
         *  "overrideResponse": "Object",
         *  "functionOutput": "String",
         *  "routeVariable": "String"
         */
        JSONObject slots = eventObj.getJSONObject("slotSummary");
        String slotValue = slots.getString("联系人姓名.联系人");
        String linkName = "";
        eventObj.put("routeVariable", "0");

        // ansj 分词标记中文人名
        ToAnalysis toAnalysis = new ToAnalysis();
        Result result = toAnalysis.parse(slotValue);
        for (org.ansj.domain.Term term: result.getTerms()) {
            if ("nr".equals(term.getNatureStr())) {
                linkName = term.getName();
                break;
            }
        }

        // Hanlp分词标记中文人名
        Segment segment = HanLP.newSegment().enableNameRecognize(true);
        List<Term> termList = segment.seg(slotValue);
        for (Term term: termList) {
            if (term.nature.toString().equals("nr")) {
                // eventObj.getJSONObject("global").get("link_man") = term.word;
                if (linkName.length() < term.word.length()) {
                    linkName = term.word;
                }
            }
        }

        JSONObject global_params = eventObj.getJSONObject("global");
        global_params.put("link_man", linkName);
        eventObj.put("global", global_params);
        if (linkName.length() > 0) {
            eventObj.put("routeVariable", "1");
        }

        return eventObj;
    }
}