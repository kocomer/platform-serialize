Java序列化工具类
优点：
    速度快
    传输数据少
    不依赖其他工具类

缺点：
    暂支持的类型：
    byte boolean short char int float long double String

使用：
    Object obj = new Object();
    //序列化
    byte[] data = SerializeUtils.encode(obj);
    //反序列化
    SerializeUtils.decode(data, Object.class);
    