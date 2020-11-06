**1. 实现一个自定义的classloader，加载如下的文件，内容需要解码，读取的字节码需要解码，解码方式：255减去原有值，并执行成功**


`

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class TestMyClassLoader {
    static class CustomClassLoader extends ClassLoader {
        private byte[] decrypt(String className) throws IOException {
            try (FileInputStream file = new FileInputStream("/Users/zoe/IdeaProjects/linzy/src/Hello.xlass"); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
                int b;
                while ((b = file.read()) != -1) {
                    byteArrayOutputStream.write((byte) ((byte) 255 - b));
                }
                return byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                throw new IOException();
            }
        }
        /**
         * 重写findClass方法
         */
        @Override
        protected Class<?> findClass(String className) throws ClassNotFoundException {
            try {
                byte[] bytes = decrypt(className);
                return defineClass(className, bytes, 0, bytes.length);
            } catch (Exception e) {
                throw new ClassNotFoundException(className);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        CustomClassLoader classLoader = new CustomClassLoader();
        Class<?> clazz = classLoader.loadClass("Hello");
        Object object  = clazz.newInstance();
        Method method = clazz.getMethod("hello");
        method.invoke(object);
    }
}   
`


**运行结果**
`Hello, classLoader!`

**2. 分析以下GC日志，尽可能详细的标注出GC发生时相关的信息。**

发生CMS GC
阶段1:初始标记  本次GC发生的时间与JVM启动时间的差 114.015					当前老年代使用情况（老年代可用容量） 当前整个堆的使用情况（整个堆的容量） 0.28秒
2020-10-29T21:19:19.488+0800: 114.015: [GC (CMS Initial Mark) [1 CMS-initial-mark: 106000K(2097152K)] 1084619K(3984640K), 0.2824583 secs] [Times: user=0.86 sys=0.00, real=0.28 secs]

阶段2:并发标记开始
2020-10-29T21:19:19.771+0800: 114.298: [CMS-concurrent-mark-start]

阶段2:并发标记时间 0.16秒
2020-10-29T21:19:19.931+0800: 114.458: [CMS-concurrent-mark: 0.160/0.160 secs] [Times: user=0.32 sys=0.03, real=0.16 secs]

阶段3:并发预清理开始
2020-10-29T21:19:19.931+0800: 114.459: [CMS-concurrent-preclean-start]

阶段3:并发预清理时间 0.65秒
2020-10-29T21:19:19.998+0800: 114.525: [CMS-concurrent-preclean: 0.065/0.066 secs] [Times: user=0.05 sys=0.01, real=0.06 secs]

阶段3:并发可终止预清理开始
2020-10-29T21:19:19.998+0800: 114.525: [CMS-concurrent-abortable-preclean-start]CMS: abort preclean due to time 

阶段3:并发可终止预清除 时间5秒
2020-10-29T21:19:25.072+0800: 119.599: [CMS-concurrent-abortable-preclean: 5.038/5.073 secs] [Times: user=7.72 sys=0.50, real=5.08 secs]

阶段4:最终标记	年轻代使用情况（年轻代可用容量）
2020-10-29T21:19:25.076+0800: 119.603: [GC (CMS Final Remark) [YG occupancy: 1279357 K (1887488 K)]

阶段4:应用停止的阶段完成存活对象的标记工作
2020-10-29T21:19:25.076+0800: 119.603: [Rescan (parallel) , 0.3120602 secs]
阶段4:第一个子阶段，随着这个阶段的进行处理弱引用
2020-10-29T21:19:25.388+0800: 119.915: [weak refs processing, 0.0015920 secs]
阶段4:第二个子阶段
2020-10-29T21:19:25.390+0800: 119.917: [class unloading, 0.0517863 secs]
阶段4:最后一个子阶段
2020-10-29T21:19:25.441+0800: 119.969: [scrub symbol table, 0.0212825 secs]
阶段4之后的 当前老年代使用情况（老年代可用容量） 当前整个堆的使用情况（整个堆的容量）
2020-10-29T21:19:25.463+0800: 119.990: [scrub string table, 0.0022435 secs][1 CMS-remark: 106000K(2097152K)] 1385358K(3984640K), 0.3959182 secs] [Times: user=1.33 sys=0.00, real=0.40 secs]

阶段5:并发清除开始
2020-10-29T21:19:25.473+0800: 120.000: [CMS-concurrent-sweep-start]
阶段5:并发清除时间 0.067秒
2020-10-29T21:19:25.540+0800: 120.067: [CMS-concurrent-sweep: 0.067/0.067 secs] [Times: user=0.18 sys=0.02, real=0.06 secs]

阶段6:并发重置开始
2020-10-29T21:19:25.540+0800: 120.068: [CMS-concurrent-reset-start]
阶段6:并发重置时间 0.003秒
2020-10-29T21:19:25.544+0800: 120.071: [CMS-concurrent-reset: 0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]


**3. 标注以下启动参数每个参数的含义**

		环境参数PRO	堆区初始值4G 堆区最大值4G 新生代最大值2G  堆外内存512M	元空间大小128M 		元空间最大值512M					禁用偏向锁				禁用热度衰减
java -Denv=PRO -server -Xms4g -Xmx4g -Xmn2g -XX:MaxDirectMemorySize=512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:-UseBiasedLocking -XX:-UseCounterDecay 
自动装箱缓存最大值为10240	开启老年代使用CMS收集器	触发执行CMS回收的当前年代区内存占用的百分比设置为百分之75 开启只根据占用情况作为开始执行CMS收集的标准 年轻代最大年龄为6
-XX:AutoBoxCacheMax=10240 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:MaxTenuringThreshold=6 
GC算法使用CMS							CMS中的下列阶段并发执行			关闭PerfData写入			启动时预申请内存	关闭优化频繁抛出的异常
-XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+PerfDisableSharedMem -XX:+AlwaysPreTouch -XX:-OmitStackTraceInFastThrow  
当JVM发生OOM时，自动生成DUMP文件		生成DUMP文件的路径/home/devjava/logs/															打印应用暂停时间
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/devjava/logs/ -Xloggc:/home/devjava/logs/lifecircle-tradecore-gc.log -XX:+PrintGCApplicationStoppedTime 
GC时打印时间戳信息					GC时打印更多详细信息
-XX:+PrintGCDateStamps -XX:+PrintGCDetails -javaagent:/home/devjava/ArmsAgent/arms-bootstrap-1.7.0-SNAPSHOT.jar -jar /home/devjava/lifecircle-tradecore/app/lifecircle-tradecore.jar