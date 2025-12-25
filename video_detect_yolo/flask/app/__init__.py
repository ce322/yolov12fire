from flask import Flask
from flask_cors import CORS

def create_app():
    app = Flask(__name__)
    
    # 启用CORS跨域支持，允许前端访问
    CORS(app, resources={r"/api/*": {"origins": "*"}})
    
    # 注册API蓝图
    from .api import api_bp
    app.register_blueprint(api_bp, url_prefix='/api')
    
    return app 