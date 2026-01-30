package com.zjrcu.iras.bi.globaloverview.controller;

import com.zjrcu.iras.bi.globaloverview.domain.mcp.McpTool;
import com.zjrcu.iras.bi.globaloverview.mcp.McpToolRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/mcp")
public class McpController {
    private final McpToolRegistry toolRegistry;

    public McpController(McpToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @PostMapping("/invoke")
    public Object invoke(
            @RequestParam String toolName,
            @RequestBody Map<String, Object> args) {

        McpTool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            throw new RuntimeException("MCP Tool not found: " + toolName);
        }
        return tool.execute(args);
    }
    @GetMapping("/tools")
    public Map<String, Object> listTools() {

        return Map.of(
                "tools",
                toolRegistry.listTools().stream().map(tool -> Map.of(
                        "name", tool.name(),
                        "description", tool.description(),
                        "parameters", tool.parameters()
                )).toList()
        );
    }
    @PostMapping("/call")
    public Map<String, Object> call(@RequestBody Map<String, Object> request) {

        String method = (String) request.get("method");
        Object id = request.get("id");
        Map<String, Object> params =
                (Map<String, Object>) request.get("params");

        McpTool tool = toolRegistry.getTool(method);
        if (tool == null) {
            throw new RuntimeException("MCP Tool not found: " + method);
        }

        Object result = tool.execute(params);

        return Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "result", result
        );
    }


}
