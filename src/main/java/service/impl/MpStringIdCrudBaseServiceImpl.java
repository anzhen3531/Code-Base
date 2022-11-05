package service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import constant.CommonConstant;
import context.AccountInfoContext;
import context.AccountInfoContextHolder;
import entity.MpBaseEntity;
import mapper.MpBaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import service.MpStringIdCrudBaseService;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class MpStringIdCrudBaseServiceImpl<T extends MpBaseEntity, R extends MpBaseMapper<T>> implements MpStringIdCrudBaseService<T> {

    private static final Logger logger = LoggerFactory.getLogger(MpStringIdCrudBaseServiceImpl.class);
    private final Class<T> entityClass;
    protected R repository;

    /**
     * 通过有参构造器传入Mapper对象
     *
     * @param repository
     */
    public MpStringIdCrudBaseServiceImpl(R repository) {
        this.repository = repository;
        // 获取范型子类的类型
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        entityClass = (Class) params[0];
    }

    @Override
    public Optional<T> findOne(String id) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", CommonConstant.ARCHIVE_STATE).eq("id", id);
        return Optional.ofNullable(repository.selectOne(queryWrapper));
    }

    @Override
    public Optional<T> findOneState(String id) {
        return Optional.ofNullable(repository.selectById(id));
    }

    @Override
    public List<T> findAll() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", CommonConstant.ARCHIVE_STATE).orderByDesc("updated_time");
        return repository.selectList(queryWrapper);
    }

    @Override
    public List<T> findAllState() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("updated_time");
        return repository.selectList(queryWrapper);
    }

    @Override
    public List<T> findByIds(Collection<String> ids) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", CommonConstant.ARCHIVE_STATE);
        queryWrapper.in(!CollectionUtils.isEmpty(ids), "id", ids);
        queryWrapper.orderByDesc("updated_time");
        return repository.selectList(queryWrapper);
    }

    @Override
    public List<T> findByNotInIds(Collection<String> ids) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", CommonConstant.ARCHIVE_STATE);
        queryWrapper.notIn(!CollectionUtils.isEmpty(ids), "id", ids);
        queryWrapper.orderByDesc("updated_time");
        return repository.selectList(queryWrapper);
    }

    @Override
    public T create(T resource) {
        this.assembleBaseInfo(resource, true);
        resource.setState(CommonConstant.ARCHIVE_STATE);
        repository.insert(resource);
        return resource;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<T> batchCreate(List<T> resources) {
        return resources.stream().peek(resource -> {
            this.assembleBaseInfo(resource, true);
            repository.insert(resource);
        }).collect(Collectors.toList());
    }


    @Override
    public T update(T resource) {
        T update = repository.selectById(resource.getId());

        if (Objects.isNull(update)) {
            throw new IllegalArgumentException("invalid " + entityClass.getName() + " id:" + resource.getId());
        }

        this.copyNonNullProperties(resource, update);
        this.assembleBaseInfo(update, false);
        repository.updateById(update);
        return update;
    }

    @Override
    public T updateAllProperties(T resource) {
        this.assembleBaseInfo(resource, false);
        repository.updateById(resource);
        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<T> resources) {
        resources.forEach(resource -> {
            this.assembleBaseInfo(resource, false);
            repository.updateById(resource);
        });
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(this::delete);
    }

    @Override
    public void achieve(String id) {
        AccountInfoContext context = AccountInfoContextHolder.getContext();
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("state", CommonConstant.ARCHIVE_STATE);
        if (Objects.nonNull(context)) {
            Long userId = context.getUserId();
            updateWrapper.set("updated_by", userId);
        }
        repository.update(null, updateWrapper);
    }

    @Override
    public void achieve(T resource) {
        resource.setState(CommonConstant.ARCHIVE_STATE);
        this.assembleBaseInfo(resource, false);
        repository.updateById(resource);
    }

    @Override
    public void batchAchieve(List<String> ids) {
        AccountInfoContext context = AccountInfoContextHolder.getContext();
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids).set("state", CommonConstant.ARCHIVE_STATE);
        if (Objects.nonNull(context)) {
            Long userId = context.getUserId();
            updateWrapper.set("updated_by", userId);
        }
        repository.update(null, updateWrapper);
    }


    /**
     * 组装基础信息
     *
     * @param resource 资源对象
     * @param isCreate 是否创建
     */
    private void assembleBaseInfo(T resource, boolean isCreate) {
        AccountInfoContext context = AccountInfoContextHolder.getContext();
        if (Objects.nonNull(context)) {
            Long userId = context.getUserId();
            if (isCreate) {
                resource.setCreatedBy(userId);
            }
            resource.setUpdatedBy(userId);
        }
    }

    /**
     * 拷贝不为空的属性
     *
     * @param resource
     * @param update
     */
    private void copyNonNullProperties(T resource, T update) {
        BeanUtils.copyProperties(resource, update, getNullProperties(resource));
    }

    /**
     * 将为空的properties给找出来,然后返回出来
     *
     * @param src
     * @return
     */
    private static String[] getNullProperties(Object src) {
        BeanWrapper srcBean = new BeanWrapperImpl(src);
        PropertyDescriptor[] pds = srcBean.getPropertyDescriptors();
        Set<String> emptyName = new HashSet<>();
        for (PropertyDescriptor p : pds) {
            Object srcValue = srcBean.getPropertyValue(p.getName());
            if (srcValue == null) {
                emptyName.add(p.getName());
            }
        }
        String[] result = new String[emptyName.size()];
        return emptyName.toArray(result);
    }
}
