package com.xiaoma.server.service.kb;

import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.config.MilvusClientFactory;
import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.service.RedisService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilvusServiceTest {

    @Mock
    private MilvusClientFactory factory;

    @Mock
    private KbProperties kbProperties;

    @Mock
    private KbProperties.Milvus milvusProperties;

    @Mock
    private RedisService redisService;

    @Mock
    private KbModelConfigService modelConfigService;

    @Mock
    private MilvusClientV2 milvusClient;

    @Mock
    private MilvusClientV2 milvusClient2;

    private RetryableMilvusClient retryableClient;
    private MilvusService milvusService;

    @BeforeEach
    void setUp() {
        when(kbProperties.getMilvus()).thenReturn(milvusProperties);
        when(milvusProperties.isEnabled()).thenReturn(true);
        when(milvusProperties.getRetryCooldownMs()).thenReturn(100);

        retryableClient = new RetryableMilvusClient(factory, kbProperties);
        milvusService = new MilvusService(retryableClient, kbProperties, redisService, modelConfigService);
    }

    @Test
    void search_whenClientCreationFails_thenFallbackToRedisAndRetryAfterCooldown() {
        // 第 1 次创建失败，第 2 次成功
        when(factory.createClient())
                .thenThrow(new RuntimeException("connection refused"))
                .thenReturn(milvusClient);

        KbModelConfig cfg = new KbModelConfig();
        cfg.setMilvusCollection("test_collection");
        when(modelConfigService.current()).thenReturn(cfg);
        when(redisService.keys(anyString())).thenReturn(Collections.emptySet());

        // 第 1 次：创建失败，进入 Redis fallback
        List<MilvusService.SearchResult> result1 = milvusService.search(1, new float[]{1.0f, 2.0f}, 3);
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
        verify(factory, times(1)).createClient();

        // 第 2 次：仍在冷却期内，不再尝试创建，仍 fallback
        List<MilvusService.SearchResult> result2 = milvusService.search(1, new float[]{1.0f, 2.0f}, 3);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        verify(factory, times(1)).createClient();

        // 等待冷却期过去
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟 Milvus 返回空结果
        SearchResp resp = mock(SearchResp.class);
        when(resp.getSearchResults()).thenReturn(Collections.emptyList());
        when(milvusClient.search(any(SearchReq.class))).thenReturn(resp);

        // 第 3 次：冷却后重新创建并恢复使用 Milvus
        List<MilvusService.SearchResult> result3 = milvusService.search(1, new float[]{1.0f, 2.0f}, 3);
        assertNotNull(result3);
        verify(factory, times(2)).createClient();
        verify(milvusClient, times(1)).search(any(SearchReq.class));
    }

    @Test
    void search_whenClientIsReady_thenUseMilvusDirectly() {
        when(factory.createClient()).thenReturn(milvusClient);

        KbModelConfig cfg = new KbModelConfig();
        cfg.setMilvusCollection("test_collection");
        when(modelConfigService.current()).thenReturn(cfg);

        SearchResp resp = mock(SearchResp.class);
        when(resp.getSearchResults()).thenReturn(Collections.emptyList());
        when(milvusClient.search(any(SearchReq.class))).thenReturn(resp);

        List<MilvusService.SearchResult> result = milvusService.search(1, new float[]{1.0f, 2.0f}, 3);
        assertNotNull(result);
        verify(factory, times(1)).createClient();
        verify(milvusClient, times(1)).search(any(SearchReq.class));
        verify(redisService, never()).keys(anyString());
    }

    @Test
    void getClient_whenClosed_thenReturnNull() {
        when(factory.createClient()).thenReturn(milvusClient);

        assertNotNull(retryableClient.getClient());
        retryableClient.close();
        assertNull(retryableClient.getClient());
        assertFalse(retryableClient.isReady());
        verify(milvusClient, times(1)).close();
    }

    @Test
    void search_whenMilvusDisabled_thenFallbackImmediately() {
        when(milvusProperties.isEnabled()).thenReturn(false);
        when(redisService.keys(anyString())).thenReturn(Collections.emptySet());

        // milvusEnabled 在 MilvusService 构造函数中确定为 final，需要重新创建服务
        MilvusService disabledService = new MilvusService(retryableClient, kbProperties, redisService, modelConfigService);

        List<MilvusService.SearchResult> result = disabledService.search(1, new float[]{1.0f, 2.0f}, 3);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(factory, never()).createClient();
        verify(redisService, times(1)).keys(anyString());
    }

    @Test
    void reinitialize_resetsClientAndReCreatesOnNextRequest() {
        when(factory.createClient()).thenReturn(milvusClient);

        assertNotNull(retryableClient.getClient());
        assertTrue(retryableClient.isReady());
        verify(factory, times(1)).createClient();

        when(factory.createClient()).thenReturn(milvusClient2);
        retryableClient.reinitialize();

        assertFalse(retryableClient.isReady());
        MilvusClientV2 newClient = retryableClient.getClient();
        assertSame(milvusClient2, newClient);
        verify(factory, times(2)).createClient();
        verify(milvusClient, times(1)).close();
    }

    @Test
    void isReady_reflectsClientState() {
        assertFalse(retryableClient.isReady());

        when(factory.createClient()).thenReturn(milvusClient);
        assertNotNull(retryableClient.getClient());
        assertTrue(retryableClient.isReady());
    }
}
