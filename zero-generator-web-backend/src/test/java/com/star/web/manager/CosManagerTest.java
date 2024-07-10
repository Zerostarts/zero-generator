package com.star.web.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;


    @Test
    void deleteObject() {
        cosManager.deleteObject("test.jpg");
    }


    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("generator_make_template/1/a.zip", "generator_make_template/1/b.zip"));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("/generator_picture/1/");
    }


}