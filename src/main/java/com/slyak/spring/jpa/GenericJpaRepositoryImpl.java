package com.slyak.spring.jpa;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/7
 */
public class GenericJpaRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements GenericJpaRepository<T, ID>, Serializable {


    private JpaEntityInformation<T, ID> eif;

    private boolean isStatusAble = false;

    private Method statusReadMethod;

    private Method statusWriteMethod;

    public GenericJpaRepositoryImpl(JpaEntityInformation<T, ID> eif, EntityManager em) {
        super(eif, em);
        this.eif = eif;
        PropertyDescriptor descriptor = findFieldPropertyDescriptor(eif.getJavaType(), Status.class);
        isStatusAble = descriptor != null;
        if (isStatusAble) {
            statusReadMethod = descriptor.getReadMethod();
            statusWriteMethod = descriptor.getWriteMethod();
        }
    }

    @Override
    public Map<ID, T> mget(Collection<ID> ids) {
        return toMap(findAllById(ids));
    }

    @Override
    public Map<ID, T> mgetOneByOne(Collection<ID> ids) {
        return toMap(findAllOneByOne(ids));
    }

    @Override
    public List<T> findAllOneByOne(Collection<ID> ids) {
        List<T> results = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(results::add);
        }
        return results;
    }

    private Map<ID, T> toMap(List<T> list) {
        Map<ID, T> result = new LinkedHashMap<>();
        for (T t : list) {
            if (t != null) {
                result.put(eif.getId(t), t);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void toggleStatus(ID id) {
        if (isStatusAble && id != null) {
            Optional<T> target = findById(id);
            if (target.isPresent()) {
                Status status = (Status) ReflectionUtils.invokeMethod(statusReadMethod, target);
                if (status == Status.ENABLED || status == Status.DISABLED) {
                    ReflectionUtils.invokeMethod(statusWriteMethod, target,
                            status == Status.DISABLED ? Status.ENABLED : Status.DISABLED);
                    save(target.get());
                }
            }
        }
    }

    @SafeVarargs
    @Override
    @Transactional
    public final void fakeDelete(ID... ids) {
        for (ID id : ids) {
            changeStatus(id, Status.DELETED);
        }
    }

    private void changeStatus(ID id, Status status) {
        if (isStatusAble && id != null) {
            Optional<T> target = findById(id);
            if (target.isPresent()) {
                Status oldStatus = (Status) ReflectionUtils.invokeMethod(statusReadMethod, target);
                if (oldStatus != status) {
                    ReflectionUtils.invokeMethod(statusWriteMethod, target, status);
                    save(target.get());
                }
            }
        }
    }

    private PropertyDescriptor findFieldPropertyDescriptor(Class target, Class fieldClass) {
        PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(target);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getPropertyType() == fieldClass) {
                return pd;
            }
        }
        return null;
    }
}
