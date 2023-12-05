package org.hibernate.examples.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import org.zalando.jackson.datatype.money.MoneyModule;

@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {
	@Override
	public void customize(ObjectMapper mapper) {
		mapper.registerModule( new MoneyModule().withoutFormatting() );
	}
}
