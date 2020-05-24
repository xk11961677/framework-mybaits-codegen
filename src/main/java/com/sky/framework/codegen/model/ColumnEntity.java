package com.sky.framework.codegen.model;

import lombok.Data;

/**
 * @author
 */
@Data
public class ColumnEntity {
    /**
     * 字段名称
     */
    private String columnName;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 备注
     */
    private String comments;
    /**
     * 驼峰属性
     */
    private String caseAttrName;
    /**
     * 普通属性
     */
    private String lowerAttrName;
    /**
     * 数据库属性类型
     */
    private String attrType;
    /**
     * 实体属性类型
     */
    private String entityAttrType;
    /**
     * 其他信息。
     */
    private String extra;
}
