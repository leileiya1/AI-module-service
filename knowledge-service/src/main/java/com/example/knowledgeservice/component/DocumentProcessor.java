package com.example.knowledgeservice.component;

import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DocumentProcessor {

    private final Tika tika = new Tika();
    private static final int CHUNK_SIZE = 250; // 每个片段的目标大小（字符数）
    private static final int CHUNK_OVERLAP = 50; // 相邻片段的重叠大小（字符数）

    /**
     * 从输入流解析文本内容并切分为片段。
     * @param inputStream 文件输入流
     * @return 文本片段列表
     */
    public List<String> parseAndChunk(InputStream inputStream) throws Exception {
        log.info("【文档处理器】开始从输入流解析文本...");
        String text = tika.parseToString(inputStream);
        log.info("【文档处理器】文本解析完成，总字符数: {}。开始切分...", text.length());
        List<String> chunks = new ArrayList<>();
        // 使用简单的滑动窗口进行切分
        for (int i = 0; i < text.length(); i += (CHUNK_SIZE - CHUNK_OVERLAP)) {
            int end = Math.min(i + CHUNK_SIZE, text.length());
            chunks.add(text.substring(i, end));
            if (end == text.length()) {
                break;
            }
        }
        log.info("【文档处理器】文本切分完成，共生成 {} 个片段。", chunks.size());
        return chunks;
    }
}
