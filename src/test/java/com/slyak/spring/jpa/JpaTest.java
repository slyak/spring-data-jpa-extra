package com.slyak.spring.jpa;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class JpaTest {
	@Autowired
	private SampleRepository sampleRepository;

	@Before
	public void addSomeSample() {
		sampleRepository.deleteAll();
		for (int i = 0; i < 12; i++) {
			Sample sample = new Sample();
			sample.setContent("hello world" + i);
			sampleRepository.save(sample);
		}
		
	}

	@Test
	public void findByTemplateQuery() {
		Page<Sample> samples = sampleRepository.findByContent("%world1%", new PageRequest(0, 100));
		Assert.assertTrue(samples.getTotalElements() == 12);
	}
	
	@Test
	public void findByTemplateQueryNullValue() {
		Page<Sample> samples = sampleRepository.findByContent(null, new PageRequest(0, 100));
		Assert.assertTrue(samples.getTotalElements() == 12);
	}
	

	@Test
	public void countByTemplateQuery() {
		long count = sampleRepository.countContent("%world1%");
		Assert.assertTrue(count == 3);
	}

	@Test
	public void findByTemplateQueryAndReturnDTOs() {
		List<SampleDTO> dtos = sampleRepository.findDtos();
		Assert.assertTrue(dtos.size() == 12);
	}

	@Test
	public void findByTemplateQueryWithTemplateQueryObject() {
		SampleQuery sq = new SampleQuery();
		sq.setContent("%world1%");
		List<Sample> samples = sampleRepository.findByTemplateQueryObject(sq, null);
		Assert.assertTrue(samples.size() == 3);
	}

	@Test
	public void findBySpringElQuery() {
		List<Sample> dtos = sampleRepository.findDtos2("%world1%");
		Assert.assertTrue(dtos.size() == 3);
	}

	@Test
	public void findMap(){
		List<Map<String,Object>> listMaps = sampleRepository.findMap();
		Assert.assertTrue(listMaps.size() == 12);
	}

}
