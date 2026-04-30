import axios from 'axios';
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus';

// API 基础配置
const API_URL = '/api';
let token = getToken();

/**
 * 统一处理API请求错误
 * @param {Error} error - 错误对象
 * @param {string} serviceName - 服务名称，用于日志记录
 * @param {string} defaultErrorMsg - 默认错误消息
 * @returns {Object} 包含错误信息的对象
 */
const handleApiError = (error, serviceName, defaultErrorMsg = '请求失败') => {
  let errorMsg = defaultErrorMsg;
  
  if (error.response && error.response.data) {
    errorMsg = error.response.data.msg || defaultErrorMsg;
    console.error(`API错误详情 [${serviceName}]:`, error.response.data);
  } else if (error.message) {
    errorMsg = `${defaultErrorMsg}: ${error.message}`;
  }
  
  ElMessage.error(errorMsg);
  console.error(`API错误 [${serviceName}]:`, error);
  
  return {
    code: error.response?.status || 500,
    msg: errorMsg,
    success: false
  };
};

/**
 * 统一处理API响应
 * @param {Object} response - 响应对象
 * @param {string} serviceName - 服务名称，用于日志记录
 * @param {string} successMsg - 成功消息，如果为null则不显示消息
 * @returns {Object} 处理后的响应对象
 */
const handleApiResponse = (response, serviceName, successMsg = null) => {
  if (!response || !response.data) {
    console.error(`API响应无效 [${serviceName}]:`, response);
    return {
      code: 500,
      msg: '服务器响应无效',
      success: false
    };
  }

  if (response.data.code === 200) {
    if (successMsg) {
      ElMessage.success(successMsg);
    }
    return response.data;
  } else {
    ElMessage.error(response.data.msg || '操作失败');
    console.error(`API请求失败 [${serviceName}]:`, response.data);
    return {
      ...response.data,
      success: false
    };
  }
};

/**
 * 用户认证相关接口
 */
// 账号密码登录
export const login = async (phone, password) => {
  try {
    const response = await axios.post(`${API_URL}/user/login`, {
      phone,
      password,
    });
    
    // 确保登录成功后正确处理响应
    if (response.data && response.data.code === 200 && response.data.data.token) {
      // 重新获取token
      const newToken = response.data.data.token;
      // 更新全局token
      token = newToken;
      // 存储token到localStorage
      localStorage.setItem('token', newToken);
    }
    
    return handleApiResponse(response, 'login', '登录成功');
  } catch (error) {
    return handleApiError(error, 'login', '登录失败');
  }
};

// 发送验证码
export const sendCaptcha = async (phone) => {
  try {
    const response = await axios.post(`${API_URL}/user/sendCaptcha`, null, {
      params: { phone }
    });
    return handleApiResponse(response, 'sendCaptcha', '验证码发送成功');
  } catch (error) {
    return handleApiError(error, 'sendCaptcha', '验证码发送失败');
  }
};

// 验证码登录
export const loginByCaptcha = async (phone, captcha) => {
  try {
    const response = await axios.post(`${API_URL}/user/login2`, {
      phone,
      captcha
    });
    
    // 确保登录成功后正确处理响应
    if (response.data && response.data.code === 200 && response.data.data.token) {
      // 重新获取token
      const newToken = response.data.data.token;
      // 更新全局token
      token = newToken;
      // 存储token到localStorage
      localStorage.setItem('token', newToken);
    }
    
    return handleApiResponse(response, 'loginByCaptcha', '登录成功');
  } catch (error) {
    return handleApiError(error, 'loginByCaptcha', '登录失败');
  }
};

// 用户注册
export const register = async (userData) => {
  try {
    const response = await axios.post(`${API_URL}/user/register`, userData);
    return handleApiResponse(response, 'register', '注册成功');
  } catch (error) {
    return handleApiError(error, 'register', '注册失败');
  }
};

