package com.rp.spring.jpa;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/11.
 */
public class QueryBuilder {

    private static final Pattern ORDERBY_PATTERN_1 = Pattern
            .compile("order\\s+by.+?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    //TODO cache transformers
    public static <C> Query transform(Query query, Class<C> clazz) {
        if (Map.class.isAssignableFrom(clazz)) {
            return query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        } else if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive() || String.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz)) {
            return query.setResultTransformer(new SmartTransformer(clazz));
        } else {
            return query.setResultTransformer(new BeanTransformerAdapter<C>(clazz));
        }
    }

    public static SQLQuery toSQLQuery(EntityManager em, String nativeQuery, Object beanOrMap) {
        Session session = em.unwrap(Session.class);
        SQLQuery query = session.createSQLQuery(nativeQuery);
        setParams(query, beanOrMap);
        return query;
    }

    private static String wrapCountQuery(String query) {
        return "select count(*) from (" + query + ") as ctmp";
    }

    private static String cleanOrderBy(String query) {
        Matcher matcher = ORDERBY_PATTERN_1.matcher(query);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (matcher.find()) {
            String part = matcher.group(i);
            if (canClean(part)) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, part);
            }
            i++;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean canClean(String orderByPart) {
        return orderByPart != null && (!orderByPart.contains(")")
                ||
                StringUtils.countOccurrencesOf(orderByPart, ")") == StringUtils.countOccurrencesOf(orderByPart, "("));
    }

    public static String toCountQuery(String query) {
        return wrapCountQuery(cleanOrderBy(query));
    }

    public static void setParams(SQLQuery query, Object beanOrMap) {
        String[] nps = query.getNamedParameters();
        if (nps != null) {
            Map<String, Object> params = toParams(beanOrMap);
            for (String key : nps) {
                Object arg = params.get(key);
                if (arg == null) {
                    query.setParameter(key, null);
                } else if (arg.getClass().isArray()) {
                    query.setParameterList(key, (Object[]) arg);
                } else if (arg instanceof Collection) {
                    query.setParameterList(key, ((Collection) arg));
                } else if (arg.getClass().isEnum()) {
                    query.setParameter(key, ((Enum) arg).ordinal());
                } else {
                    query.setParameter(key, arg);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toParams(Object beanOrMap) {
        Map<String, Object> params;
        if (beanOrMap instanceof Map) {
            params = (Map<String, Object>) beanOrMap;
        } else {
            params = toMap(beanOrMap);
        }
        if (!CollectionUtils.isEmpty(params)) {
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!isValidValue(params.get(key))) {
                    keys.remove();
                }
            }
        }
        return params;
    }

    public static boolean isValidValue(Object object) {
        if (object == null) {
            return false;
        }
        /*if (object instanceof Number && ((Number) object).longValue() == 0) {
            return false;
		}*/
        return !(object instanceof Collection && CollectionUtils.isEmpty((Collection<?>) object));
    }

    public static Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> description = new HashMap<String, Object>();
            if (bean instanceof DynaBean) {
                DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();
                for (DynaProperty descriptor : descriptors) {
                    String name = descriptor.getName();
                    description.put(name, BeanUtils.getProperty(bean, name));
                }
            } else {
                PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(bean);
                for (PropertyDescriptor descriptor : descriptors) {
                    String name = descriptor.getName();
                    if (PropertyUtils.getReadMethod(descriptor) != null) {
                        description.put(name, PropertyUtils.getNestedProperty(bean, name));
                    }
                }
            }
            return description;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }


    public static void main(String[] args) {
        String t1 = "select * from user order by id";
        String t2 = "select * from abc order by xxx(convert( resName using gbk )) collate gbk_chinese_ci asc";
        String t3 = "select count * from ((select * from aaa group by a order by a) union all (select * from aaa group by a order by a))";
        String t4 = "SELECT\n" +
                "  t1.*,t2.name AS dictionaryName,t3.name AS classifyName\n" +
                "FROM res_data_element t1 LEFT JOIN sys_business_dictionary t2 ON  t1.dictionary_id = t2.id\n" +
                "  LEFT JOIN sys_business_dictionary t3 ON  t1.classify = t3.id\n" +
                "WHERE  1=1\n" +
                "       AND  t1.is_history_version = 1\n" +
                "       AND t1.status = 1\n" +
                "       AND (t1.name LIKE '%${nameOrCodeOrENameOrCompany}%'\n" +
                "         OR\n" +
                "         t1.code LIKE '%${nameOrCodeOrENameOrCompany}%'\n" +
                "         OR\n" +
                "         t1.englishname LIKE '%${nameOrCodeOrENameOrCompany}%'\n" +
                "         OR\n" +
                "         t1.submit_company LIKE '%${nameOrCodeOrENameOrCompany}%')";
        System.out.println(QueryBuilder.toCountQuery(t1));
        System.out.println(QueryBuilder.toCountQuery(t2));
        System.out.println(QueryBuilder.toCountQuery(t3));
//        System.out.println(QueryBuilder.toCountQuery(t4));
    }
}
