package com.sky.framework.codegen.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.sky.framework.codegen.enums.DALTypeEnum;
import com.sky.framework.codegen.model.CodegenEntity;
import com.sky.framework.codegen.model.ColumnEntity;
import com.sky.framework.codegen.model.TableEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.util.*;

/**
 * 代码生成器工具类
 *
 * @author
 */
@Slf4j
public class GenUtils {

    private static final String ENTITY_JAVA_VM = "Entity.java.vm";
    private static final String MAPPER_JAVA_VM = "Mapper.java.vm";
    private static final String SERVICE_JAVA_VM = "Service.java.vm";
    private static final String SERVICE_IMPL_JAVA_VM = "ServiceImpl.java.vm";
    private static final String CONTROLLER_JAVA_VM = "Controller.java.vm";
    private static final String MAPPER_XML_VM = "Mapper.xml.vm";

    private static List<String> getTemplates() {
        List<String> templates = new ArrayList<>();
        String dalValue = DALTypeEnum.acquire(PropertyUtils.generatorConfig.getInt("dalType")).getValue();

        templates.add("template/" + dalValue + "/Entity.java.vm");
        templates.add("template/" + dalValue + "/Mapper.java.vm");
        templates.add("template/" + dalValue + "/Mapper.xml.vm");
        templates.add("template/" + dalValue + "/Service.java.vm");
        templates.add("template/" + dalValue + "/ServiceImpl.java.vm");

        templates.add("template/Controller.java.vm");
        return templates;
    }

    /**
     * 生成代码
     *
     * @param genConfig
     * @param table
     * @param columns
     */
    public static void generatorCode(CodegenEntity genConfig, Map<String, String> table, List<Map<String, String>> columns) {
        Configuration config = PropertyUtils.generatorConfig;
        boolean hasBigDecimal = false;
        boolean hasDate = false;
        boolean hasDatetime = false;
        boolean hasTimestamp = false;
        boolean hasUtilDate = false;
        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        if (StrUtil.isNotBlank(genConfig.getRemark())) {
            tableEntity.setComments(genConfig.getRemark());
        } else {
            tableEntity.setComments(table.get("tableComment"));
        }

        String tablePrefix = genConfig.getTablePrefix();
        if (StrUtil.isBlank(genConfig.getTablePrefix())) {
            tablePrefix = config.getString("tablePrefix");
        }

        String entityNameSuffix = genConfig.getEntityNameSuffix();
        if (StrUtil.isBlank(entityNameSuffix)) {
            entityNameSuffix = config.getString("entityNameSuffix");
        }

        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), tablePrefix);
        tableEntity.setCaseClassName(className);
        tableEntity.setEntityName(className + entityNameSuffix);
        tableEntity.setLowerClassName(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columnList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setCaseAttrName(attrName);
            columnEntity.setLowerAttrName(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);
            String entityAttrType = attrType.substring(attrType.lastIndexOf(".") + 1);
            columnEntity.setEntityAttrType(entityAttrType);
            if (!hasBigDecimal && "java.math.BigDecimal".equals(attrType)) {
                hasBigDecimal = true;
            }
            if (!hasDate && "java.sql.Date".equals(attrType)) {
                hasDate = true;
            }
//			if (!hasDatetime && "java.sql.Timestamp".equals(attrType)) {
//				hasDatetime = true;
//			}
            if (!hasTimestamp && "java.sql.Timestamp".equals(attrType)) {
                hasTimestamp = true;
            }

            if (!hasTimestamp && "java.util.Date".equals(attrType)) {
                hasUtilDate = true;
            }
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            columnList.add(columnEntity);
        }
        tableEntity.setColumns(columnList);
        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        //封装模板数据
        Map<String, Object> map = new HashMap<>(16);
        map.put("tableName", tableEntity.getTableName());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getCaseClassName());
        map.put("classname", tableEntity.getLowerClassName());
        map.put("entityName", tableEntity.getEntityName());
        map.put("pathName", tableEntity.getLowerClassName().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("hasDate", hasDate);
        map.put("hasDatetime", hasDatetime);
        map.put("hasTimestamp", hasTimestamp);
        map.put("hasUtilDate", hasUtilDate);
        map.put("datetime", DateUtil.now());

        if (StrUtil.isNotBlank(genConfig.getRemark())) {
            map.put("comments", genConfig.getRemark());
        } else {
            map.put("comments", tableEntity.getComments());
        }
        if (StrUtil.isNotBlank(genConfig.getAuthor())) {
            map.put("author", genConfig.getAuthor());
        } else {
            map.put("author", config.getString("author"));
        }
        if (StrUtil.isNotBlank(genConfig.getModuleName())) {
            map.put("moduleName", genConfig.getModuleName());
        } else {
            map.put("moduleName", config.getString("moduleName"));
        }
        if (StrUtil.isNotBlank(genConfig.getPackageName())) {
            map.put("package", genConfig.getPackageName());
        } else {
            map.put("package", config.getString("package"));
        }

        VelocityContext context = new VelocityContext(map);
        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, CharsetUtil.UTF_8);
            tpl.merge(context, sw);
            try {
                String aPackage = map.get("package").toString();
                String moduleName = map.get("moduleName").toString();
                String entityName = tableEntity.getEntityName();
                String fileName = getFileName(template, className, entityName, aPackage, moduleName);
                writeFile(fileName, sw);
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 将文件输出
     *
     * @param fileName
     * @param sw
     * @throws IOException
     */
    private static void writeFile(String fileName, StringWriter sw) throws IOException {
        InputStream is = null;
        OutputStream out = null;
        try {
            String pathName = PropertyUtils.outputPath + fileName;
            //创建目录
            createDirs(pathName);

            File file = new File(pathName);
            out = new FileOutputStream(file);
            is = new ByteArrayInputStream(sw.toString().getBytes("UTF-8"));
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } finally {
            IoUtil.close(is);
            IoUtil.close(out);
            IoUtil.close(sw);
        }
    }

    private static void createDirs(String fileName) {
        String path = fileName.substring(0, fileName.lastIndexOf(File.separator));
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    /**
     * 列名转换成Java属性名
     */
    private static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    private static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取文件名
     */
    private static String getFileName(String template, String className, String entityName, String packageName, String moduleName) {
        String packagePath = PropertyUtils.generatorConfig.getString("projectName") + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }
        if (template.contains(ENTITY_JAVA_VM)) {
            return packagePath + "model" + File.separator + "entity" + File.separator + entityName + ".java";
        }
        if (template.contains(MAPPER_JAVA_VM)) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }
        if (template.contains(SERVICE_JAVA_VM)) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }
        if (template.contains(SERVICE_IMPL_JAVA_VM)) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }
        if (template.contains(MAPPER_XML_VM)) {
            return PropertyUtils.generatorConfig.getString("projectName") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + className + "Mapper.xml";
        }
        if (template.contains(CONTROLLER_JAVA_VM)) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }
        return null;
    }
}
