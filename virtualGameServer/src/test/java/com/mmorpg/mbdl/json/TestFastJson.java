package com.mmorpg.mbdl.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmorpg.mbdl.framework.common.utils.JsonUtil;
import com.mmorpg.mbdl.business.container.resource.ItemRes;
import com.mmorpg.mbdl.framework.reflectasm.withunsafe.FieldAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试类
 *
 * @author Sando Geek
 * @since v1.0 2018/12/3
 **/
public class TestFastJson {
    private static Logger logger = LoggerFactory.getLogger(TestFastJson.class);
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);

    }

    @Test
    void 测试() {
        Map<Integer,Long> map = new HashMap<>();
        map.put(1,0L);

        // I know this way violates the java generic constraint.
        String json = null;
        try {
            json = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<Integer,Long> mapFromJson = null;
        try {
            mapFromJson = mapper.readValue(json, new TypeReference<Map<Integer,Long>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Long v : mapFromJson.values()) {
            // will throw ClassCastException
            System.out.println(v);
        }
    }

    @Test
    void 枚举转换测试() {
        FieldAccess javaBeanTestAccess = FieldAccess.accessUnsafe(JavaBeanTest.class);
        FieldAccess javaBean2Access = FieldAccess.accessUnsafe(JavaBean2.class);
        JavaBeanTest javaBeanTest = new JavaBeanTest();
        JavaBean2 javaBean2 = new JavaBean2();
        javaBeanTestAccess.setInt(javaBeanTest,javaBeanTestAccess.getIndex("anInt"),15);
        javaBeanTestAccess.setObject(javaBeanTest,"state",State.LOGIN);
        javaBean2Access.setInt(javaBean2,javaBeanTestAccess.getIndex("anInt"),25);
        javaBean2Access.setObject(javaBean2,"state",State.ONLINE);
        javaBeanTestAccess.setObject(javaBeanTest,"javaBean2",javaBean2);
        javaBeanTestAccess.setObject(javaBeanTest,"string","hello json");
        javaBeanTestAccess.setObject(javaBeanTest,"integer",1);

        try {
            String s = mapper.writeValueAsString(javaBeanTest);
            logger.info("jackson:{}",s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        JavaBeanTest test = JSON.parseObject("{\"string\":\"hello json\",\"anInt\":15,\"state\":\"LOGIN\"}", JavaBeanTest.class, Feature.SupportNonPublicField);
        logger.info("{}", JSON.toJSON(javaBeanTest, new SerializeConfig(true)));

    }

    @Test
    void jsonProperty设置() throws IllegalAccessException, InstantiationException, IOException {
        ItemRes itemRes = ItemRes.class.newInstance();
        FieldAccess itemResAccess = FieldAccess.accessUnsafe(ItemRes.class);
        itemResAccess.setObject(itemRes,itemResAccess.getIndex("id"),"hello");
        itemResAccess.setObject(itemRes,itemResAccess.getIndex("name"),"what");
        String writeValueAsString = JsonUtil.object2String(itemRes);
        logger.info("{}",writeValueAsString);
        ItemRes itemRes1 = mapper.readValue("{\"Id\":20,\"Name\":\"what\"}", ItemRes.class);
        logger.info("{}",itemRes1);
    }
}
