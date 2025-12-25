import os
import sys
import base64
import io
import json
import time
from flask import Blueprint, request, jsonify
from PIL import Image
import numpy as np

# 创建API蓝图
api_bp = Blueprint('api', __name__)

# 获取模型路径
current_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(current_dir, '..', 'models', 'best12.pt')

# 全局变量，用于存储模型实例
yolo_model = None

def load_model():
    """延迟加载YOLO模型，避免启动时间过长"""
    global yolo_model
    if yolo_model is None:
        try:
            # 将上级目录加入路径，以便导入ultralytics模块
            yolo_path = os.path.join(current_dir, '..', '..', 'ultralytics-main')
            if yolo_path not in sys.path:
                sys.path.append(yolo_path)
            
            # 导入YOLO
            from ultralytics import YOLO
            
            # 加载模型
            yolo_model = YOLO(model_path)
            print("🔥 YOLO actually loaded from:", yolo_model.ckpt_path)
            print(f"成功加载模型: {model_path}")
        except Exception as e:
            print(f"加载模型失败: {str(e)}")
            return False
    return True

@api_bp.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({
        "status": "ok",
        "timestamp": time.time(),
        "service": "video-detect-flask",
        "model_path": model_path,
        "model_loaded": yolo_model is not None
    })

@api_bp.route('/detect', methods=['POST'])
def detect():
    print("🔥 detect() called")  # ← 加这一行
    """图像检测接口"""
    # 检查请求
    if not request.is_json:
        return jsonify({"error": "仅接受JSON格式请求"}), 400
    
    # 获取图像数据
    data = request.get_json()
    if 'image' not in data:
        return jsonify({"error": "请求中未找到图像数据"}), 400
    
    # 解码Base64图像
    try:
        image_data = base64.b64decode(data['image'])
        image = Image.open(io.BytesIO(image_data))
    except Exception as e:
        return jsonify({"error": f"图像解码失败: {str(e)}"}), 400
    
    # 加载模型（如果尚未加载）
    if not load_model():
        return jsonify({"error": "模型加载失败"}), 500
    
    # 执行检测
    try:
        results = yolo_model(image)
        
        # 处理检测结果
        detections = []
        for result in results:
            # 获取边界框
            for box, cls, conf in zip(result.boxes.xyxy.cpu().numpy(), 
                                      result.boxes.cls.cpu().numpy(),
                                      result.boxes.conf.cpu().numpy()):
                x1, y1, x2, y2 = map(int, box)
                class_id = int(cls)
                confidence = float(conf)
                
                # 获取类别名称
                class_name = result.names[class_id]
                
                # 转换为[x, y, width, height]格式，前端更容易处理
                det_box = [x1, y1, x2 - x1, y2 - y1]
                
                detections.append({
                    "box": det_box,
                    "label": class_name,
                    "confidence": confidence
                })
        
        # 返回结果
        return jsonify({
            "timestamp": time.time(),
            "detections": detections
        })
        
    except Exception as e:
        print(f"检测过程中出错: {str(e)}")
        return jsonify({"error": f"检测过程中出错: {str(e)}"}), 500 