// 用户登出
export const logout = async () => {
  try {
    const response = await axios.delete(`${API_URL}/user/logout`, {
      headers: { token }
    });
    return handleApiResponse(response, 'logout', '登出成功');
  } catch (error) {
    return handleApiError(error, 'logout', '登出失败');
  }
};

/**
 * 检查相关接口
 */
// 列表
export const checkList = async (current = 1, size = 10, filters = {}) => {
  try {
    // 构建查询参数
    const queryParams = {
      current,
      size,
      ...(filters.duty && { duty: filters.duty }),
      ...(filters.place && { place: filters.place }),
      ...(filters.score !== undefined && filters.score !== null && { score: filters.score }),
      ...(filters.startTime && { startTime: filters.startTime }),
      ...(filters.endTime && { endTime: filters.endTime })
    };

    console.log("检查查询参数:", queryParams);

    const response = await axios.get(`${API_URL}/check/list`, {
      headers: { token },
      params: queryParams,
    });
    
    return response.data;
  } catch (error) {
    console.error("检查列表请求错误:", error.response || error);
    return handleApiError(error, 'checkList', '获取检查列表失败');
  }
};

// 删除
export const checkDelete = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/check/delete`, {
      headers: { token },
      params: { id },
    });
    return handleApiResponse(response, 'checkDelete', '删除成功');
  } catch (error) {
    return handleApiError(error, 'checkDelete', '删除失败');
  }
};

//详情
export const checkDetail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/check/detail`, {
      headers: { token },
      params: { id },
    });
    return response.data;
  } catch (error) {
    return handleApiError(error, 'checkDetail', '获取详情失败');
  }
};

//更新
export const checkUpdate = async (data) => {
  try {
    const response = await axios.put(
      `${API_URL}/check/modify/${data.id}`,
      data,
      {
        headers: { token },
      }
    );
    return handleApiResponse(response, 'checkUpdate', '更新成功');
  } catch (error) {
    return handleApiError(error, 'checkUpdate', '更新失败');
  }
};

//插入
export const checkAdd = async (data) => {
  try {
    const response = await axios.post(
      `${API_URL}/check/add`, 
      data,
      {
        headers: { token },
      }
    );
    return handleApiResponse(response, 'checkAdd', '添加成功');
  } catch (error) {
    return handleApiError(error, 'checkAdd', '添加失败');
  }
};

/**
 * 地点相关接口
 */
// 列表
export const placeList = async (current = 1, size = 10, filters = {}) => {
  try {
    // 构建查询参数
    const queryParams = {
      current,
      size,
      ...(filters.duty && { duty: filters.duty }),
      ...(filters.province && { province: filters.province }),
      ...(filters.town && { town: filters.town }),
      ...(filters.area && { area: filters.area }),
      ...(filters.status !== undefined && filters.status !== null && { status: filters.status }),
    };

    const response = await axios.get(`${API_URL}/place/list`, {
      headers: { token },
      params: queryParams,
    });

    console.log('请求成功:', response.data);
    return response.data;
  } catch (error) {
    return handleApiError(error, 'placeList', '获取地点列表失败');
  }
};

// 添加地点
export const placeAdd = async (data) => {
  try {
    const response = await axios.post(`${API_URL}/place/add`, data, {
      headers: { token }
    });
    return handleApiResponse(response, 'placeAdd', '添加地点成功');
  } catch (error) {
    return handleApiError(error, 'placeAdd', '添加地点失败');
  }
};

// 删除
export const placeDelete = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/place/delete`, {
      headers: { token },
      params: { id },
    });
    return handleApiResponse(response, 'placeDelete', '删除成功');
  } catch (error) {
    return handleApiError(error, 'placeDelete', '删除失败');
  }
};

//详情
export const placeDetail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/place/id/${id}`, {
      headers: { token },
    });
    return response.data;
  } catch (error) {
    return handleApiError(error, 'placeDetail', '获取地点详情失败');
  }
};

