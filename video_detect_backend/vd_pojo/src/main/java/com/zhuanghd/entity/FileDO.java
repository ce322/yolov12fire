package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * @TableName vd_file
 */
@TableName(value ="vd_file")
@Data
public class FileDO {
    private Long id;

    private String fileName;

    private Long userId;

    private String url;

    private String extension;

    private LocalDateTime uploadTime;
}