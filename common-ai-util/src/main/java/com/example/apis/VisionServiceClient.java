package com.example.apis;

import com.example.dto.AI.VisionAnalysisRequest;
import com.example.dto.AI.VisionAnalysisResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用于调用 vision-analysis-service 的 OpenFeign 客户端。
 */
@FeignClient(name = "vision-service", url = "http://localhost:1242", path = "/api/v1")
public interface VisionServiceClient {

    /**
     * 调用视觉分析服务的 /api/v1/vision/analyze 端点。
     *
     * @param request 包含图片URL的请求体
     * @return 包含图片文字描述的响应体
     */
    @PostMapping("/vision/analyze")
    VisionAnalysisResponse analyze(@RequestBody VisionAnalysisRequest request);
}
