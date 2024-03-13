package org.hibernate.examples.representation;

import org.hibernate.examples.entities.Article;

public record SimilarArticle(Article article, Double similarity) {
}
