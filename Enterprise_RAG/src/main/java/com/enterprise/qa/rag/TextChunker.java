package com.enterprise.qa.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档分块器：按固定窗口 + 重叠切分，并尽量在句/段边界断句以保留语义完整性。
 */
@Component
public class TextChunker {

    private static final String[] SENTENCE_BOUNDARY = {"。", "！", "？", "\n", "；", ". ", "! ", "? "};

    public List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        if (overlap >= chunkSize) {
            overlap = Math.max(0, chunkSize - 1);
        }
        int start = 0;
        while (start < text.length()) {
            int end = findCutPoint(text, start, chunkSize);
            result.add(text.substring(start, end).trim());
            if (end >= text.length()) {
                break;
            }
            start = Math.max(start + 1, end - overlap);
        }
        return result.stream().filter(s -> !s.isBlank()).toList();
    }

    /** 在窗口内寻找最近的句末边界；找不到则硬切。 */
    private int findCutPoint(String text, int start, int size) {
        int hardEnd = Math.min(text.length(), start + size);
        if (hardEnd >= text.length()) {
            return hardEnd;
        }
        int cut = hardEnd;
        int best = -1;
        for (String b : SENTENCE_BOUNDARY) {
            int idx = text.indexOf(b, start + size / 2);
            if (idx >= 0 && idx < hardEnd) {
                int candidate = idx + b.length();
                if (candidate > best) {
                    best = candidate;
                }
            }
        }
        if (best > 0) {
            cut = Math.min(best, hardEnd);
        }
        return cut;
    }
}