//更新
export const placeUpdate = async (data) => {
  try {
    const response = await axios.put(
      `${API_URL}/place/update/${data.id}`,
      data,
      {
        headers: { token },
      }
    );
    return handleApiResponse(response, 'placeUpdate', '更新地点成功');
  } catch (error) {
    return handleApiError(error, 'placeUpdate', '更新地点失败');
  }
};

//获取地址
export const getPlace = async () => {
  try {
    const response = await axios.get(
      `${API_URL}/place/get-place`,
      {
        headers: { token }
      }
    );
    return response.data;
  } catch (error) {
    return handleApiError(error, 'getPlace', '获取地点列表失败');
  }
};

/**
 * 设备相关接口
 */
// 列表
export const deviceList = async (current = 1, size = 10, filters = {}) => {
  try {
    // 构建查询参数
    const queryParams = {
      current,
      size,
      ...(filters.duty && { duty: filters.duty }),
      ...(filters.place_id && { place_id: filters.place_id }),
      ...(filters.status !== undefined && filters.status !== null && { status: filters.status }),
      ...(filters.startTime && { startTime: filters.startTime }),
      ...(filters.endTime && { endTime: filters.endTime })
    };

    console.log("设备查询参数:", queryParams);

    const response = await axios.get(`${API_URL}/device/list`, {
      headers: { token },
      params: queryParams,
    });

    return response.data;
  } catch (error) {
    console.error("设备列表请求错误:", error.response || error);
    return handleApiError(error, 'deviceList', '获取设备列表失败');
  }
};

// 添加设备
export const deviceAdd = async (data) => {
  try {
    const response = await axios.post(`${API_URL}/device/add`, data, {
      headers: { token }
    });
    return handleApiResponse(response, 'deviceAdd', '添加设备成功');
  } catch (error) {
    return handleApiError(error, 'deviceAdd', '添加设备失败');
  }
};

// 删除
export const deviceDelete = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/device/delete`, {
      headers: { token },
      params: { id },
    });
    return handleApiResponse(response, 'deviceDelete', '删除成功');
  } catch (error) {
    return handleApiError(error, 'deviceDelete', '删除失败');
  }
};

//详情
export const deviceDetail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/device/detail`, {
      headers: { token },
      params: { id }
    });
    return response.data;
  } catch (error) {
    return handleApiError(error, 'deviceDetail', '获取设备详情失败');
  }
};

//更新
export const deviceUpdate = async (data) => {
  try {
    const response = await axios.put(
      `${API_URL}/device/modify/${data.id}`,
      data,
      {
        headers: { token },
      }
    );
    return handleApiResponse(response, 'deviceUpdate', '更新设备成功');
  } catch (error) {
    return handleApiError(error, 'deviceUpdate', '更新设备失败');
  }
};

/**
 * 文件模块
 */
// 上传文件
export const uploadFile = async (file) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    
    const response = await axios.post(`${API_URL}/file/upload`, formData, {
      headers: { 
        token,
        'Content-Type': 'multipart/form-data' 
      },
    });
    
    return handleApiResponse(response, 'uploadFile', '文件上传成功');
  } catch (error) {
    return handleApiError(error, 'uploadFile', '文件上传失败');
  }
};

export const queryAddress = async (id) => {
  if (!id || isNaN(id)) {
    console.error("queryAddress 调用时 id 非法:", id);
    return { status: false, msg: "id 非法" };
  }

  try {
    const response = await axios.get(`${API_URL}/file/query-address`, {
      headers: { "token": getToken() },
      params: { id: Number(id) }
    });

    return handleApiResponse(response, 'queryAddress');
  } catch (error) {
    return handleApiError(error, 'queryAddress');
  }
};


