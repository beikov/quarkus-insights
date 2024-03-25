package org.hibernate.examples.resources;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.examples.entities.Article;
import org.hibernate.examples.repository.ArticleRepository;
import org.hibernate.examples.repository.SimilarityFunction;
import org.hibernate.examples.representation.SimilarArticles;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

@Path("articles")
public class ArticleResource {

	@Inject
	ArticleRepository articleRepository;
	@Inject
	HttpClient httpClient;
	@Inject
	EmbeddingModel embeddingModel;

	@PUT
	@Path("{url}")
	public Response scan(@PathParam("url") URI url) {
		var existingArticle = articleRepository.findById( url );
		if ( existingArticle == null ) {
			var article = processArticle( url );
			articleRepository.persist( article );
		}
		return Response.ok().build();
	}

	@POST
	@Path("import")
	public Response importFeed(@QueryParam("atomUrl") @DefaultValue("https://in.relation.to/blog.atom") URI feedUrl) {
		var articles = processFeed( feedUrl );
		articleRepository.persistAll( articles );
		return Response.ok().build();
	}

	@POST
	@Path("similar")
	public List<SimilarArticles> getSimilarArticles(
			@QueryParam("function") @DefaultValue("L2") SimilarityFunction function,
			@QueryParam("threshold") @DefaultValue("5") double threshold) {
		return articleRepository.getSimilarArticles( function, threshold );
	}

	@POST
	public Response processArticles() {
		var unprocessedArticles = articleRepository.findUnprocessedArticles();
		if ( !unprocessedArticles.isEmpty() ) {
			for ( Article unprocessedArticle : unprocessedArticles ) {
				var embedding = computeEmbedding( unprocessedArticle );
				articleRepository.updateEmbedding( unprocessedArticle.url, embedding );
			}
		}
		return Response.ok().build();
	}

	private float[] computeEmbedding(Article unprocessedArticle) {
		var text = new StringBuilder();
		text.append( "Title: " ).append( unprocessedArticle.title ).append( "\\n" );
		text.append( "Author: " ).append( unprocessedArticle.author ).append( "\\n" );
		text.append( "Content: " ).append( unprocessedArticle.content );
		var embeddingResponse = embeddingModel.embed( text.toString() );
		return embeddingResponse.content().vector();
	}

	private Article processArticle(URI url) {
		final HttpRequest request = HttpRequest.newBuilder( url ).GET().build();
		final HttpResponse<String> httpResponse;
		try {
			httpResponse = httpClient.send( request, HttpResponse.BodyHandlers.ofString() );
		}
		catch (IOException | InterruptedException e) {
			throw new WebApplicationException( e );
		}
		var document = Jsoup.parse( httpResponse.body() );
		var title = document.selectXpath( "//*[@itemprop='headline']" ).text();
		var author = document.selectXpath( "//*[@itemprop='author']" ).text();
		var content = document.selectXpath( "//*[@itemprop='articleBody']" ).text();
		return new Article( url, title, author, content );
	}

	private Collection<Article> processFeed(URI feedUrl) {
		final HttpRequest request = HttpRequest.newBuilder( feedUrl ).GET().build();
		final HttpResponse<String> httpResponse;
		try {
			httpResponse = httpClient.send( request, HttpResponse.BodyHandlers.ofString() );
		}
		catch (IOException | InterruptedException e) {
			throw new WebApplicationException( e );
		}
		var document = Jsoup.parse( httpResponse.body(), Parser.xmlParser() );
		var entryElements = document.selectXpath( "//entry" );
		var articles = new HashMap<URI, Article>( entryElements.size() );
		for ( Element entry : entryElements ) {
			var url = URI.create( entry.selectXpath( "link" ).attr( "href" ) );
			var title = entry.selectXpath( "title" ).text();
			var author = entry.selectXpath( "author/name" ).text();
			var contentHtml = entry.selectXpath( "content" ).text();
			var content = Jsoup.parseBodyFragment( contentHtml ).text();
			articles.put( url, new Article( url, title, author, content ) );
		}

		var foundArticles = articleRepository.findAllById( articles.keySet() );
		for ( Article foundArticle : foundArticles ) {
			if ( foundArticle != null ) {
				articles.remove( foundArticle.url );
			}
		}
		return articles.values();
	}
}
