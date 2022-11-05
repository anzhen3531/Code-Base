package mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import entity.MpBaseEntity;

/**
 * mybatis-plus 基础Mapper
 *
 * @param <T>
 */
public interface MpBaseMapper<T extends MpBaseEntity> extends BaseMapper<T> {
}
