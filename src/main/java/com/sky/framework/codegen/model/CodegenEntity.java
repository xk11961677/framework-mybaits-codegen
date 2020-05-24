package com.sky.framework.codegen.model;


import lombok.Data;

import java.io.Serializable;


/**
 * 代码生成器表
 *
 * @author
 */
@Data
public class CodegenEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 包名
     */
    private String packageName;
    /**
     * 模块名
     */
    private String moduleName;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 表前缀
     */
    private String tablePrefix;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 作者
     */
    private String author;
    /**
     * 备注
     */
    private String remark;
    /**
     * 实体后缀
     */
    private String entityNameSuffix;

}
