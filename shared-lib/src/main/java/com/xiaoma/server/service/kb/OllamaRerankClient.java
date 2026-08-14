/**
 * OllamaRerank客户端实现类。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiaoma.server.common.BizException;
import jakarta.annotation.PreDestroy;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 Ollama Rerank 客户端。
 * <p>
 * 通过 Ollama /api/generate 端点调用专用 reranker 模型，对向量召回的候选片段进行精排。
 * 默认模型为 dengcao/Qwen3-Reranker-0.6B:Q8_0，适合本地 CPU/GPU 推理。
 * <p>
 * 注意：Ollama 没有原生 rerank API，这里采用 instruction prompt 让模型输出 0~1 相关性分数的方式实现。
 * 为了降低延迟，支持并发打分和本地缓存。
 */
@Component
public class OllamaRerankClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaRerankClient.class);
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern SCORE_PATTERN = Pattern.compile("(0(\\.\\d+)?|1(\\.0*)?)");

    @Value("${ollama.base-url:http://127.0.0.1:11434}")
    private String baseUrl; // baseUrl 字段

    @Value("${ollama.rerank-model:}")
    private String rerankModel; // rerankModel 字段

    @Value("${ollama.rerank-concurrency:4}")
    private int concurrency; // concurrency 字段

    private final OkHttpClient client; // client 字段
    private final ExecutorService executor; // executor 字段
    private final Cache<String, Double> scoreCache; // scoreCache 字段

    /**
     * 构造 OllamaRerankClient 实例。
     */
    public OllamaRerankClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors()),
                r -> {
                    Thread t = new Thread(r, "ollama-rerank-");
                    t.setDaemon(true);
                    return t;
                });
        this.scoreCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    /**
     * 对候选片段进行重排，返回按相关性分数降序排列的前 limit 条。
     *
     * @param query          用户问题
     * @param candidates     向量召回的候选片段（会被修改 score 字段）
     * @param limit          返回条数上限
     * @param promptTemplate prompt 模板，需包含 {query} 和 {passage} 占位符
     * @return 重排后的候选列表；若调用失败则降级返回原候选列表
     */
    public List<MilvusService.SearchResult> rerank(String query,
                                                     List<MilvusService.SearchResult> candidates,
                                                     int limit,
                                                     String promptTemplate) {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("Ollama base-url 未配置，跳过 rerank");
            return candidates;
        }
        if (rerankModel == null || rerankModel.isBlank()) {
            log.warn("Ollama rerank-model 未配置，跳过 rerank");
            return candidates;
        }
        if (candidates == null || candidates.isEmpty()) {
            return candidates;
        }
        if (promptTemplate == null || promptTemplate.isBlank()
                || !promptTemplate.contains("{query}") || !promptTemplate.contains("{passage}")) {
            throw new BizException("rerank prompt 模板不合法，必须包含 {query} 和 {passage} 占位符");
        }

        long start = System.currentTimeMillis();
        String url = baseUrl.replaceAll("/$", "") + "/api/generate";

        // 并发打分
        List<CompletableFuture<ScoredResult>> futures = new ArrayList<>(candidates.size());
        for (MilvusService.SearchResult candidate : candidates) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> scoreCandidate(candidate, query, promptTemplate, url), executor));
        }

        List<ScoredResult> scoredResults = new ArrayList<>(candidates.size());
        for (CompletableFuture<ScoredResult> future : futures) {
            try {
                scoredResults.add(future.get(70, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("Rerank 并发任务执行异常", e);
                // 继续处理其他任务，异常任务不加入结果
            }
        }

        if (scoredResults.isEmpty()) {
            log.warn("Rerank 全部失败，降级返回原候选列表");
            return candidates;
        }

        // 按 rerank 分数降序，分数相同则保留原向量分数高的在前
        scoredResults.sort(Comparator.comparingDouble((ScoredResult sr) -> sr.score)
                .thenComparingDouble(sr -> sr.candidate.score)
                .reversed());

        List<MilvusService.SearchResult> result = new ArrayList<>(Math.min(limit, scoredResults.size()));
        for (int i = 0; i < scoredResults.size() && i < limit; i++) {
            ScoredResult sr = scoredResults.get(i);
            sr.candidate.score = (float) sr.score;
            result.add(sr.candidate);
        }

        log.debug("Rerank 完成：候选 {} 条 → 返回 {} 条，并发 {}，耗时 {} ms，缓存命中率 {}",
                candidates.size(), result.size(), concurrency, System.currentTimeMillis() - start,
                scoreCache.stats().hitRate());
        return result;
    }

    private ScoredResult scoreCandidate(MilvusService.SearchResult candidate,
                                        String query,
                                        String promptTemplate,
                                        String url) {
        String passage = candidate.content != null ? candidate.content : "";
        String prompt = promptTemplate.replace("{query}", query).replace("{passage}", passage);
        String cacheKey = buildCacheKey(prompt);

        Double cached = scoreCache.getIfPresent(cacheKey);
        if (cached != null) {
            return new ScoredResult(candidate, cached);
        }

        GenerateRequest requestBody = new GenerateRequest();
        requestBody.model = rerankModel;
        requestBody.prompt = prompt;
        requestBody.stream = false;
        requestBody.options = new Options();
        requestBody.options.temperature = 0.0;

        RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), JSON_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        double score = 0.0;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String err = response.body() != null ? response.body().string() : "";
                log.error("Ollama rerank error: status={}, body={}", response.code(), err);
            } else {
                String rawJson = response.body().string();
                score = parseScore(rawJson);
                scoreCache.put(cacheKey, score);
            }
        } catch (Exception e) {
            log.error("Ollama rerank 单条评分异常, docId={}", candidate.docId, e);
            score = 0.0;
        }

        return new ScoredResult(candidate, score);
    }

    private String buildCacheKey(String prompt) {
        return rerankModel + "|" + Objects.hash(prompt);
    }

    private double parseScore(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return 0.0;
        }
        try {
            GenerateResponse resp = JSON.parseObject(rawJson, GenerateResponse.class);
            if (resp == null || resp.response == null || resp.response.isBlank()) {
                return 0.0;
            }
            String text = resp.response.trim();
            Matcher matcher = SCORE_PATTERN.matcher(text);
            if (matcher.find()) {
                double score = Double.parseDouble(matcher.group(1));
                return Math.max(0.0, Math.min(1.0, score));
            }
        } catch (Exception e) {
            log.warn("解析 rerank 分数失败: {}", rawJson, e);
        }
        return 0.0;
    }

    /**
     * shutdown 方法。
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class ScoredResult {
        final MilvusService.SearchResult candidate;
        final double score; // score 字段

        ScoredResult(MilvusService.SearchResult candidate, double score) {
            this.candidate = candidate;
            this.score = score;
        }
    }

    public static class GenerateRequest {
        public String model; // model 字段
        public String prompt; // prompt 字段
        public Boolean stream; // stream 字段
        public Options options; // options 字段
    }

    public static class Options {
        public Double temperature; // temperature 字段
    }

    public static class GenerateResponse {
        public String model; // model 字段
        public String response; // response 字段
        public Boolean done; // done 字段
    }
}
