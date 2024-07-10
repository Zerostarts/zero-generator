package com.star.web.mapper;

import com.star.web.model.entity.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class GeneratorMapperTest {


    @Resource
    private GeneratorMapper generatorMapper;
    @Test
    void selectDeleted() {
        List<Generator> generators = generatorMapper.listDeletedGenerator();
        generators.forEach(System.out::println);
    }

}