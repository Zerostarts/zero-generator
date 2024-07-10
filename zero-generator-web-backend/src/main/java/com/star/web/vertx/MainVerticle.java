package com.star.web.vertx;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.star.web.common.ResultUtils;
import com.star.web.controller.GeneratorController;
import com.star.web.manager.CacheManager;
import com.star.web.model.dto.generator.GeneratorQueryRequest;
import com.star.web.model.entity.Generator;
import com.star.web.model.vo.GeneratorVO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

public class MainVerticle extends AbstractVerticle {


    private CacheManager cacheManager;

    public MainVerticle(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    HttpMethod method = req.method();
                    String path = req.path();
                    //分页获取生成器
                    if (HttpMethod.POST.equals(method) && "/generator/page".equals(path)) {
                        //设置请求体处理器
                        req.handler(buffer -> {
                            String requestBody = buffer.toString();
                            GeneratorQueryRequest generatorQueryRequest = JSONUtil.toBean(requestBody, GeneratorQueryRequest.class);
                            //处理json数据
                            String cacheKey = GeneratorController.getPageCacheKey(generatorQueryRequest);
                            //设置响应头
                            HttpServerResponse response = req.response();
                            response.putHeader("content-type", "application/json");
                            //本地缓存
                            Object value = cacheManager.get(cacheKey);
                            if (value != null) {
                                response.end(JSONUtil.toJsonStr(ResultUtils.success((Page<GeneratorVO>) value)));
                                return;
                            }
                            response.end("");
                        });
                    }

                })
                .listen(8888)
                .onSuccess(server -> {
                    System.out.println("HTTP server started on port " + server.actualPort());
                });
    }
}
