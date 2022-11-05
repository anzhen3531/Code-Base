package service;

import entity.MpBaseEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 基础Crud方法实现
 *
 * @param <T>
 */
public interface MpCrudBaseService<T extends MpBaseEntity> {

    /**
     * find one by id
     *
     * @param id resource id
     */
    Optional<T> findOne(Long id);

    /**
     * find one by id ignore state
     *
     * @param id resource id
     */
    Optional<T> findOneState(Long id);

    /**
     * find all resource
     */
    List<T> findAll();

    /**
     * find all resource ignore state
     */
    List<T> findAllState();

    /**
     * find resource by ids
     */
    List<T> findByIds(Collection<Long> ids);

    /**
     * find resource not equals ids
     *
     * @param ids resource ids
     */
    List<T> findByNotInIds(Collection<Long> ids);

    /**
     * Create new resource.
     *
     * @param resource Resource to create
     * @return new resource
     */
    T create(T resource);

    /**
     * batch Create resource
     *
     * @param resources Resources to create
     */
    List<T> batchCreate(List<T> resources);

    /**
     * Update existing resource.
     *
     * @param resource Resource to update
     * @return resource updated
     */
    T update(T resource);

    /**
     * Update existing resource by id with no null columns
     *
     * @param resource Resource to update
     * @return resource updated
     */
    T updateAllProperties(T resource);

    /**
     * batch update resource
     *
     * @param resources Resources to update
     */
    void batchUpdate(List<T> resources);

    /**
     * Delete existing resource.
     *
     * @param id Resource id
     */
    void delete(Long id);

    /**
     * Delete existing resource.
     *
     * @param ids Resource id
     */
    void batchDelete(List<Long> ids);

    /**
     * 归档
     */
    void achieve(Long id);

    /**
     * 归档
     */
    void achieve(T resource);

    /**
     * 归档
     *
     * @param ids Resource id
     */
    void batchAchieve(List<Long> ids);


}