export const queryThumbAddress = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/file/query-thumb-address`, {
      headers: { token },
      params: { id }
    });

    return handleApiResponse(response, 'queryThumbAddress', null, '获取缩略图地址失败');
  } catch (error) {
    return handleApiError(error, 'queryThumbAddress', '获取缩略图地址失败');
  }
};

/**
 * 用户信息相关接口
 */
// 获取用户信息
export const getUserInfo = async () => {
  try {
    const response = await axios.get(`${API_URL}/user/query-detail`, {
      headers: { token }
    });
    return handleApiResponse(response, 'getUserInfo');
  } catch (error) {
    return handleApiError(error, 'getUserInfo', '获取用户信息失败');
  }
};

// 更新用户信息
export const updateUserInfo = async (userData) => {
  try {
    const response = await axios.put(`${API_URL}/user/update-info`, userData, {
      headers: { token }
    });
    return handleApiResponse(response, 'updateUserInfo', '更新用户信息成功');
  } catch (error) {
    return handleApiError(error, 'updateUserInfo', '更新用户信息失败');
  }
};

// 上传用户头像
export const uploadUserAvatar = async (formData) => {
  try {
    const response = await axios.post(`${API_URL}/user/upload-avatar`, formData, {
      headers: { 
        token,
        'Content-Type': 'multipart/form-data'
      }
    });
    return handleApiResponse(response, 'uploadUserAvatar', '上传头像成功');
  } catch (error) {
    return handleApiError(error, 'uploadUserAvatar', '上传头像失败');
  }
};

/**
 * 用户设置相关接口
 */
// 修改密码
export const changePassword = async (oldPassword, newPassword, confirmPassword) => {
  try {
    const response = await axios.put(`${API_URL}/user/edit-password`, {
      oldPassword,
      newPassword,
      confirmPassword
    }, {
      headers: { token }
    });
    return handleApiResponse(response, 'changePassword', '修改密码成功');
  } catch (error) {
    return handleApiError(error, 'changePassword', '修改密码失败');
  }
};

/**
 * 视频相关接口
 */
// 上传视频
export const uploadVideo = async (formData) => {
    try {
        const response = await axios.post(`${API_URL}/video/upload`, formData, {
            headers: { 
                token,
                'Content-Type': 'multipart/form-data'
            },
            timeout: 600000 // 设置超时时间为10分钟，因为视频上传可能耗时较长
        });
        
        if (response.data.code !== 200) {
            ElMessage.error(response.data.msg || "上传视频失败");
            return null;
        }
        
        return response.data;
    } catch (error) {
        ElMessage.error("请求失败，请检查网络连接");
        console.error('API请求错误:', error);
        return null;
    }
};

// 触发视频处理
export const processVideo = async (videoId, placeId) => {
    try {
        const response = await axios.post(`${API_URL}/video/process`, null, {
            params: {
                videoId,
                placeId
            },
            headers: { token }
        });
        
        if (response.data.code !== 200) {
            ElMessage.error(response.data.msg || "触发视频处理失败");
            return null;
        }
        
        return response.data;
    } catch (error) {
        ElMessage.error("请求失败，请检查网络连接");
        console.error('API请求错误:', error);
        return null;
    }
};

// 获取视频列表
export const getVideoList = async (current = 1, size = 10) => {
  try {
    const response = await axios.get(`${API_URL}/video/list`, {
      headers: { token },
      params: { current, size }
    });
    return handleApiResponse(response, 'getVideoList', null, '获取视频列表失败');
  } catch (error) {
    return handleApiError(error, 'getVideoList', '获取视频列表失败');
  }
};

// 获取视频详情
export const getVideoDetail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/video/detail`, {
      headers: { token },
      params: { id }
    });
    return handleApiResponse(response, 'getVideoDetail', null, '获取视频详情失败');
  } catch (error) {
    return handleApiError(error, 'getVideoDetail', '获取视频详情失败');
  }
};

