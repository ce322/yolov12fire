# Video-Detect: 基于 SpringBoot 和 YOLO 的视频流数据处理和目标检测系统

![演示视频](https://gitee.com/zhuanghongdong/video-detect/raw/master/%E6%BC%94%E7%A4%BA%E8%A7%86%E9%A2%91.gif)

## 系统架构说明
本系统采用前后端分离架构，包含三大部分：

- 前端：基于Vue 3，负责用户交互与数据可视化。
- 后端：基于Spring Boot，负责业务逻辑、数据管理、权限认证等。
- 目标检测服务：基于YOLOv8与Flask，负责视频/图片的目标检测推理。

## 主要技术栈
- 前端：Vue 3、Element Plus、ECharts、Axios

- 后端：Spring Boot、MyBatis-Plus、MySQL、Redis、JWT、FFmpeg
- AI推理：YOLOv8（ultralytics）、Flask

## 主要功能
- 实时视频流目标检测与结果展示

- 设备、检查、火灾等业务数据管理
- 检测结果的可视化与统计分析
- 用户权限与安全认证
- 文件上传与管理
- 各模块启动方式

前端

```bash
cd video_detect_front
npm install
npm run serve
```

后端

```bash
cd video_detect_backend/vd_server
mvn spring-boot:run
```

目标检测服务

```bash
cd video_detect_yolo/flask
pip install -r requirements.txt
python run.py
```

数据库

```bash
使用MySQL，表结构见video_detect.sql
```