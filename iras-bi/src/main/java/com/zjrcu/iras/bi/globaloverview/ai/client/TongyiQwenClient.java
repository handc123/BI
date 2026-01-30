package com.zjrcu.iras.bi.globaloverview.ai.client;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TongyiQwenClient {
    @Value("${ai.tongyi.api-key}")
    private String apiKey;

    @Value("${ai.tongyi.model}")
    private String model;

    public String chat(String prompt) {

        Constants.apiKey = apiKey;

        Generation gen = new Generation();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .model(model)
                .messages(List.of(userMsg))
                .temperature(0.2f)
                .topP(0.8)
                .build();

        try {
            return gen.call(param)
                    .getOutput()
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            throw new RuntimeException("通义千问调用失败", e);
        }
    }
}
