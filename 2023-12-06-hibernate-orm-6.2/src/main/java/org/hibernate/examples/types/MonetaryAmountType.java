package org.hibernate.examples.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.ValueAccess;
import org.hibernate.usertype.CompositeUserType;

public class MonetaryAmountType implements CompositeUserType<MonetaryAmount> {
	@Override
	public Object getPropertyValue(MonetaryAmount component, int property) throws HibernateException {
		switch ( property ) {
			case 0:
				return component.getNumber().numberValue( BigDecimal.class );
			case 1:
				return Currency.getInstance( component.getCurrency().getCurrencyCode() );
			default:
				throw new IllegalArgumentException( "Unknown property index: " + property );
		}
	}

	@Override
	public MonetaryAmount instantiate(ValueAccess values, SessionFactoryImplementor sessionFactory) {
		final BigDecimal amount = values.getValue( 0, BigDecimal.class );
		final Currency currency = values.getValue( 1, Currency.class );
		return Monetary.getDefaultAmountFactory()
				.setNumber( amount )
				.setCurrency( Monetary.getCurrency( currency.getCurrencyCode() ) )
				.create();
	}

	@Override
	public Class<?> embeddable() {
		return MonetaryAmountEmbeddable.class;
	}

	@Override
	public Class<MonetaryAmount> returnedClass() {
		return MonetaryAmount.class;
	}

	@Override
	public boolean equals(MonetaryAmount x, MonetaryAmount y) {
		return Objects.equals( x, y );
	}

	@Override
	public int hashCode(MonetaryAmount x) {
		return Objects.hashCode( x );
	}

	@Override
	public MonetaryAmount deepCopy(MonetaryAmount value) {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(MonetaryAmount value) {
		return (Serializable) value;
	}

	@Override
	public MonetaryAmount assemble(Serializable cached, Object owner) {
		return (MonetaryAmount) cached;
	}

	@Override
	public MonetaryAmount replace(MonetaryAmount detached, MonetaryAmount managed, Object owner) {
		return detached;
	}

	public static class MonetaryAmountEmbeddable {
		BigDecimal amount;
		Currency currency;
	}
}
