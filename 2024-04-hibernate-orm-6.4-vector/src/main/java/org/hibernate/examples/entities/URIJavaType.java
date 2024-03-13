package org.hibernate.examples.entities;

import java.net.URI;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

public class URIJavaType extends AbstractClassJavaType<URI> {
	public URIJavaType() {
		super( URI.class );
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
		return indicators.getJdbcType( SqlTypes.VARCHAR );
	}

	@Override
	public <X> X unwrap(URI value, Class<X> type, WrapperOptions options) {
		if ( type == URI.class ) {
			return type.cast( value );
		}
		if ( type == String.class ) {
			return (X) value.toString();
		}
		throw unknownUnwrap( type );
	}

	@Override
	public <X> URI wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( value instanceof URI ) {
			return (URI) value;
		}
		if ( value instanceof String ) {
			return URI.create( (String) value );
		}
		throw unknownWrap( value.getClass() );
	}
}
