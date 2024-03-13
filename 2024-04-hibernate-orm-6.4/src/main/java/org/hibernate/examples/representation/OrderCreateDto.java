package org.hibernate.examples.representation;

import java.util.List;

public record OrderCreateDto(List<OrderLineCreateDto> orderLines) {
}
