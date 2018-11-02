package com.mmorpg.mbdl.framework.communicate.websocket.generator;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 经过定制的Twitter_Snowflake算法<br>
 * SnowFlake的结构:<br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 39位时间截(毫秒级)，注意，39位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截),
 * 得到的值，这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序PlayerIdGenerator类的startTime属性）。
 * 39位的时间截，大约可以使用17年，年T = (1L << 39) / (1000L * 60 * 60 * 24 * 365)<br>
 * 11位的数据机器位，可以部署在2048个节点，包括5位datacenterId和6位serverId<br>
 * 9位序列，毫秒内的计数，7位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生128个ID序号,每秒大约能生成10w个id<br>
 * 只要每秒注册人数不超过10w就不会产生问题<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
@Component
public class PlayerIdGenerator implements IIdGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PlayerIdGenerator.class);

    /** 线程id所占的位数,目前留空 */
    private static final long threadIdBits = 4L;
    /** 最大线程id量结果是64,因为用来求余，所以加1 */
    private static final long maxThreadId = ~(-1L << threadIdBits);
    /** 数据标识id所占的位数 */
    private static final long datacenterIdBits = 5L;
    /** 支持的最大数据标识id，结果是31 */
    private static final long maxDatacenterId = ~(-1L << datacenterIdBits);
    /** 机器id所占的位数 */
    private static final long serverIdBits = 6L;
    /** 支持的最大机器id，结果是63 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private static final long maxServerId = ~(-1L << serverIdBits);

    /** 序列在id中占的位数 */
    private static final long sequenceBits = 9L;
    /** 机器ID向左移8位 */
    private static final long serverIdShift = sequenceBits;
    /** 数据中心标识id向左移15位(6+9) */
    private static final long datacenterIdShift= serverIdShift + serverIdBits;
    /** 线程Id左移5+15=20位 */
    private static final long threadIdShift = datacenterIdShift + datacenterIdBits;
    /** 时间截向左移24位(5+15) + 4 4位预留位，用于存放其他信息*/
    private static final long timestampLeftShift = threadIdShift + threadIdBits;
    /** 生成序列的掩码，512-1 */
    private static final long sequenceMask = ~(-1L << sequenceBits);

    /** 开始时间截 */
    @Value("${server.config.beginOn}")
    private String beginOnString = "2018/10/30";
    private long beginOn ;
    /** 数据中心ID(0~31) */
    @Value("${server.config.dataCenterId}")
    private long datacenterId = 1;
    /** 服务器ID(0~63) */
    @Value("${server.config.serverId}")
    private long serverId = 1;

    /** 毫秒内序列号(0~255) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    @PostConstruct
    private void init(){
        String[] date = StringUtils.split(this.beginOnString,'/');
        Calendar calendar = new Calendar.Builder().setDate(Integer.valueOf(date[0]),Integer.valueOf(date[1])-1,Integer.valueOf(date[2])).build();
        this.beginOn = calendar.getTimeInMillis();
        if (serverId > maxServerId || serverId < 0) {
            throw new IllegalArgumentException(String.format("服务器Id不能大于%d或小于0", maxServerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("数据中心Id不能大于%d或小于0", maxDatacenterId));
        }
        if (this.beginOn >= System.currentTimeMillis()) {
            throw new IllegalArgumentException("当前时间未到设定的运营时间: " + DateFormat.getDateInstance().format(new Date(this.beginOn)) + "无法生成id");
        }
    }

    /**
     * 生成id，如果时钟发生回拨，那么会尝试生成10（默认）次id,超出次数会抛出RuntimeException
     * @return id
     */
    @Override
    public Long generate() {
        return nextId(10);
    }

    /**
     * 发生时钟回拨时尝试生成Id的次数，到达上限后悔抛出异常
     * @param tryTimes 尝试次数
     * @return id
     */
    public Long generate(int tryTimes) {
        return nextId(tryTimes);
    }
    /** 由于加锁也能每秒生成30w个以上的id，因此可以考虑不做优化 */
    private synchronized long nextId(int tryTimes) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            if (tryTimes==0) {
                throw new RuntimeException("时钟发生回移，本次生成id失败");
            }
            return nextId(--tryTimes);
        }

        //如果上次生成时间和当前时间相同,在同一毫秒内
        if (lastTimestamp == timestamp) {
            //sequence自增，因为sequence只有7bit，所以和sequenceMask相与一下，去掉高位
            sequence = (sequence+1) & sequenceMask;
            //判断是否溢出,也就是每毫秒内超过512，当为512时，与sequenceMask相与，sequence就等于0
            if (sequence == 0) {
                //自旋等待到下一毫秒
                timestamp = tailNextMillis(lastTimestamp);
            }
        } else {
            // 如果和上次生成时间不同,重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加
            sequence=0;
        }
        lastTimestamp = timestamp;
        return ((timestamp - this.beginOn) << timestampLeftShift)
                // | (Thread.currentThread().getChatChannelId()%16 << threadIdShift)
                | (datacenterId << datacenterIdShift)
                | (serverId << serverIdShift)
                | sequence;
    }

    // 防止产生的时间比之前的时间还要小（由于NTP回拨等问题）,保持增量的趋势.
    private long tailNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    // 获取当前的时间戳
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /** 测试 */
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        PlayerIdGenerator playerIdGenerator = ctx.getBean(PlayerIdGenerator.class);
        Set<Long> longsConcurrent = new ConcurrentSet<>();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("test-pool-%d").build();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int j = 0; j < 30_0000; j++) {
            playerIdGenerator.generate();
        }
        stopWatch.stop();
        logger.info("耗时{}ms",stopWatch.getTime());

        int threadSize = 6;
        ExecutorService threadPool = new ThreadPoolExecutor(threadSize, threadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < threadSize; i++) {
            threadPool.execute(()->{
                // logger.info("{}",Thread.currentThread().getChatChannelId()%16);
                long start = System.currentTimeMillis();
                for (int j = 0; j < 30_0000/threadSize; j++) {
                    long id = playerIdGenerator.generate();
                    // logger.info("{}",id);
                    if (longsConcurrent.contains(id)){
                        logger.info("{}",Long.toBinaryString(id).substring(0,Math.toIntExact(64-timestampLeftShift)));
                    }else {
                        longsConcurrent.add(id);
                    }
                }
                // stopwatch.stop();
                // 注意，因为是并发，所以时长以最后完成的时间为准
                logger.info(Thread.currentThread().getName()+"完成,"+"生成{}个id耗时：{}ms",300000,System.currentTimeMillis()- start);
            });
        }
        threadPool.shutdown();
        try {
            if (threadPool.awaitTermination(5,TimeUnit.SECONDS)){
                logger.info(longsConcurrent.size()+"");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
