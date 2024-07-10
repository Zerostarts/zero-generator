package com.star.web.mapper;

import com.star.web.model.entity.Generator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author hp
* @description 针对表【generator(代码生成器)】的数据库操作Mapper
* @createDate 2024-05-30 16:50:22
* @Entity com.star.web.model.entity.Generator
*/
public interface GeneratorMapper extends BaseMapper<Generator> {

//    @Select("SELECT id, dispath,  FROM generator WHERE isDeleted = 0")
    List<Generator> listDeletedGenerator();

}




