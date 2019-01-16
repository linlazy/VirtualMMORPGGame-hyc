package com.mmorpg.mbdl.business.common.resource;

import com.mmorpg.mbdl.framework.resource.annotation.Id;
import com.mmorpg.mbdl.framework.resource.annotation.ResDef;

/**
 * 定义静态资源
 *
 * @author Sando Geek
 * @since v1.0
 **/
@ResDef(relativePath = "excel/a/ErrorTipsRes.xlsx")
// @ResDef
public class ErrorTipsRes {
    @Id
    private int code;

    public int getCode() {
        return code;
    }
}
