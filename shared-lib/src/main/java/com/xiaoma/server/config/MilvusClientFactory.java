/**
 * Milvus 客户端工厂。
 * 负责根据当前配置创建并初始化 MilvusClientV2，不缓存客户端实例。
 */
package com.xiaoma.server.config;

import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.service.kb.KbModelConfigService;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.common.IndexParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Milvus 客户端工厂。
 * 封装 MilvusClientV2 的创建与 collection 初始化逻辑。
 */
@Component
public class MilvusClientFactory {

    private static final Logger log = LoggerFactory.getLogger(MilvusClientFactory.class);

    private final KbProperties kbProperties;
    private final KbModelConfigService modelConfigService;

    /**
     * 构造 MilvusClientFactory 实例。
     */
    public MilvusClientFactory(KbProperties kbProperties, KbModelConfigService modelConfigService) {
        this.kbProperties = kbProperties;
        this.modelConfigService = modelConfigService;
    }

    /**
     * 根据当前配置创建并初始化 MilvusClientV2。
     *
     * @return 初始化后的客户端；若 kb.milvus.enabled=false 则返回 null
     * @throws RuntimeException 连接或 collection 初始化失败时抛出
     */
    public MilvusClientV2 createClient() {
        if (!kbProperties.getMilvus().isEnabled()) {
            log.info("Milvus is disabled via kb.milvus.enabled=false, skipping MilvusClientV2 creation");
            return null;
        }

        try {
            KbModelConfig cfg = modelConfigService.current();
            String host = StringUtils.hasText(cfg.getMilvusHost()) ? cfg.getMilvusHost() : kbProperties.getMilvus().getHost();
            Integer portValue = cfg.getMilvusPort() != null ? cfg.getMilvusPort() : kbProperties.getMilvus().getPort();
            int port = portValue != null ? portValue : 19530;
            String username = StringUtils.hasText(cfg.getMilvusUsername()) ? cfg.getMilvusUsername() : kbProperties.getMilvus().getUsername();
            String password = StringUtils.hasText(cfg.getMilvusPassword()) ? cfg.getMilvusPassword() : kbProperties.getMilvus().getPassword();
            String database = StringUtils.hasText(cfg.getMilvusDatabase()) ? cfg.getMilvusDatabase() : kbProperties.getMilvus().getDatabase();

            String uri = String.format("http://%s:%d", host, port);
            String token = username + ":" + password;
            log.info("Connecting to Milvus at {} (database={})", uri, database);
            ConnectConfig connectConfig = ConnectConfig.builder()
                    .uri(uri)
                    .token(token)
                    .dbName(database)
                    .build();

            MilvusClientV2 client = new MilvusClientV2(connectConfig);
            initCollection(client, cfg);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Milvus client: " + e.getMessage(), e);
        }
    }

    private void initCollection(MilvusClientV2 client, KbModelConfig cfg) {
        String collectionName = StringUtils.hasText(cfg.getMilvusCollection())
                ? cfg.getMilvusCollection()
                : kbProperties.getMilvus().getCollection();
        try {
            boolean exists = client.hasCollection(HasCollectionReq.builder()
                    .collectionName(collectionName)
                    .build());
            if (exists) {
                log.info("Milvus collection '{}' already exists", collectionName);
                return;
            }

            int dim = cfg.getMilvusVectorDim() != null
                    ? cfg.getMilvusVectorDim()
                    : kbProperties.getMilvus().getVectorDim();
            List<CreateCollectionReq.FieldSchema> fieldList = new ArrayList<>();
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("id")
                    .dataType(DataType.Int64)
                    .isPrimaryKey(true)
                    .autoID(false)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("category_id")
                    .dataType(DataType.Int64)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("doc_id")
                    .dataType(DataType.Int64)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("source_type")
                    .dataType(DataType.VarChar)
                    .maxLength(32)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("file_type")
                    .dataType(DataType.VarChar)
                    .maxLength(32)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("title")
                    .dataType(DataType.VarChar)
                    .maxLength(512)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("content")
                    .dataType(DataType.VarChar)
                    .maxLength(65535)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("vector")
                    .dataType(DataType.FloatVector)
                    .dimension(dim)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("status")
                    .dataType(DataType.Int8)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("create_time")
                    .dataType(DataType.Int64)
                    .build());
            fieldList.add(CreateCollectionReq.FieldSchema.builder()
                    .name("update_time")
                    .dataType(DataType.Int64)
                    .build());

            CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                    .fieldSchemaList(fieldList)
                    .build();

            String metricType = StringUtils.hasText(cfg.getMilvusMetricType())
                    ? cfg.getMilvusMetricType().toUpperCase()
                    : kbProperties.getMilvus().getMetricType().toUpperCase();
            Map<String, Object> hnswExtra = new HashMap<>();
            hnswExtra.put("M", 16);
            hnswExtra.put("efConstruction", 200);
            IndexParam vectorIndex = IndexParam.builder()
                    .fieldName("vector")
                    .indexType(IndexParam.IndexType.HNSW)
                    .metricType(IndexParam.MetricType.valueOf(metricType))
                    .extraParams(hnswExtra)
                    .build();

            String consistencyLevel = StringUtils.hasText(cfg.getMilvusConsistencyLevel())
                    ? cfg.getMilvusConsistencyLevel().toUpperCase()
                    : kbProperties.getMilvus().getConsistencyLevel().toUpperCase();
            CreateCollectionReq createReq = CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .collectionSchema(schema)
                    .indexParams(Collections.singletonList(vectorIndex))
                    .consistencyLevel(ConsistencyLevel.valueOf(consistencyLevel))
                    .build();

            client.createCollection(createReq);

            createScalarIndex(client, collectionName, "category_id");
            createScalarIndex(client, collectionName, "doc_id");
            createScalarIndex(client, collectionName, "status");

            log.info("Milvus collection '{}' created successfully with dim={}", collectionName, dim);
        } catch (Exception e) {
            log.error("Failed to initialize Milvus collection '{}': {}", collectionName, e.getMessage(), e);
        }
    }

    private void createScalarIndex(MilvusClientV2 client, String collectionName, String fieldName) {
        try {
            client.createIndex(CreateIndexReq.builder()
                    .collectionName(collectionName)
                    .indexParams(Collections.singletonList(
                            IndexParam.builder()
                                    .fieldName(fieldName)
                                    .indexType(IndexParam.IndexType.AUTOINDEX)
                                    .build()
                    ))
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create scalar index on {}: {}", fieldName, e.getMessage());
        }
    }
}