// 获取视频缩略图
export const getVideoThumbnail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/video/thumbnail`, {
      headers: { token },
      params: { id }
    });
    return handleApiResponse(response, 'getVideoThumbnail', null, '获取视频缩略图失败');
  } catch (error) {
    return handleApiError(error, 'getVideoThumbnail', '获取视频缩略图失败');
  }
};

/**
 * 火灾记录相关接口
 */
// 获取火灾记录列表
export const fireList = async (current = 1, size = 10, params = {}) => {
  try {
    const requestParams = {
      current,
      size,
      ...params
    };

    const response = await axios.get(`${API_URL}/fire/list`, {
      headers: { token },
      params: requestParams,
    });

    return response.data;
  } catch (error) {
    return handleApiError(error, 'fireList', '获取火灾记录列表失败');
  }
};

// 条件查询火灾记录
export const queryFireRecords = async (queryParam) => {
  try {
    const response = await axios.post(`${API_URL}/fire/query`, queryParam, {
      headers: { token }
    });
    
    return response.data;
  } catch (error) {
    return handleApiError(error, 'queryFireRecords', '查询火灾记录失败');
  }
};

// 获取火灾记录详情
export const getFireDetail = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/fire/id/${id}`, {
      headers: { token }
    });
    
    return response.data;
  } catch (error) {
    return handleApiError(error, 'getFireDetail', '获取火灾记录详情失败');
  }
};

// 更新火灾记录
export const updateFire = async (id, data) => {
  try {
    const response = await axios.put(`${API_URL}/fire/update/${id}`, data, {
      headers: { token }
    });
    
    return handleApiResponse(response, 'updateFire', '更新火灾记录成功');
  } catch (error) {
    return handleApiError(error, 'updateFire', '更新火灾记录失败');
  }
};

// 删除火灾记录
export const deleteFire = async (id) => {
  try {
    const response = await axios.delete(`${API_URL}/fire/delete`, {
      headers: { token },
      params: { id }
    });
    
    return handleApiResponse(response, 'deleteFire', '删除火灾记录成功');
  } catch (error) {
    return handleApiError(error, 'deleteFire', '删除火灾记录失败');
  }
};

// 添加火灾记录
export const addFire = async (data) => {
  try {
    const response = await axios.post(`${API_URL}/fire/add`, data, {
      headers: { token }
    });
    
    return handleApiResponse(response, 'addFire', '添加火灾记录成功');
  } catch (error) {
    return handleApiError(error, 'addFire', '添加火灾记录失败');
  }
};

// 获取火灾记录对应的视频信息
export const getFireVideo = async (fireId) => {
  try {
    const response = await axios.get(`${API_URL}/fire/video/${fireId}`, {
      headers: { token }
    });
    return response.data;
  } catch (error) {
    return handleApiError(error, 'getFireVideo', '获取视频信息失败');
  }
};

