# 一、传统的连接机制与数据库连接池的运行机制区别

## 1、不使用连接池流程

下面以访问MySQL为例，执行一个SQL命令，如果不使用连接池，需要经过哪些流程。

![img](https://img-blog.csdnimg.cn/20190617220442881.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM2MDk1Njc5,size_16,color_FFFFFF,t_70)

**不使用数据库连接池的步骤：** 

> 1. TCP建立连接的三次握手
> 2. MySQL认证的三次握手
> 3. 真正的SQL执行
> 4. MySQL的关闭
> 5. TCP的四次握手关闭

**传统连接机制的缺点：**

> 1. 网络IO较多
> 2. 数据库的负载较高
> 3. 响应时间较长及QPS较低
> 4. 应用频繁的创建连接和关闭连接，导致临时对象较多，GC频繁
> 5. 在关闭连接后，会出现大量TIME_WAIT 的TCP状态（在2个MSL之后关闭）

## 2、**使用连接池流程**

![img](https://img-blog.csdnimg.cn/20190617220800965.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM2MDk1Njc5,size_16,color_FFFFFF,t_70)

### 2.1连接池的作用

> 连接池是将已经创建好的连接保存在池中，当有请求来时，直接使用已经创建好的连接对数据库进行访问。这样省略了创建连接和销毁连接的过程。这样性能上得到了提高。

### 2.1数据库连接池的工作原理

>  **第一、连接池的建立**。一般在系统初始化时，连接池会根据系统配置建立，并在池中创建了几个连接对象，以便使用时能从连接池中获取。连接池中的连接不能随意创建和关闭，这样避免了连接随意建立和关闭造成的系统开销。Java中提供了很多容器类可以方便的构建连接池，例如Vector、Stack等。
>
> **第二、连接池的管理。**连接池管理策略是连接池机制的核心，连接池内连接的分配和释放对系统的性能有很大的影响。其管理策略是：1）**当客户请求数据库连接时**，首先查看连接池中**是否有空闲连接**，**如果存在空闲连接**，则将连接分配给客户使用；**如果没有空闲连接**，则查看当前所开的连接数是否已经达到最大连接数，如果没达到就重新创建一个连接给请求的客户；如果达到就按设定的最大等待时间进行等待，如果超出最大等待时间，则抛出异常给客户。2）**当客户释放数据库连接时**，先判断该连接的引用次数**是否超过了规定值**，如果超过就从连接池中删除该连接，否则保留为其他客户服务。（该策略保证了数据库连接的有效复用，避免频繁的建立、释放连接所带来的系统资源开销）
>
> **第三、连接池的关闭。**当应用程序退出时，关闭连接池中所有的连接，释放连接池相关的资源，该过程正好与创建相反。



# 二、Druid

## **Druid的简介**

Druid首先是一个数据库连接池。Druid是目前最好的数据库连接池，在功能、性能、扩展性方面，都超过其他数据库连接池，包括DBCP、C3P0、BoneCP、Proxool、JBoss DataSource。Druid已经在阿里巴巴部署了超过600个应用，经过一年多生产环境大规模部署的严苛考验。Druid是阿里巴巴开发的号称为监控而生的数据库连接池。

同时Druid不仅仅是一个数据库连接池，它包括四个部分：

   **Druid是一个JDBC组件，它包括三个部分：**

-   基于Filter－Chain模式的插件体系。

-   DruidDataSource 高效可管理的数据库连接池。

-   SQLParser


## **Druid的功能**

- 替换DBCP和C3P0。Druid提供了一个高效、功能强大、可扩展性好的数据库连接池。
- 可以监控数据库访问性能，Druid内置提供了一个功能强大的StatFilter插件，能够详细统计SQL的执行性能，这对于线上分析数据库访问性能有帮助。

- 数据库密码加密。直接把数据库密码写在配置文件中，这是不好的行为，容易导致安全问题。DruidDruiver和DruidDataSource都支持PasswordCallback。

- SQL执行日志，Druid提供了不同的LogFilter，能够支持Common-Logging、Log4j和JdkLog，你可以按需要选择相应的LogFilter，监控你应用的数据库访问情况。

- 扩展JDBC，如果你要对JDBC层有编程的需求，可以通过Druid提供的Filter机制，很方便编写JDBC层的扩展插件。

## **Druid 相对于其他数据库连接池的优点：**

1. 强大的监控特性，通过Druid提供的监控功能，可以清楚知道连接池和SQL的工作情况。

 	a.  监控SQL的执行时间、ResultSet持有时间、返回行数、更新行数、错误次数、错误堆栈信息;

 	b.  SQL执行的耗时区间分布。什么是耗时区间分布呢？比如说，某个SQL执行了1000次，其中0~1毫秒区间50次，1~10毫秒800次，		  10~100毫秒100次，100~1000毫秒30次，1~10秒15次，10秒以上5次。通过耗时区间分布，能够非常清楚知道SQL的执行耗时情		  况

 	c.  监控连接池的物理连接创建和销毁次数、逻辑连接的申请和关闭次数、非空等待次数、PSCache命中率等。

2. 其次，方便扩展。Druid提供了Filter-Chain模式的扩展API，可以自己编写Filter拦截JDBC中的任何方法，可以在上面做任何事情，比如说性能监控、SQL审计、用户名密码加密、日志等等。

3. Druid集合了开源和商业数据库连接池的优秀特性，并结合阿里巴巴大规模苛刻生产环境的使用经验进行优化

# 三、spring boot配置druid和监控

springboot的默认数据源是org.apache.tomcat.jdbc.pool.DataSource。因为我们在这里使用的是druid，所以需要修改spring.datasource.type为druid。

####     **a.添加pom依赖**

前往https://mvnrepository.com/artifact/com.alibaba/druid-spring-boot-starter/1.1.10

```html
<!-- https://mvnrepository.com/artifact/com.alibaba/druid-spring-boot-starter -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.10</version>
</dependency>
```

####     **b.将与数据库连接的配置进行修改**

   文件名：application.yml

```html
spring:
  datasource:
 	  # 基本属性
      name: dev
      url: jdbc:mysql://127.0.0.1:3306/demo?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull&useSSL=false
      username: root
      password: 123456
      # 可以不配置，根据url自动识别，建议配置
      driver-class-name: com.mysql.jdbc.Driver
      ###################以下为druid增加的配置###########################
      type: com.alibaba.druid.pool.DruidDataSource
      # 初始化连接池个数
      initialSize: 5
      # 最小连接池个数——》已经不再使用，配置了也没效果
      minIdle: 2
      # 最大连接池个数
      maxActive: 20
      # 配置获取连接等待超时的时间，单位毫秒，缺省启用公平锁，并发效率会有所下降
      maxWait: 60000
      # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
      timeBetweenEvictionRunsMillis: 60000
      # 配置一个连接在池中最小生存的时间，单位是毫秒
      minEvictableIdleTimeMillis: 300000
      # 用来检测连接是否有效的sql，要求是一个查询语句。
      # 如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用
      validationQuery: SELECT 1 FROM DUAL
      # 建议配置为true，不影响性能，并且保证安全性。
      # 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
      testWhileIdle: true
      # 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
      testOnBorrow: false
      # 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
      testOnReturn: false
      # 打开PSCache，并且指定每个连接上PSCache的大小
      poolPreparedStatements: true
      maxPoolPreparedStatementPerConnectionSize: 20
      # 通过别名的方式配置扩展插件，多个英文逗号分隔，常用的插件有： 
      # 监控统计用的filter:stat
      # 日志用的filter:log4j
      # 防御sql注入的filter:wall
      filters: stat,wall,log4j
      # 通过connectProperties属性来打开mergeSql功能；慢SQL记录
      connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      # 合并多个DruidDataSource的监控数据
      useGlobalDataSourceStat: true
```

####     **c.配置监控统计功能**

```java
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DruidConfiguration {

    /**
     * 注册一个StatViewServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean DruidStatViewServle(){

        //org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");

        //添加初始化参数：initParams

        //白名单：
        servletRegistrationBean.addInitParameter("allow","127.0.0.1");

        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
        servletRegistrationBean.addInitParameter("deny","192.168.0.114");

        //登录查看信息的账号密码.
        servletRegistrationBean.addInitParameter("loginUsername","admin");
        servletRegistrationBean.addInitParameter("loginPassword","123456");

        //是否能够重置数据.
        servletRegistrationBean.addInitParameter("resetEnable","false");
        return servletRegistrationBean;
    }

    /**
     * 注册一个：filterRegistrationBean
     * @return
     */
    @Bean
    public FilterRegistrationBean druidStatFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());

        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns("/*");

        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return filterRegistrationBean;
    }
}
```

运行项目，然后打开http://localhost:8080/druid/login.html登录后进入下图界面即配置成功

![image-20201107001249456](C:\Users\asus\AppData\Roaming\Typora\typora-user-images\image-20201107001249456.png)

#### 可能会遇见的问题

报错

```
程序包com.alibaba.druid.support.http不存在
```

处理办法File -> Settings -> Build,Execution,Deployment -> Build Tools -> Maven -> Runner，将下图圈出部分勾选重启即可

![image-20201107000630579](C:\Users\asus\AppData\Roaming\Typora\typora-user-images\image-20201107000630579.png)

# 四、监控界面详解

结合监控页面讲解

# 五、对数据库密码加密

数据库密码直接写在配置中，对运维安全来说，是一个很大的挑战。Druid提供了一种数据库密码加密的手段ConfigFilter。

### 执行命令加密数据库密码

在cmd中跳转至druid包的文件夹内，然后在命令行中执行如下命令：

```
java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools your_password
eg:
java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools 123456
```

druid-1.1.10.jar 为jar 的包名
you_password 为你设置的初始密码

输出:

```
privateKey:MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAtSW2imSbuFV+ZrErmOQP+jQfoQtyTQ62VQIAEe+mCgelY3gexKFA8CDcbj2n9pujh79PjO0hg2NE/mASR75ROQIDAQABAkEAqKX237uxOpnl94elNk/GDERw2geFH/a9jEEzfX9nT1efzP6PiPwb4H82V6FRoWfCwSsmlbeSMb3/4LxqBlVZ8QIhAPkAUQ8EYBt953UVOS4JX42qzDDtmfOKV0oEcJ0oED6VAiEAuj0np7Q5BRHYPITPL4vylr4MgBjgqa1fXfqZ7NlLsxUCIAwW9diSz8/F5y0KIMKZdeg8+LkPcEAg4WgYmxcmUNOZAiAJfFfquMk2suP6oSEUYOJoPyHP3HSE7+mtlbgFUkQCyQIgZOUpQRGZ1CRCsRf+4yJmV4wtUrmz+YhBdT851lwuoCU=
publicKey:MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALUltopkm7hVfmaxK5jkD/o0H6ELck0OtlUCABHvpgoHpWN4HsShQPAg3G49p/abo4e/T4ztIYNjRP5gEke+UTkCAwEAAQ==
password:bN2OreHMM6RtqaK87h91p/Ea9fev0uhHoOxgmt1c/RNphVnblVkxa18GPp8Zr7W3v/XMETBTikm1MR30sbidrw==
```



需要用到 publicKey 和 password
privateKey：私钥，用于生成密文密码用，不用管
如果没配置好，可能会报一大推奇奇怪怪的错误，比如：org.springframework.beans.factory.BeanCreationException: Error creatingbean with name 'shiroFilter':....
在工程application.yml文件中，用生成的password覆盖原来的明文密码，同时在文件中添加如下配置：
spring.datasource.druid.publicKey=生成的publicKey

解密，在Druid中已经提供了解密的方法，只需要在配置文件中添加如下配置：

```java
filters: config,stat
connection-properties: config.decrypt=true;config.decrypt.key=生成的publicKey     
```

运行代码，能进入监控页面即配置成功

### **数据库密码解密：**

1. 创建一个DesPassword类写main方法，继承druid中的DruidPasswordCallback类。

2. 在main方法中引用ConfigTools.decrypt(publickey, password)方法，填入已知的公钥和密码

3. 打印结果，done

4. ```java
   import com.alibaba.druid.filter.config.ConfigTools;
   import com.alibaba.druid.util.DruidPasswordCallback;
     
     
     
   public class DesPassword extends DruidPasswordCallback {
     
        public static void main(String[] args) throws Exception{
           String publickey = 填入公钥;
           String password = 填入密码;
           String pwd = ConfigTools.decrypt(publickey, password);
     
           System.out.println(pwd);
        }
   }
   ```

   

# 六、打印sql执行日志

### 二、log4j和logback的区别

相同点：两个都是受欢迎的日志框架

区别：

- log4j是apache实现的一个开源日志组件。（Wrapped implementations）
- logback同样是由log4j的作者设计完成的，拥有更好的特性，用来取代log4j的一个日志框架。是slf4j的原生实现。（Native implementations）
- logback是直接实现了slf4j的接口，而log4j不是对slf4j的原生实现，所以slf4j api在调用log4j时需要一个适配层

三、logback相对于log4j的一些优点

**1.更快的实现** 
Logback的内核重写了，在一些关键执行路径上性能提升10倍以上。而且logback不仅性能提升了，初始化内存加载也更小了。 

**2.Logback-classic非常自然实现了SLF4j** 
Logback-classic实现了SLF4j。在使用SLF4j中，你都感觉不到logback-classic。而且因为logback-classic非常自然地实现了SLF4J，所以切换到log4j或者其他，非常容易，只需要提供成另一个jar包就OK，根本不需要去动那些通过SLF4JAPI实现的代码。

**3.自动重新加载配置文件** 
当配置文件修改了，Logback-classic能自动重新加载配置文件。扫描过程快且安全，它并不需要另外创建一个扫描线程。这个技术充分保证了应用程序能跑得很欢在JEE环境里面。

**4.SiftingAppender（一个非常多功能的Appender）** 
它可以用来分割日志文件根据任何一个给定的运行参数。如，SiftingAppender能够区别日志事件跟进用户的Session，然后每个用户会有一个日志文件。 
**5.自动压缩已经打出来的log** 
RollingFileAppender在产生新文件的时候，会自动压缩已经打出来的日志文件。压缩是个异步过程，所以甚至对于大的日志文件，在压缩过程中应用不会受任何影响。
**6.自动去除旧的日志文件** 
通过设置TimeBasedRollingPolicy或者SizeAndTimeBasedFNATP的maxHistory属性，你可以控制已经产生日志文件的最大数量。如果设置maxHistory为12，那那些log文件超过12个月的都会被自动移除

![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20191115165516320.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dldGVyX2Ryb3A=,size_16,color_FFFFFF,t_70)

日志输出内容元素具体如下：

- 时间日期：精确到毫秒
- 日志级别：ERROR, WARN, INFO, DEBUG or TRACE
- 进程ID
- 分隔符：— 标识实际日志的开始
- 线程名：方括号括起来（可能会截断控制台输出）
- Logger名：通常使用源代码的类名
- 日志内容

日志级别从低到高分为：![image-20201110100921968](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20201110100921968.png)

只能展示 **大于或等于** 设置的日志级别的日志；也就是说springboot默认级别为INFO，那么在控制台展示的日志级别只有INFO 、WARN、ERROR、FATAL



#### 1. pom.xml中springboot版本依赖

```java
<!--Spring-boot中去掉logback的依赖-->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-logging</artifactId>
		</exclusion>
	</exclusions>
</dependency>

<!--日志-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!--数据库连接池-->
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.6</version>
</dependency>

<!--其他依赖-->
```

#### 2. log4j2.xml文件中的日志配置

```java
<?xml version="1.0" encoding="UTF-8"?>
<configuration status="OFF">
    <appenders>

        <Console name="Console" target="SYSTEM_OUT">
            <!--只接受程序中DEBUG级别的日志进行处理-->
            <ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY"/>
            <PatternLayout pattern="[%d{HH:mm:ss.SSS}] %-5level %class{36} %L %M - %msg%xEx%n"/>
        </Console>

        <!--处理DEBUG级别的日志，并把该日志放到logs/debug.log文件中-->
        <!--打印出DEBUG级别日志，每次大小超过size，则这size大小的日志会自动存入按年份-月份建立的文件夹下面并进行压缩，作为存档-->
        <RollingFile name="RollingFileDebug" fileName="./logs/debug.log"
                     filePattern="logs/$${date:yyyy-MM}/debug-%d{yyyy-MM-dd}-%i.log.gz">
            <Filters>
                <ThresholdFilter level="DEBUG"/>
                <ThresholdFilter level="INFO" onMatch="DENY" onMismatch="NEUTRAL"/>
            </Filters>
            <PatternLayout
                    pattern="[%d{yyyy-MM-dd HH:mm:ss}] %-5level %class{36} %L %M - %msg%xEx%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>

        <!--处理INFO级别的日志，并把该日志放到logs/info.log文件中-->
        <RollingFile name="RollingFileInfo" fileName="./logs/info.log"
                     filePattern="logs/$${date:yyyy-MM}/info-%d{yyyy-MM-dd}-%i.log.gz">
            <Filters>
                <!--只接受INFO级别的日志，其余的全部拒绝处理-->
                <ThresholdFilter level="INFO"/>
                <ThresholdFilter level="WARN" onMatch="DENY" onMismatch="NEUTRAL"/>
            </Filters>
            <PatternLayout
                    pattern="[%d{yyyy-MM-dd HH:mm:ss}] %-5level %class{36} %L %M - %msg%xEx%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>

        <!--处理WARN级别的日志，并把该日志放到logs/warn.log文件中-->
        <RollingFile name="RollingFileWarn" fileName="./logs/warn.log"
                     filePattern="logs/$${date:yyyy-MM}/warn-%d{yyyy-MM-dd}-%i.log.gz">
            <Filters>
                <ThresholdFilter level="WARN"/>
                <ThresholdFilter level="ERROR" onMatch="DENY" onMismatch="NEUTRAL"/>
            </Filters>
            <PatternLayout
                    pattern="[%d{yyyy-MM-dd HH:mm:ss}] %-5level %class{36} %L %M - %msg%xEx%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>

        <!--处理error级别的日志，并把该日志放到logs/error.log文件中-->
        <RollingFile name="RollingFileError" fileName="./logs/error.log"
                     filePattern="logs/$${date:yyyy-MM}/error-%d{yyyy-MM-dd}-%i.log.gz">
            <ThresholdFilter level="ERROR"/>
            <PatternLayout
                    pattern="[%d{yyyy-MM-dd HH:mm:ss}] %-5level %class{36} %L %M - %msg%xEx%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>

        <!--druid的日志记录追加器-->
        <RollingFile name="druidSqlRollingFile" fileName="./logs/druid-sql.log"
                     filePattern="logs/$${date:yyyy-MM}/api-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="[%d{yyyy-MM-dd HH:mm:ss}] %-5level %L %M - %msg%xEx%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>
    </appenders>

    <loggers>
        <root level="DEBUG">
            <appender-ref ref="Console"/>
            <appender-ref ref="RollingFileInfo"/>
            <appender-ref ref="RollingFileWarn"/>
            <appender-ref ref="RollingFileError"/>
            <appender-ref ref="RollingFileDebug"/>
        </root>

        <!--记录druid-sql的记录-->
        <logger name="druid.sql.Statement" level="debug" additivity="false">
            <appender-ref ref="druidSqlRollingFile"/>
        </logger>
        <logger name="druid.sql.Statement" level="debug" additivity="false">
            <appender-ref ref="druidSqlRollingFile"/>
        </logger>

        <!--log4j2 自带过滤日志-->
        <Logger name="org.apache.catalina.startup.DigesterFactory" level="error" />
        <Logger name="org.apache.catalina.util.LifecycleBase" level="error" />
        <Logger name="org.apache.coyote.http11.Http11NioProtocol" level="warn" />
        <logger name="org.apache.sshd.common.util.SecurityUtils" level="warn"/>
        <Logger name="org.apache.tomcat.util.net.NioSelectorPool" level="warn" />
        <Logger name="org.crsh.plugin" level="warn" />
        <logger name="org.crsh.ssh" level="warn"/>
        <Logger name="org.eclipse.jetty.util.component.AbstractLifeCycle" level="error" />
        <Logger name="org.hibernate.validator.internal.util.Version" level="warn" />
        <logger name="org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration" level="warn"/>
        <logger name="org.springframework.boot.actuate.endpoint.jmx" level="warn"/>
        <logger name="org.thymeleaf" level="warn"/>
    </loggers>
</configuration>
```

#### 3. 配置application.properties

```java
# 配置日志输出
spring.datasource.druid.filter.slf4j.enabled=true
spring.datasource.druid.filter.slf4j.statement-create-after-log-enabled=false
spring.datasource.druid.filter.slf4j.statement-close-after-log-enabled=false
spring.datasource.druid.filter.slf4j.result-set-open-after-log-enabled=false
spring.datasource.druid.filter.slf4j.result-set-close-after-log-enabled=false
```

#### 4. 输出日志于druid-sql.log

```java
[2018-02-07 14:15:50] DEBUG 134 statementLog - {conn-10001, pstmt-20000} created. INSERT INTO city  ( id,name,state ) VALUES( ?,?,? )
[2018-02-07 14:15:50] DEBUG 134 statementLog - {conn-10001, pstmt-20000} Parameters : [null, b2ffa7bd-6b53-4392-aa39-fdf8e172ddf9, a9eb5f01-f6e6-414a-bde3-865f72097550]
[2018-02-07 14:15:50] DEBUG 134 statementLog - {conn-10001, pstmt-20000} Types : [OTHER, VARCHAR, VARCHAR]
[2018-02-07 14:15:50] DEBUG 134 statementLog - {conn-10001, pstmt-20000} executed. 5.113815 millis. INSERT INTO city  ( id,name,state ) VALUES( ?,?,? )
[2018-02-07 14:15:50] DEBUG 134 statementLog - {conn-10001, stmt-20001} executed. 0.874903 millis. SELECT LAST_INSERT_ID()
[2018-02-07 14:15:52] DEBUG 134 statementLog - {conn-10001, stmt-20002, rs-50001} query executed. 0.622665 millis. SELECT 1

```



# 七、SQL parser

在 Druid 的 SQL 解析器中，有三个重要的组成部分，它们分别是：

- Parser
  - 词法分析
  - 语法分析
- AST(Abstract Syntax Tree，抽象语法树)
- Visitor

这三者的关系如下图所示：

![图片描述](https://segmentfault.com/img/bVIeBg?w=344&h=412)

Parser 由两部分组成，词法分析和语法分析。
当拿到一条形如 `select id, name from user` 的 SQL 语句后，首先需要解析出每个独立的单词，select，id，name，from，user。这一部分，称为**词法分析**，也叫作 **Lexer**。
通过词法分析后，便要进行语法分析了。
经常能听到很多人在调侃自己英文水平很一般时会说：**26个字母我都知道，但是一组合在一起我就不知道是什么意思了**。这说明他掌握了词法分析的技能，却没有掌握语法分析的技能。
那么对于 SQL 解析器来说呢，它不仅需要知道每个单词，而且要知道这些单词组合在一起后，表达了什么含义。语法分析的职责就是明确一个语句的语义，表达的是什意思。
自然语言和形式语言的一个重要区别是，自然语言的一个语句，可能有多重含义，而形式语言的一个语句，只能有一个语义;形式语言的语法是人为规定的，有了一定的语法规则，语法解析器就能根据语法规则，解析出一个语句的一个唯一含义。

AST 是 Parser 的产物，语句经过词法分析，语法分析后，它的结构需要以一种计算机能读懂的方式表达出来，最常用的就是抽象语法树。
树的概念很接近于一个语句结构的表示，一个语句，我们经常会对它这样看待：它由哪些部分组成？其中一个组成部分又有哪些部分组成？例如一条 select 语句，它由 select 列表、where 子句、排序字段、分组字段等组成，而 select 列表则由一个或多个 select 项组成，where 子句又由一个或者多个 where条件组成。
在我们人类的思维中，这种组成结构就是一个总分的逻辑结构，用树来表达，最合适不过。并且对于计算机来说，它显然比人类更擅长处理“树”。

AST 仅仅是语义的表示，但如何对这个语义进行表达，便需要去访问这棵 AST，看它到底表达什么含义。通常遍历语法树，使用 VISITOR 模式去遍历，从根节点开始遍历，一直到最后一个叶子节点，在遍历的过程中，便不断地收集信息到一个上下文中，整个遍历过程完成后，对这棵树所表达的语法含义，已经被保存到上下文了。有时候一次遍历还不够，需要二次遍历。遍历的方式，广度优先的遍历方式是最常见的。

## 为什么要先了解ast？

ast全称是abstract syntax tree，中文直译抽象语法树。

SQL解析，本质上就是把SQL字符串给解析成ast，也就是说SqlParser的入参是SQL字符串，结果就是一个ast。你怎么使用这个ast结果又是另外一回事，你可以修改ast，也可以添加点东西等等，但整个过程都是围绕着ast这个东西。

![img](https://img2018.cnblogs.com/blog/1209816/201904/1209816-20190410234736398-1820516882.jpg)

## 什么是ast？

上面提了好几次ast，那ast又是个什么东西呢？

参照维基百科的说法，在计算机科学领域内，ast表示的是你写的编程语言源代码的抽象语法结构。如图：

![img](https://img2018.cnblogs.com/blog/1209816/201904/1209816-20190410235708924-2017257369.jpg)

左边是一个非常简单的编程语言源代码：1 + 2，做了一个加法计算，而当它被解析成ast以后如右边的图所示。我们可以看到ast存在三个节点，顶部的 + 表示一个加法节点，这个表达式组合了1、2两个数值节点，由这三个组合在一起的节点就组成了1+2这样的语法结构。

我们看到ast很清晰地用数据结构表示出了字符串源代码，ast的每一个节点均表示源代码当中的一个语法结构。反过来思考一下，我们可以知道源代码解析出来的ast是由很多这样简单的语法结构组合而成的，也就形成了一个复杂的语法树。下面我们看一个稍微复杂一点的，来自维基百科的示例

源代码：

```
1 while b ≠ 0
2   if a > b
3     a = a − b
4   else
5     b = b − a
6 return a
```

语法树：

![img](https://img2018.cnblogs.com/blog/1209816/201904/1209816-20190411001137101-75806028.png)

这个语法树也清晰地表示的源代码程序，主要由一个while语法和if/else语法以及一些变量之类的组成。

到这里，似乎对源代码和ast有了一个简单的概念，但是还是存在困惑，我为什么要把好好的代码搞成这样？它有什么用？如果只是修改语法，我用正则表达式修改字符串不是简单吗？

确实，有的时候直接处理字符串会是更快速更好的解决方式，但是当源程序语法非常复杂的时候字符串处理的复杂度已经不是一个简单的事了。而ast则把这些字符串变成结构化的数据了，你可以精确地知道一段代码里面有哪些变量名，函数名，参数等，你可以非常精准地处理，相对于字符串处理来说，遍历数据大大降低的处理难度。而ast也常常用在如IDE中错误提示、自动补全、编译器、语法翻译、重构、代码混淆压缩转换等。

了解ast可以参考文章：

https://mp.weixin.qq.com/s/UYzwVRPFas6hwe2U7R0eIg

https://www.cnblogs.com/jacksplwxy/p/10676578.html

https://blog.csdn.net/fei33423/article/details/79452922

https://www.jianshu.com/p/6a2f4ae4e099

https://en.wikipedia.org/wiki/Abstract_syntax_tree

https://en.wikipedia.org/wiki/Parse_tree#Constituency-based_parse_trees

## SqlParser

我们知道了ast是一种结构化的源代码表示，那针对SQL来说ast就是把SQL语句用结构化的数据来表示了。而SqlParser也就是把SQL解析成ast，这个解析过程则被SqlParser做了隐藏，我们不需要去实现这样一个字符串解析过程。

由此可见，我们需要了解两方面内容：

1、怎么用SqlParser把SQL语句解析成ast；

2、SqlParser解析出来的ast是什么样的一个结构。

### 解析成ast

解析语句相对简单，wiki上直接有示例，如：

```java
String dbType = JdbcConstants.MYSQL;
List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
```

SQLUtils的parseStatements方法会把你传入的SQL语句给解析成SQLStatement对象集合，每一个SQLStatement代表一条完整的SQL语句，如：

```sql
SELECT id FROM user WHERE status = 1
```

多个SQLStatement如：

```sql
SELECT id FROM user WHERE status = 1;
SELECT id FROM order WHERE create_time > '2018-01-01'
```

一般上我们只处理一条语句。

### ast的结构

#### 1. 在Druid SQL Parser中有哪些AST节点类型

在Druid中，AST节点类型主要包括SQLObject、SQLExpr、SQLStatement三种抽象类型。

```java
package com.alibaba.druid.sql.ast;

interface SQLObject {}
interface SQLExpr extends SQLObject {}
interface SQLStatement extends SQLObject {}

interface SQLTableSource extends SQLObject {}
class SQLSelect extends SQLObject {}
class SQLSelectQueryBlock extends SQLObject {}
```

##### 1.1. 常用的SQLExpr有哪些

```java
package com.alibaba.druid.sql.ast.expr;

// SQLName是一种的SQLExpr的Expr，包括SQLIdentifierExpr、SQLPropertyExpr等
public interface SQLName extends SQLExpr {}

// 例如 ID = 3 这里的ID是一个SQLIdentifierExpr
class SQLIdentifierExpr implements SQLExpr, SQLName {
    String name;
} 

// 例如 A.ID = 3 这里的A.ID是一个SQLPropertyExpr
class SQLPropertyExpr implements SQLExpr, SQLName {
    SQLExpr owner;
    String name;
} 

// 例如 ID = 3 这是一个SQLBinaryOpExpr
// left是ID (SQLIdentifierExpr)
// right是3 (SQLIntegerExpr)
class SQLBinaryOpExpr implements SQLExpr {
    SQLExpr left;
    SQLExpr right;
    SQLBinaryOperator operator;
}

// 例如 select * from where id = ?，这里的?是一个SQLVariantRefExpr，name是'?'
class SQLVariantRefExpr extends SQLExprImpl { 
    String name;
}

// 例如 ID = 3 这里的3是一个SQLIntegerExpr
public class SQLIntegerExpr extends SQLNumericLiteralExpr implements SQLValuableExpr { 
    Number number;

    // 所有实现了SQLValuableExpr接口的SQLExpr都可以直接调用这个方法求值
    @Override
    public Object getValue() {
        return this.number;
    }
}

// 例如 NAME = 'jobs' 这里的'jobs'是一个SQLCharExpr
public class SQLCharExpr extends SQLTextLiteralExpr implements SQLValuableExpr{
    String text;
}
```

##### 1.2. 常用的SQLStatement

最常用的Statement当然是SELECT/UPDATE/DELETE/INSERT，他们分别是

```java
package com.alibaba.druid.sql.ast.statement;

class SQLSelectStatement implements SQLStatement {
    SQLSelect select;
}
class SQLUpdateStatement implements SQLStatement {
    SQLExprTableSource tableSource;
     List<SQLUpdateSetItem> items;
     SQLExpr where;
}
class SQLDeleteStatement implements SQLStatement {
    SQLTableSource tableSource; 
    SQLExpr where;
}
class SQLInsertStatement implements SQLStatement {
    SQLExprTableSource tableSource;
    List<SQLExpr> columns;
    SQLSelect query;
}
```

##### 1.3. SQLTableSource

常见的SQLTableSource包括SQLExprTableSource、SQLJoinTableSource、SQLSubqueryTableSource、SQLWithSubqueryClause.Entry

```java
class SQLTableSourceImpl extends SQLObjectImpl implements SQLTableSource { 
    String alias;
}

// 例如 select * from emp where i = 3，这里的from emp是一个SQLExprTableSource
// 其中expr是一个name=emp的SQLIdentifierExpr
class SQLExprTableSource extends SQLTableSourceImpl {
    SQLExpr expr;
}

// 例如 select * from emp e inner join org o on e.org_id = o.id
// 其中left 'emp e' 是一个SQLExprTableSource，right 'org o'也是一个SQLExprTableSource
// condition 'e.org_id = o.id'是一个SQLBinaryOpExpr
class SQLJoinTableSource extends SQLTableSourceImpl {
    SQLTableSource left;
    SQLTableSource right;
    JoinType joinType; // INNER_JOIN/CROSS_JOIN/LEFT_OUTER_JOIN/RIGHT_OUTER_JOIN/...
    SQLExpr condition;
}

// 例如 select * from (select * from temp) a，这里第一层from(...)是一个SQLSubqueryTableSource
SQLSubqueryTableSource extends SQLTableSourceImpl {
    SQLSelect select;
}

/* 
例如
WITH RECURSIVE ancestors AS (
    SELECT *
    FROM org
    UNION
    SELECT f.*
    FROM org f, ancestors a
    WHERE f.id = a.parent_id
)
SELECT *
FROM ancestors;

这里的ancestors AS (...) 是一个SQLWithSubqueryClause.Entry
*/
class SQLWithSubqueryClause {
    static class Entry extends SQLTableSourceImpl {
         SQLSelect subQuery;
    }
}
```

##### 1.4. SQLSelect & SQLSelectQuery

SQLSelectStatement包含一个SQLSelect，SQLSelect包含一个SQLSelectQuery，都是组成的关系。SQLSelectQuery有主要的两个派生类，分别是SQLSelectQueryBlock和SQLUnionQuery。

```java
class SQLSelect extends SQLObjectImpl { 
    SQLWithSubqueryClause withSubQuery;
    SQLSelectQuery query;
}

interface SQLSelectQuery extends SQLObject {}

class SQLSelectQueryBlock implements SQLSelectQuery {
    List<SQLSelectItem> selectList;
    SQLTableSource from;
    SQLExprTableSource into;
    SQLExpr where;
    SQLSelectGroupByClause groupBy;
    SQLOrderBy orderBy;
    SQLLimit limit;
}

class SQLUnionQuery implements SQLSelectQuery {
    SQLSelectQuery left;
    SQLSelectQuery right;
    SQLUnionOperator operator; // UNION/UNION_ALL/MINUS/INTERSECT
}
```

##### 1.5. SQLCreateTableStatement

建表语句包含了一系列方法，用于方便各种操作

```java
public class SQLCreateTableStatement extends SQLStatementImpl implements SQLDDLStatement, SQLCreateStatement {
    SQLExprTableSource tableSource;
    List<SQLTableElement> tableElementList;
    Select select;

    // 忽略大小写的查找SQLCreateTableStatement中的SQLColumnDefinition
    public SQLColumnDefinition findColumn(String columName) {}

    // 忽略大小写的查找SQLCreateTableStatement中的column关联的索引
    public SQLTableElement findIndex(String columnName) {}

    // 是否外键依赖另外一个表
    public boolean isReferenced(String tableName) {}
}
```

#### 2. 怎样产生AST

##### 2.1. 通过SQLUtils产生List<SQLStatement>

```java
import com.alibaba.druid.util.JdbcConstants;

String dbType = JdbcConstants.MYSQL;
List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
```

##### 2.2. 通过SQLUtils产生SQLExpr

```java
String dbType = JdbcConstants.MYSQL;
SQLExpr expr = SQLUtils.toSQLExpr("id=3", dbType);
```

#### 3. 怎样打印AST节点

##### 3.1. 通过SQLUtils工具类打印节点

```java
package com.alibaba.druid.sql;

public class SQLUtils {
    // 可以将SQLExpr/SQLStatement打印为String类型
    static String toSQLString(SQLObject sqlObj, String dbType);

    // 可以将一个&lt;SQLStatement&gt;打印为String类型
    static String toSQLString(List<SQLStatement> statementList, String dbType);
}
```

## Visitor

- 从 demo 代码中可以看到，有了 AST 语法树后，则需要一个 visitor 来访问它

  ```java
          // 使用visitor来访问AST
          MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
          statement.accept(visitor);
          
          System.out.println(visitor.getColumns());
          System.out.println(visitor.getOrderByColumns());
  ```

  statement 调用 accept 方法，以 visitor 作为参数，开始了访问之旅。在这里 statement 的实际类型是 `SQLSelectStatement`。

  在 Druid 中，一条 SQL 语句中的元素，无论是高层次还是低层次的元素，都是一个 `SQLObject`，statement 是一种 SQLObject，表达式 expr 也是一种 SQLObject，函数、字段、条件等等，这些都是一种 SQLObject，SQLObject 是一个接口，`accept` 方法便是它定义的，目的是为了让访问者在访问 SQLObject 时，告知访问者一些事情，好让访问者在访问的过程中能够收集到关于该 SQLObject 的一些信息。

  具体的 `accept()` 实现，在 `SQLObjectImpl` 这个类中，代码如下所示：

  ```java
      public final void accept(SQLASTVisitor visitor) {
          if (visitor == null) {
              throw new IllegalArgumentException();
          }
  
          visitor.preVisit(this);
  
          accept0(visitor);
  
          visitor.postVisit(this);
      }
  ```

  这是一个 final 方法，意味着所有的子类都要遵循这个模板，首先 accept 方法前和后，visitor 都会做一些工作。真正的访问流程定义在 `accept0()` 方法里，而它是一个**抽象方法**。

  因此要知道 Druid 中是如何访问 AST 的，先拿 SQLSelectStatement 的 accept0() 方法来探探究竟。

  ```java
      protected void accept0(SQLASTVisitor visitor) {
          if (visitor.visit(this)) {
              acceptChild(visitor, this.select);
          }
          visitor.endVisit(this);
      }
  ```

  首先，使 visitor 访问自己，访问自己后，visitor 会决定是否还要访问自己的子元素。
  打开 `MySqlSchemaStateVisitor` 的 visit 方法，可以看到，visitor 做了一些事，初始化了自己的 aliasMap，然后 return true，这意味着还要访问 SQLSelectStatement 的子节点。

  ```java
      public boolean visit(SQLSelectStatement x) {
          setAliasMap();
          return true;
      }
  ```

  接下来访问子元素

  ```java
      protected final void acceptChild(SQLASTVisitor visitor, SQLObject child) {
          if (child == null) {
              return;
          }
  
          child.accept(visitor);
      }
  ```

  由此可以看出，SQLObject 负责通知 visitor 要访问自己的哪些元素，而 visitor 则负责访问相应元素前，中，后三个过程的逻辑处理。

### 1. 实现自己的Visitor

SqlParser：

```java
public class SqlParser {

    public static void main(String[] args) {
        String sql = "select * from t where id=1 and name=ming group by uid limit 1,200 order by ctime";

        // 新建 MySQL Parser
        SQLStatementParser parser = new MySqlStatementParser(sql);

        // 使用Parser解析生成AST，这里SQLStatement就是AST
        SQLStatement sqlStatement = parser.parseStatement();

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        sqlStatement.accept(visitor);

        System.out.println("getTables:" + visitor.getTables());
        System.out.println("getParameters:" + visitor.getParameters());
        System.out.println("getOrderByColumns:" + visitor.getOrderByColumns());
        System.out.println("getGroupByColumns:" + visitor.getGroupByColumns());
        System.out.println("---------------------------------------------------------------------------");

        // 使用select访问者进行select的关键信息打印
        SelectPrintVisitor selectPrintVisitor = new SelectPrintVisitor();
        sqlStatement.accept(selectPrintVisitor);

        System.out.println("---------------------------------------------------------------------------");
        // 最终sql输出
        StringWriter out = new StringWriter();
        TableNameVisitor outputVisitor = new TableNameVisitor(out);
        sqlStatement.accept(outputVisitor);
        System.out.println(out.toString());
    }

}
```

SelectPrintVisitor：

```java
/**
 * 查询语句访问者
 */
public class SelectPrintVisitor extends SQLASTVisitorAdapter {

    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        List<SQLSelectItem> selectItemList = x.getSelectList();
        selectItemList.forEach(selectItem -> {
            System.out.println("attr:" + selectItem.getAttributes());
            System.out.println("expr:" + SQLUtils.toMySqlString(selectItem.getExpr()));
        });

        System.out.println("table:" + SQLUtils.toMySqlString(x.getFrom()));
        System.out.println("where:" + SQLUtils.toMySqlString(x.getWhere()));
        System.out.println("order by:" + SQLUtils.toMySqlString(x.getOrderBy().getItems().get(0)));
        System.out.println("limit:" + SQLUtils.toMySqlString(x.getLimit()));

        return true;
    }

}
```

TableNameVisitor：

```java
/**
 * 数据库表名访问者
 */
public class TableNameVisitor extends MySqlOutputVisitor {

    public TableNameVisitor(Appendable appender) {
        super(appender);
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        SQLName table = (SQLName) x.getExpr();
        String tableName = table.getSimpleName();

        // 改写tableName
        print0("new_" + tableName.toUpperCase());

        return true;
    }

}
```

