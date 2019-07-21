# **Seckill**
### **项目介绍：**

本项目是使用SSM框架开发的高并发限时秒杀web应用。

项目功能介绍：

    商品秒杀开启前，用户能看到商品秒杀倒计时，但不能进行秒杀。

    商品秒杀开启时，可以进行秒杀但不能进行重复秒杀。

    商品秒杀结束后，显示商品秒杀已结束，阻止用户进行秒杀。
    

### **安装：**
    1. git clone https://github.com/MENGMUZI/seckill_learning
    2. 打开IDEA --> File --> New --> Open
    3. 项目导入后，打开 Project Settings -->Project 设置 Project SDK (本项目JDK版本需在1.8以上)
    4. 打开File --> Settings --> Build,Execution,Deployment -->Maven 配置maven相关信息
    5. 在 sql 包下，执行 seckill.sql 与 execute_seckill.sql，建立数据库，然后找到 jdbc.properties 文件修改username and password