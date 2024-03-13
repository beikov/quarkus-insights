package org.hibernate.examples.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.examples.entities.Article;
import org.hibernate.examples.representation.SimilarArticle;
import org.hibernate.examples.representation.SimilarArticles;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ArticleRepository {

	@Inject
	Session session;

	public Article findById(URI url) {
		return session.find( Article.class, url );
	}

	public List<Article> findAllById(Collection<URI> urls) {
		return session.byMultipleIds( Article.class )
				.multiLoad( urls.toArray( Object[]::new ) );
	}

	public List<Article> findUnprocessedArticles() {
		return session.createSelectionQuery( "from Article a where a.embedding is null order by a.url", Article.class )
				.setMaxResults( 10 )
				.getResultList();
	}

	public List<SimilarArticle> findSimilarArticles(float[] embedding, double threshold) {
		return session.createSelectionQuery(
						"""
								select new org.hibernate.examples.representation.SimilarArticle(
								    a,
								    l2_distance(a.embedding, :embedding) as similarity
								)
								from Article a
								where l2_distance(a.embedding, :embedding) < :threshold
								order by similarity
								""",
						SimilarArticle.class
				)
				.setParameter( "embedding", embedding )
				.setParameter( "threshold", threshold )
				.getResultList();
	}

	@Transactional
	public void persist(Article article) {
		session.persist( article );
	}

	@Transactional
	public void persistAll(Iterable<Article> articles) {
		for ( Article article : articles ) {
			session.persist( article );
		}
	}

	@Transactional
	public void updateEmbedding(URI url, float[] embedding) {
		final int updateCount = session.createMutationQuery(
						"""
								update Article a
								set a.embedding = :embedding
								where a.url = :url
								"""
				)
				.setParameter( "embedding", embedding )
				.setParameter( "url", url )
				.executeUpdate();
		if ( updateCount != 1 ) {
			throw new OptimisticLockException();
		}
	}

	public List<SimilarArticles> getSimilarArticles(SimilarityFunction function, double threshold) {
		return session.createSelectionQuery(
						STR."""
								select new org.hibernate.examples.representation.SimilarArticles(
								    a1.url,
								    a2.url,
								    \{function.name().toLowerCase( Locale.ROOT )}_distance(a1.embedding, a2.embedding) as similarity
								)
								from Article a1 join Article a2 on a1 <> a2
								where \{function.name().toLowerCase( Locale.ROOT )}_distance(a1.embedding, a2.embedding) < :threshold
								order by similarity
								""",
						SimilarArticles.class
				)
				.setParameter( "threshold", threshold )
				.getResultList();
	}
}
