package com.sky.framework.codegen;

import com.sky.framework.codegen.core.GenCore;
import lombok.extern.slf4j.Slf4j;

/**
 * 修改配置文件后,运行此代码生成代码文件
 *
 * @author
 */
@Slf4j
public class ToolApplication {

    public static void main(String[] args) {
        GenCore.generatorCode();
    }
}
