package com.star.web.manager;

import com.star.web.vertx.MainVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class VertxManager {

    @Resource
    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        Vertx vertx = Vertx.vertx();
        Verticle myVerticle = new MainVerticle(cacheManager);
        vertx.deployVerticle(myVerticle);
    }
}

