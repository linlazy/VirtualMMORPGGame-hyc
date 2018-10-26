package com.mmorpg.mbdl.framework.communicate.websocket.handler;

import com.google.common.base.Predicate;
import com.mmorpg.mbdl.bussiness.chat.packet.ChatResp;
import com.mmorpg.mbdl.framework.communicate.websocket.annotation.PacketHandler;
import com.mmorpg.mbdl.framework.communicate.websocket.model.AbstractPacket;
import com.mmorpg.mbdl.framework.communicate.websocket.model.WsSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllMethods;

/**
 * AbstractPacket包分发处理器，用于将AbstractPacket对象赋值给相应模块对应的处理方法的形参
 * (因为是Inbound所以只有请求包能到达这里)
 * 所以需要一个 Table <abstractPacket.getClass,method, bean（Object>
 * Reflections库以及spring的ReflectionUtils可以帮助完成以上需求
 * @author sando
 */
@ChannelHandler.Sharable
@Component
public class AbstractPacketDispacherHandler extends SimpleChannelInboundHandler<AbstractPacket>
                                            implements BeanPostProcessor {
    private static final Logger logger= LoggerFactory.getLogger(AbstractPacketDispacherHandler.class);
    // private Table<Class<?>, Object, Method> abstractPacket2Method2Object= HashBasedTable.create();
    private Map<Class<?>,Object> class2Object = new HashMap<>();
    private Map<Class<?>,Method> class2Method = new HashMap<>();
    Channel firstChannel ;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractPacket abstractPacket) throws Exception {
        // TODO 不能在netty worker线程池作业务处理,如果当前请求处理发生阻塞，那么这条（4条之一）worker线程就会被阻塞
        // 另外由于同一个channel的消息可能会被分到不同的worker线程，这样就可能导致后到的请求先被处理，从而导致严重的问题
        // 以下用于验证是否同一个channel的请求走到不同的worker线程，确认成功，后续删除这些注释
        // if (firstChannel==null){
        //     firstChannel=ctx.channel();
        // }
        // if (ctx.channel()==firstChannel) {
        //     logger.info(ctx.channel().remoteAddress()+"");
        //     Object obj=ReflectionUtils.invokeMethod(class2Method.get(abstractPacket.getClass()),class2Object.get(abstractPacket.getClass())
        //             ,new WsSession(),abstractPacket);
        // }

        Object obj= ReflectionUtils.invokeMethod(class2Method.get(abstractPacket.getClass()),class2Object.get(abstractPacket.getClass())
                ,new WsSession(),abstractPacket);
        if (obj != null){

        }
        // 发送响应包 LoginResultResp
        ChatResp chatResp = new ChatResp();
        chatResp.setContent("聊天响应接收成功");
        ctx.channel().writeAndFlush(chatResp);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, java.lang.String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        // TODO 如果PacketHandler类的请求处理方法很长，需要切分时可能会产生问题，到时候增加个可选注解即可，切分时用注解标明哪个是入口
        if (clazz.isAnnotationPresent(PacketHandler.class)){
            // 获取clazz中其中一个参数的父类或本身是AbstractPacket的所有方法
            Set<Method> methods=getAllMethods(clazz, withAnyParametersAssignableFrom(AbstractPacket.class));
            if (methods.size()==0){
                java.lang.String message = java.lang.String.format("类[%s]带@PacketHandler但却没有任何带AbstractPacket参数的方法",clazz.toString());
                logger.error(message);
                throw new  RuntimeException(message);
            }
            // 这些方法的参数数量应该为二，第一个参数应为WSession,
            // 第二个为AbstractPacket的子类（确认第一个参数后第二个参数为AbstractPacket的子类必然成立）
            // TODO 返回值校验并把返回值作为响应包发回去
            for (Method method : methods) {
                if (method.getParameterTypes().length!=2){
                    java.lang.String message = java.lang.String.format("方法[%s]必须为两个参数",
                            method.getDeclaringClass().getSimpleName()+"::"+method.getName());
                    throw new IllegalArgumentException(message);
                }
                if ( !WsSession.class.isAssignableFrom(method.getParameterTypes()[0])){
                    java.lang.String message = java.lang.String.format("方法[%s]第一个参数的类型必须为WSession或其子类",
                            method.getDeclaringClass().getSimpleName()+"::"+method.getName());
                    throw new IllegalArgumentException(message);
                }
                Class<?> clazz2 = method.getParameterTypes()[1];
                if (class2Method.keySet().contains(clazz2)) {
                    Method methodOld = class2Method.get(clazz2);
                    java.lang.String message =
                            java.lang.String.format("类型为[%s]的请求包同时被方法[%s]和方法[%s]处理", clazz2.getSimpleName(),
                                    methodOld.getDeclaringClass().getSimpleName()+"::"+methodOld.getName(),
                                    method.getDeclaringClass().getSimpleName()+"::"+method.getName());
                    throw new IllegalArgumentException(message);
                }
                Class<?> returnType=method.getReturnType();
                if ( returnType!= void.class && !AbstractPacket.class.isAssignableFrom(returnType)){
                    java.lang.String message = java.lang.String.format("方法[%s]返回值只能是void或者AbstractPacket或其子类",
                            method.getDeclaringClass().getSimpleName()+"::"+method.getName());
                    throw new IllegalArgumentException(message);
                }
                class2Object.put(clazz2,bean);
                class2Method.put(clazz2,method);
            }
        }
        return bean;
    }

    /**
     * 看看方法任一参数的类型是不是任一给定的类型<types>的子类或本身，是则返回true
     * TODO 加到某个util中去
     * @param types
     * @return
     */
    public static Predicate<Method> withAnyParametersAssignableFrom(final Class... types) {
        return (@javax.annotation.Nullable Method input) -> {
                if (input != null) {
                    Class<?>[] parameterTypes = input.getParameterTypes();
                    for (Class<?> parameterType : parameterTypes) {
                        for (Class<?> type : types) {
                            if (type.isAssignableFrom(parameterType)){
                                return true;
                            }
                        }
                    }

                }
                return false;
            };
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, java.lang.String beanName) throws BeansException {
        return bean;
    }
}
