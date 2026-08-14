/**
 * Milvus业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.alibaba.fastjson2.JSON;
import com.google.gson.JsonObject;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.service.RedisService;
import com.xiaoma.server.service.kb.KbModelConfigService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Milvus业务服务类。
 * 本类定义了 MilvusService 的公共契约与数据结构。
 */
@Service
public class MilvusService {

    private static final Logger log = LoggerFactory.getLogger(MilvusService.class);

    private static final String VEC_KEY_PREFIX = "kb:vec:";

    private final RetryableMilvusClient milvusClientHolder; // milvus 客户端包装器
    private final KbProperties kbProperties; // kb 配置属性
    private final RedisService redisService; // redis 服务
    private final KbModelConfigService modelConfigService; // 模型配置服务
    private final boolean milvusEnabled; // milvusEnabled 字段

    /**
     * 构造 MilvusService 实例。
     * @param milvusClientHolder 参数说明
     * @param kbProperties 参数说明
     * @param redisService 参数说明
     * @param modelConfigService 参数说明
     */
    public MilvusService(RetryableMilvusClient milvusClientHolder,
                         KbProperties kbProperties,
                         RedisService redisService,
                         KbModelConfigService modelConfigService) {
        this.milvusClientHolder = milvusClientHolder;
        this.kbProperties = kbProperties;
        this.redisService = redisService;
        this.modelConfigService = modelConfigService;
        this.milvusEnabled = kbProperties.getMilvus().isEnabled();
    }

    /**
     * collectionName 方法。
     * @return 返回值说明
     */
    public String collectionName() {
        KbModelConfig cfg = modelConfigService.current();
        return StringUtils.hasText(cfg.getMilvusCollection())
                ? cfg.getMilvusCollection()
                : kbProperties.getMilvus().getCollection();
    }

