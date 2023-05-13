# Voicegender - Spring boot

Voicegender的处理后端，基于Spring boot

------
## 模型简介

| 序号 | 简称     | 详细名称                                         | 运行环境               | 说明 |
|:---|:-------|:---------------------------------------------|:-------------------|:---|
| 1  | GBT    | 梯度提升回归树(Gradient Boosted Trees) | Tensorflow-Serving |    |
| 2  | CART   | 分类回归树(Classification and Regression Tree)    | Tensorflow-Serving |    |
| 3  | RF     | 随机森林(Random Forest)                          | Tensorflow-Serving |    |
| 4  | XGBOOT | 极端梯度提升树(eXtreme Gradient Boosting)           | Jvm                |    |

------
## 数据集

### 英文数据集
* 来自Github: [voice-gender](https://github.com/primaryobjects/voice-gender) 的 [预训练数据集](https://raw.githubusercontent.com/primaryobjects/voice-gender/master/voice.csv)

### 中文数据集
* [Free ST Chinese Mandarin Corpus](https://www.openslr.org/38/)
* [A CHINESE CANTONESE (CANTON) CONVERSATIONAL SPEECH CORPUS](https://magichub.com/datasets/guangzhou-cantonese-conversational-speech-corpus/)
* [A SCRIPTED CHINESE CANTONESE (CANTON) DAILY-USE SPEECH CORPUS](https://magichub.com/datasets/guangzhou-cantonese-scripted-speech-corpus-daily-use-sentence/)

------

## 常见错误
###

**Q. 必须在有效 Spring Bean 中定义自动装配成员** 
  
A: 在IDEA设置中找到 编辑器 > 检查 > Spring > Spring Core > Autowiring for Bean Class > 选择 `弱警告` 或以下即可

**Q. Unable to determine Dialect without JDBC metadata**

A: 检查数据库是否启动，若启动，请检查地址是否配置正确

------

## 状态码

### 

#### 请求状态码

| 状态码 | 说明            |
|:----|:--------------|
| 200 | 请求成功          |
| 100 | 文件格式错误        |
| 101 | 文件大小超过限制      |
| 102 | 超过每小时请求限制     |
| 103 | 音频时长过短        |
| 104 | 音频时长过长        |
| 105 | 分析正在进行中       |
| 106 | 分析不存在(UUID无效) |
| 107 | 分析正在注册中(未开始)  |
| 400 | 请求参数错误        |
| 404 | 无效的路径         |
| 500 | 服务器内部错误       | 

### 

#### 分析状态码
| 状态码 | 说明      |
|:----|:--------|
| 110 | 等待分析    |
| 111 | 转换文件类型中 |
| 112 | 计算音频特征中 |
| 113 | 分析音频特征中 |
| 120 | 分析成功    |
| -1  | 分析失败    |

---

## 请求接口

###

###  **服务器状态**
    GET /status
**返回:**
    
    {
        "status": {
        "code": 200,
        "message": "OK"
        }
    }

###  **分析音频**
    POST /analyze
**返回:**
    
    {
	    "status": {
		    "code": 200,
		    "message": "success"
	    },
	    "analyzeUUID": "分析UUID"
    }

###  **获取分析状态**
    GET /analyze/{analyzeUUID}/status
**返回:**

    {
	    "status": {
		    "code": 200,
		    "message": "success"
        },
	    "analyzeStatus": {
		    "code": 120,
		    "message": "finished"
	    } 
    }
_状态代码参见上述章节_

###  **获取分析结果**
    GET /analyze/{analyzeUUID}/result
**返回:**
    
    {
	"status": {
		"code": 200,
		"message": "success"
	},
	"uuid": "(analyzeUUID)",
	"modelResult": {
		"GBT": {
			"MASCULINE": xxx,
			"FEMININE": xxx
		}
            ...
    }   

