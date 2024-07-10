package com.star.web.job;


import cn.hutool.core.util.StrUtil;
import com.star.web.manager.CosManager;
import com.star.web.mapper.GeneratorMapper;
import com.star.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearJobHandler {
    /**
     * 每天执行
     */
    @Resource
    private CosManager cosManager;
    @Resource
    private GeneratorMapper generatorMapper;


    @XxlJob("clearCosJobHandler")
    public void clearCosJobHandler() {
        log.info("clearCosJobHandler start");
        //编写业务逻辑
        //1. 删除用户上传的模板文件
        cosManager.deleteDir("/test/");
        //2. 已删除的代码生成器对应的产物包
        List<Generator> generatorList = generatorMapper.listDeletedGenerator();

        List<String> keyList = generatorList.stream().map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                .map(disPath -> disPath.substring(1))
                .collect(Collectors.toList());
        cosManager.deleteObjects(keyList);

        log.info("clearCosJobHandler end");

    }

}
