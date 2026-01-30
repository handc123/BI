package com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.ai;

import com.zjrcu.iras.bi.globaloverview.ai.client.TongyiQwenClient;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateNonPerformingLoanVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RealEstateLoanDetailAIService {
    @Resource
    private TongyiQwenClient tongyiQwenClient;


    public String analyzeRealEstateNpl(List<RealEstateNonPerformingLoanVo> list) {

        if (list == null || list.isEmpty()) {
            return "当前时间范围内未查询到房地产不良贷款数据。";
        }

        String prompt = buildPrompt(list);

        return tongyiQwenClient.chat(prompt);
    }

    private String buildPrompt(List<RealEstateNonPerformingLoanVo> list) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
你是一名银行风险管理专家。
请基于以下房地产不良贷款趋势数据进行分析，
仅允许基于数据本身，不得编造事实。

分析要求：
1. 判断整体趋势（上升 / 下降 / 稳定）
2. 给出简要风险提示（如有）
3. 不超过 3 句话

数据如下：
""");

        for (RealEstateNonPerformingLoanVo vo : list) {
            sb.append(String.format(
                    "日期：%s，不良贷款余额：%s，不良贷款率：%s%n",
                    vo.getDataDate(),
                    vo.getRealEstateNonPerformingLoanBalance(),
                    vo.getRealEstateNonPerformingLoanRatio()
            ));
        }

        return sb.toString();

    }
}
