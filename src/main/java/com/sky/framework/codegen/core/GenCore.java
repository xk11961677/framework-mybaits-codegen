package com.sky.framework.codegen.core;

import com.sky.framework.codegen.model.CodegenEntity;
import com.sky.framework.codegen.util.GenUtils;
import com.sky.framework.codegen.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.sky.framework.codegen.util.TableUtils.*;

/**
 * @author
 */
@Slf4j
public class GenCore {
    /**
     * 生成代码
     *
     * @throws Exception
     */
    public static void generatorCode() {
        String tableName = PropertyUtils.generatorConfig.getString("tableName");
        List<String> tables = ("*".equals(tableName) || StringUtils.isBlank(tableName)) ? getTables() : Arrays.asList(tableName.split(","));
        CodegenEntity codegen = buildCodegenEntity();
        for (String name : tables) {
            codegen.setTableName(name);
            generatorCode(codegen);
        }
        log.info("代码生成完毕,目录位置:{}", PropertyUtils.outputPath);
    }

    /**
     * 创建codegen实体
     *
     * @return
     */
    private static CodegenEntity buildCodegenEntity() {
        CodegenEntity codegen = new CodegenEntity();
        codegen.setAuthor(PropertyUtils.generatorConfig.getString("author"));
        codegen.setModuleName(PropertyUtils.generatorConfig.getString("moduleName"));
        codegen.setPackageName(PropertyUtils.generatorConfig.getString("package"));
        codegen.setRemark(PropertyUtils.generatorConfig.getString("remark"));
        codegen.setTablePrefix(PropertyUtils.generatorConfig.getString("tablePrefix"));
        return codegen;
    }

    /**
     * 生成代码
     *
     * @param genConfig
     * @throws Exception
     */
    public static void generatorCode(CodegenEntity genConfig) {
        try {
            //查询表信息
            Map<String, String> table = getTableInfo(genConfig.getTableName());
            if (table == null) {
                throw new RuntimeException("数据源没有此表");
            }
            //查询列信息
            List<Map<String, String>> columns = getTableColumns(genConfig.getTableName());
            //生成代码
            GenUtils.generatorCode(genConfig, table, columns);
        } catch (Exception e) {
            log.error("generatorCode exception:{}", e.getMessage(), e);
        }
    }

    /**
     * 关闭流
     *
     * @param os
     * @param is
     * @param out
     */
    private static void close(ByteArrayOutputStream os, InputStream is, OutputStream out) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            log.error("close os exception:{}", e.getMessage(), e);
        }
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            log.error("close is exception:{}", e.getMessage(), e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            log.error("close out exception:{}", e.getMessage(), e);
        }
    }
}
