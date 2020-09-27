package com.slyak.spring.jpa;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.rp.spring.jpa.GenericJpaRepository;
import com.rp.spring.jpa.TemplateQuery;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
public interface SampleRepository extends GenericJpaRepository<Sample, Long> {

	@TemplateQuery
	Page<Sample> findByContent(String content, Pageable pageable);

	@TemplateQuery
	List<Sample> findByTemplateQueryObject(SampleQuery sampleQuery, Pageable pageable);

	@TemplateQuery
	Long countContent(String content);

	@TemplateQuery
	List<SampleDTO> findDtos();

	// #{name?:'and content like :name'}
	@Query(nativeQuery = true, value = "select * from t_sample where content like ?1")
	List<Sample> findDtos2(String name);

	@TemplateQuery
	List<Map<String,Object>> findMap();
}
