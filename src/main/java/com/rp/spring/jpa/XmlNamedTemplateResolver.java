package com.rp.spring.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import java.util.Iterator;
import java.util.List;

/**
 * .
 *
 * @author stormning on 2016/12/17.
 */
public class XmlNamedTemplateResolver implements NamedTemplateResolver {
	protected final Log logger = LogFactory.getLog(getClass());

	private String encoding = "UTF-8";

	private DocumentLoader documentLoader = new DefaultDocumentLoader();

	private EntityResolver entityResolver;

	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	public XmlNamedTemplateResolver(ResourceLoader resourceLoader) {
		this.entityResolver = new ResourceEntityResolver(resourceLoader);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public Iterator<Void> doInTemplateResource(Resource resource, final NamedTemplateCallback callback)
			throws Exception {
		InputSource inputSource = new InputSource(resource.getInputStream());
		inputSource.setEncoding(encoding);
		Document doc = documentLoader.loadDocument(inputSource, entityResolver, errorHandler,
				XmlValidationModeDetector.VALIDATION_XSD, false);
		final List<Element> sqes = DomUtils.getChildElementsByTagName(doc.getDocumentElement(), "sql");

		return new Iterator<Void>() {
			int index = 0, total = sqes.size();

			@Override
			public boolean hasNext() {
				return index < total;
			}

			@Override
			public Void next() {
				Element sqle = sqes.get(index);
				callback.process(sqle.getAttribute("name"), sqle.getTextContent());
				index++;
				return null;
			}

			@Override
			public void remove() {
				//ignore
			}
		};
	}
}