    /**
     * insert 方法。
     * @return 返回值说明
     */
    public List<Long> insert(int categoryId, int docId, String sourceType, String fileType,
                            String title, List<String> contents, List<float[]> vectors) {
        if (contents.size() != vectors.size()) {
            throw new IllegalArgumentException("contents and vectors size mismatch");
        }

        MilvusClientV2 client = client();
        if (client == null) {
            log.warn("Milvus 不可用，知识库写入降级到 DB+Redis（docId={}）", docId);
            return fallbackInsert(categoryId, docId, sourceType, fileType, title, contents, vectors);
        }

        ensureLoaded(client);

        List<JsonObject> data = new ArrayList<>();
        long now = System.currentTimeMillis();
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            long id = ((long) docId << 20) + i;
            ids.add(id);
            JsonObject row = new JsonObject();
            row.addProperty("id", id);
            row.addProperty("category_id", (long) categoryId);
            row.addProperty("doc_id", (long) docId);
            row.addProperty("source_type", sourceType);
            row.addProperty("file_type", fileType);
            row.addProperty("title", title);
            row.addProperty("content", contents.get(i));
            row.add("vector", toJsonArray(vectors.get(i)));
            row.addProperty("status", 0);
            row.addProperty("create_time", now);
            row.addProperty("update_time", now);
            data.add(row);
        }

        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName())
                .data(data)
                .build();
        InsertResp resp = client.insert(insertReq);
        log.info("Inserted {} segments into Milvus for docId={}", resp.getInsertCnt(), docId);
        return ids;
    }

    /**
     * search 方法。
     * @param categoryId 参数说明
     * @param vector 参数说明
     * @param topK 参数说明
     * @return 返回值说明
     */
    public List<SearchResult> search(int categoryId, float[] vector, int topK) {
        MilvusClientV2 client = client();
        if (client == null) {
            log.warn("Milvus 不可用，知识库检索降级到 DB+Redis（categoryId={}）", categoryId);
            return fallbackSearch(categoryId, vector, topK);
        }

        ensureLoaded(client);

        List<String> outputFields = List.of("id", "doc_id", "title", "content", "source_type", "file_type");
        SearchReq searchReq = SearchReq.builder()
                .collectionName(collectionName())
                .data(Collections.singletonList(new FloatVec(vector)))
                .annsField("vector")
                .topK(topK)
                .outputFields(outputFields)
                .filter("category_id == " + categoryId + " and status == 0")
                .build();

        SearchResp resp = client.search(searchReq);
        List<List<SearchResp.SearchResult>> results = resp.getSearchResults();
        if (results == null || results.isEmpty()) {
            log.info("Milvus 检索返回空: categoryId={}", categoryId);
            return Collections.emptyList();
        }

        List<SearchResult> list = new ArrayList<>();
        for (SearchResp.SearchResult r : results.get(0)) {
            Map<String, Object> entity = r.getEntity();
            SearchResult sr = new SearchResult();
            sr.id = ((Number) entity.get("id")).longValue();
            sr.docId = ((Number) entity.get("doc_id")).intValue();
            sr.title = (String) entity.get("title");
            sr.content = (String) entity.get("content");
            sr.sourceType = (String) entity.get("source_type");
            sr.fileType = (String) entity.get("file_type");
            sr.score = r.getScore();
            list.add(sr);
        }
        log.info("Milvus 检索完成: categoryId={}, topK={}, hits={}", categoryId, topK, list.size());
        return list;
    }

    /**
     * deleteByDocId 方法。
     * @param docId 参数说明
     */
    public void deleteByDocId(int docId) {
        MilvusClientV2 client = client();
        if (client == null) {
            log.warn("Milvus 不可用，删除文档向量降级到 DB+Redis（docId={}）", docId);
            fallbackDeleteByDocId(docId);
            return;
        }

        ensureLoaded(client);
        client.delete(DeleteReq.builder()
                .collectionName(collectionName())
                .filter("doc_id == " + docId)
                .build());
    }

    /**
     * deleteByIds 方法。
     * @param ids 参数说明
     */
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        MilvusClientV2 client = client();
        if (client == null) {
            log.warn("Milvus 不可用，按 ID 删除向量降级到 DB+Redis（ids={}）", ids);
            fallbackDeleteByIds(ids);
            return;
        }

        ensureLoaded(client);
        StringBuilder expr = new StringBuilder("id in [");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) expr.append(",");
            expr.append(ids.get(i));
        }
        expr.append("]");
        client.delete(DeleteReq.builder()
                .collectionName(collectionName())
                .filter(expr.toString())
                .build());
    }

    private MilvusClientV2 client() {
        return milvusEnabled ? milvusClientHolder.getClient() : null;
    }

    private void ensureLoaded(MilvusClientV2 client) {
        try {
            client.loadCollection(LoadCollectionReq.builder()
                    .collectionName(collectionName())
                    .build());
        } catch (Exception e) {
            log.warn("Load collection failed (may already loaded): {}", e.getMessage());
        }
    }

    /* ==================== DB + Redis 降级实现 ==================== */

    private List<Long> fallbackInsert(int categoryId, int docId, String sourceType, String fileType,
                                      String title, List<String> contents, List<float[]> vectors) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            long id = ((long) docId << 20) + i;
            ids.add(id);

            VectorDoc doc = new VectorDoc();
            doc.categoryId = categoryId;
            doc.docId = docId;
            doc.seq = i;
            doc.sourceType = sourceType;
            doc.fileType = fileType;
            doc.title = title;
            doc.content = contents.get(i);
            doc.vector = vectors.get(i);

            String key = VEC_KEY_PREFIX + docId + ":" + i;
            redisService.set(key, JSON.toJSONString(doc));
        }
        log.info("Fallback inserted {} segments into Redis for docId={}", contents.size(), docId);
        return ids;
    }

    private List<SearchResult> fallbackSearch(int categoryId, float[] vector, int topK) {
        Set<String> keys = redisService.keys(VEC_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.warn("Redis 中没有向量数据，降级检索无结果: categoryId={}", categoryId);
            return Collections.emptyList();
        }

        List<ScoredResult> scored = new ArrayList<>();
        for (String key : keys) {
            try {
                String json = redisService.get(key);
                if (json == null || json.isBlank()) {
                    continue;
                }
                VectorDoc doc = JSON.parseObject(json, VectorDoc.class);
                if (doc == null || doc.categoryId != categoryId || doc.vector == null) {
                    continue;
                }
                float score = cosineSimilarity(vector, doc.vector);
                SearchResult sr = new SearchResult();
                sr.id = ((long) doc.docId << 20) + doc.seq;
                sr.docId = doc.docId;
                sr.title = doc.title;
                sr.content = doc.content;
                sr.sourceType = doc.sourceType;
                sr.fileType = doc.fileType;
                sr.score = score;
                scored.add(new ScoredResult(sr, score));
            } catch (Exception e) {
                log.warn("解析 Redis 向量数据失败, key={}: {}", key, e.getMessage());
            }
        }

        scored.sort((a, b) -> Float.compare(b.score, a.score));
        List<SearchResult> result = new ArrayList<>(Math.min(topK, scored.size()));
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            result.add(scored.get(i).result);
        }
        log.info("Redis 降级检索完成: categoryId={}, redisKeys={}, matched={}, topK={}, hits={}",
                categoryId, keys.size(), scored.size(), topK, result.size());
        return result;
    }

    private void fallbackDeleteByDocId(int docId) {
        Set<String> keys = redisService.keys(VEC_KEY_PREFIX + docId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisService.delete(keys);
            log.info("Fallback deleted {} Redis vectors for docId={}", keys.size(), docId);
        }
    }

    private void fallbackDeleteByIds(List<Long> ids) {
        List<String> keys = new ArrayList<>();
        for (Long id : ids) {
            int docId = (int) (id >> 20);
            int seq = (int) (id & 0xFFFFFL);
            keys.add(VEC_KEY_PREFIX + docId + ":" + seq);
        }
        redisService.delete(keys);
        log.info("Fallback deleted {} Redis vectors by ids", keys.size());
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0f;
        }
        double dot = 0d;
        double normA = 0d;
        double normB = 0d;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0d || normB == 0d) {
            return 0f;
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private com.google.gson.JsonArray toJsonArray(float[] vector) {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (float v : vector) {
            array.add(v);
        }
        return array;
    }

    public static class SearchResult {
        public long id; // id 字段
        public int docId; // docId 字段
        public String title; // title 字段
        public String content; // content 字段
        public String sourceType; // sourceType 字段
        public String fileType; // fileType 字段
        public float score; // score 字段
    }

    /**
     * 降存在 Redis 中的向量文档。
     */
    public static class VectorDoc {
        public int categoryId;
        public int docId;
        public int seq;
        public String sourceType;
        public String fileType;
        public String title;
        public String content;
        public float[] vector;
    }

    private record ScoredResult(SearchResult result, float score) {}
}
