# 自定义封装的Web Shiro策略模板
1. 实现Redis缓存和Session管理
2. 实现登录失败次数超限，锁定用户一定时间
3. 实现同一用户多会话限制和踢出策略（踢出最先登录或最后登录会话）

使用方法：
1. application.yml增加ShiroProperties和Redis属性配置
2. 复制资源目录下的fastjson.properties属性文件到项目资源目录下
3. 参照资源目录下sql文件创建数据表
4. 实现ShiroService服务类（用于获取用户认证和授权等相关数据）（可参照src下的ShiroServiceImplDemo、ShiroDaoDemo和资源目录下的Mybatis内的mapper）
5. 继承AbstractShiroConfig配置类（参照src目录下的ShiroConfigDemo.java）
6. Main主类需添加@EnableAspectJAutoProxy注解
