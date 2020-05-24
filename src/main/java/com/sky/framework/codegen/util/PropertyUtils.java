package com.sky.framework.codegen.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

/**
 * 属性文件加载工具
 *
 * @author
 */
public class PropertyUtils {

    public static Configuration datasourceConfig;

    public static Configuration generatorConfig;

    public static String outputPath;

    static {
        datasourceConfig = getDatasourceConfig();
        generatorConfig = getGeneratorConfig();
        outputPath = generatorConfig.getString("outputPath");
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
    }

    /**
     * 获取数据库配置信息
     */
    private static Configuration getDatasourceConfig() {
        try {
            return new PropertiesConfiguration("database.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取数据库配置文件失败，", e);
        }
    }

    /**
     * 获取配置信息
     */
    private static Configuration getGeneratorConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

}
