package com.zjrcu.iras.bi.globaloverview.mcp;

import com.zjrcu.iras.bi.globaloverview.domain.mcp.McpTool;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class McpToolRegistry {
    private final Map<String, McpTool> toolMap = new HashMap<>();

    public McpToolRegistry(List<McpTool> tools) {
        for (McpTool tool : tools) {
            toolMap.put(tool.name(), tool);
        }
    }

    public McpTool getTool(String name) {
        return toolMap.get(name);
    }

    public Collection<McpTool> listTools() {
        return toolMap.values();
    }
}
