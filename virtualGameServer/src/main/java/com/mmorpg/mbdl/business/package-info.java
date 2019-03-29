@TypeDefs({
    @TypeDef(name = JsonType.NAME, typeClass = JsonType.class),
    @TypeDef(name = EnumReadableType.NAME, typeClass = EnumReadableType.class)
})
package com.mmorpg.mbdl.business;
/**
 * https://gwoham-163-com.iteye.com/blog/1895101
 */

import com.mmorpg.mbdl.common.orm.EnumReadableType;
import com.mmorpg.mbdl.common.orm.JsonType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;