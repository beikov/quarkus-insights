package org.hibernate.examples.representation;

import java.net.URI;

public record SimilarArticles(URI article1, URI article2, Double similarity) {
}
