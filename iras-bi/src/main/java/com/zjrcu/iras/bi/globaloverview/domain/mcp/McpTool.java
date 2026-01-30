package com.zjrcu.iras.bi.globaloverview.domain.mcp;

import java.util.Map;

public interface McpTool {
    String name();

    String description();

    Map<String, Object> parameters();

    Object execute(Map<String, Object> args);
}
