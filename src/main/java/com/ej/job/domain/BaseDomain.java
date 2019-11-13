package com.ej.job.domain;

import lombok.Data;

import java.util.Date;

@Data
public class BaseDomain {
    /** 主键 **/
    private Long id;
    /** 创建时间 **/
    private Date created;
    /** 创建人 **/
    private String creator;
    /** 修改时间 **/
    private Date modified;
    /** 修改人 **/
    private String modifier;
}