// 获取火灾记录对应的视频文件
export const getFireVideoFile = async (fireId) => {
  try {
    const response = await axios.get(`${API_URL}/fire/video/file/${fireId}`, {
      headers: { token },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    return handleApiError(error, 'getFireVideoFile', '获取视频文件失败');
  }
};

/**
 * 统计数据相关接口
 */
// 获取检查记录统计数据
export const getCheckStats = async (startTime, endTime) => {
  try {
    const params = {};
    if (startTime) {
      params.startTime = startTime;
    }
    if (endTime) {
      params.endTime = endTime;
    }

    const response = await axios.get(`${API_URL}/stats/check`, {
      headers: { token },
      params
    });
    return handleApiResponse(response, 'getCheckStats', null, '获取检查记录统计数据失败');
  } catch (error) {
    return handleApiError(error, 'getCheckStats', '获取检查记录统计数据失败');
  }
};

// 获取设备统计数据
export const getDeviceStats = async () => {
  try {
    const response = await axios.get(`${API_URL}/stats/device`, {
      headers: { token }
    });
    return handleApiResponse(response, 'getDeviceStats', null, '获取设备统计数据失败');
  } catch (error) {
    return handleApiError(error, 'getDeviceStats', '获取设备统计数据失败');
  }
};

// 获取火灾记录统计数据
export const getFireStats = async (startTime, endTime) => {
  try {
    const params = {};
    if (startTime) {
      params.startTime = startTime;
    }
    if (endTime) {
      params.endTime = endTime;
    }

    const response = await axios.get(`${API_URL}/stats/fire`, {
      headers: { token },
      params
    });
    return handleApiResponse(response, 'getFireStats', null, '获取火灾记录统计数据失败');
  } catch (error) {
    return handleApiError(error, 'getFireStats', '获取火灾记录统计数据失败');
  }
};

// 获取地点统计数据
export const getPlaceStats = async () => {
  try {
    const response = await axios.get(`${API_URL}/stats/place`, {
      headers: { token }
    });
    return handleApiResponse(response, 'getPlaceStats', null, '获取地点统计数据失败');
  } catch (error) {
    return handleApiError(error, 'getPlaceStats', '获取地点统计数据失败');
  }
};

// 获取地点设备数量统计
export const getPlaceDeviceStats = async () => {
  try {
    console.log('请求地点设备数量统计...');
    const response = await axios.get(`${API_URL}/stats/place-device`, {
      headers: { token }
    });
    console.log('地点设备数量统计响应:', response.data);
    return handleApiResponse(response, 'getPlaceDeviceStats', null, '获取地点设备数量统计失败');
  } catch (error) {
    console.error('地点设备数量统计请求异常:', error);
    return handleApiError(error, 'getPlaceDeviceStats', '获取地点设备数量统计失败');
  }
};

// 获取地点检查次数统计
export const getPlaceCheckStats = async () => {
  try {
    console.log('请求地点检查次数统计...');
    const response = await axios.get(`${API_URL}/stats/place-check`, {
      headers: { token }
    });
    console.log('地点检查次数统计响应:', response.data);
    return handleApiResponse(response, 'getPlaceCheckStats', null, '获取地点检查次数统计失败');
  } catch (error) {
    console.error('地点检查次数统计请求异常:', error);
    return handleApiError(error, 'getPlaceCheckStats', '获取地点检查次数统计失败');
  }
};

/**
 * 视频检测相关接口
 */
// 发送视频帧进行目标检测
export const detectVideoFrame = async (imageBase64) => {
  try {
    const response = await axios.post(`http://localhost:5000/api/detect`, {
      image: imageBase64
    }, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 5000 // 缩短超时时间为5秒，以便更快触发错误处理
    });
    
    if (response.status === 200) {
      return response.data;
    } else {
      console.error("视频检测请求失败:", response);
      return {
        success: false,
        error: '视频检测请求失败',
        data: []
      };
    }
  } catch (error) {
    // 详细记录错误信息
    if (error.code === 'ECONNABORTED') {
      console.error("视频检测请求超时");
    } else if (error.message && error.message.includes('Network Error')) {
      console.error("网络错误，可能后端服务未启动");
    } else {
      console.error("视频检测请求错误:", error);
    }
    
    return {
      success: false,
      error: error.message || '视频帧检测失败',
      data: []
    };
  }
};

// 获取检测模型配置
export const getDetectModelConfig = async () => {
  try {
    const response = await axios.get(`http://localhost:5000/api/model/config`, {
      timeout: 5000
    });
    return response.data;
  } catch (error) {
    console.error("获取检测模型配置失败:", error);
    return {
      success: false,
      error: error.message || '获取检测模型配置失败'
    };
  }
};

// 切换检测模型（后端会更新配置文件中的模型路径）
export const switchDetectModel = async (modelName) => {
  try {
    const response = await axios.post(`http://localhost:5000/api/model/switch`, {
      model_name: modelName
    }, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 10000
    });
    return response.data;
  } catch (error) {
    console.error("切换检测模型失败:", error);
    return {
      success: false,
      error: error.message || '切换检测模型失败'
    };
  }
};

// 实时检测触发告警（由监控页调用）
export const sendRealtimeAlert = async (payload) => {
  try {
    token = getToken();
    const response = await axios.post(`${API_URL}/alert/realtime`, payload, {
      headers: { token }
    });
    return handleApiResponse(response, "sendRealtimeAlert", null);
  } catch (error) {
    return handleApiError(error, "sendRealtimeAlert", "发送实时告警失败");
  }
};
