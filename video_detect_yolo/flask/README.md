# 视频目标检测 - Flask后端

该服务为视频目标检测系统提供API后端，用于接收前端发送的图像数据，并返回目标检测结果。

## 依赖项

- Python 3.7+
- Flask 2.0+
- Flask-CORS
- Pillow
- NumPy
- YOLOv8（通过video_detect_yolo/ultralytics-main目录）

## 安装

1. 安装Python依赖项：

```bash
pip install -r requirements.txt
```

2. 将YOLOv8模型文件放入`models`目录（创建该目录如果不存在）：

```
video_detect_yolo/flask/models/best8.pt
```

3. 确保ultralytics模块目录存在：

```
video_detect_yolo/ultralytics-main/
```

## 运行

```bash
python run.py
```

默认情况下，服务将在`http://0.0.0.0:5000`上运行。

### 可选参数

- `--host`: 指定监听地址（默认为0.0.0.0）
- `--port`: 指定监听端口（默认为5000）
- `--debug`: 启用调试模式

示例：

```bash
python run.py --host 127.0.0.1 --port 8000 --debug
```

## API接口

### 健康检查

- URL: `/api/health`
- 方法: `GET`
- 返回示例:
  ```json
  {
    "status": "ok",
    "timestamp": 1619712345.67,
    "service": "video-detect-flask",
    "model_path": "/path/to/models/best8.pt",
    "model_loaded": true
  }
  ```

### 图像检测

- URL: `/api/detect`
- 方法: `POST`
- 内容类型: `application/json`
- 请求体示例:
  ```json
  {
    "image": "base64编码的图像数据",
    "timestamp": 1619712345.67
  }
  ```
- 返回示例:
  ```json
  {
    "timestamp": 1619712345.67,
    "detections": [
      {
        "box": [x, y, width, height],
        "label": "person",
        "confidence": 0.95
      },
      ...
    ]
  }
  ```

## 与前端集成

该服务设计为与`video_detect_front`前端一起使用，前端通过MonitorPage页面发送图像数据到该服务。 