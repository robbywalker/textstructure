/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.cluster;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.SentenceSplitter;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.SentenceSplitterFactory;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Attempts to cluster related documents.
 */
public class Cluster {
  public static Document convertToDocument(File file) throws IOException {
    String input = FileUtils.readFileToString(file);
    SentenceSplitter splitter = new SentenceSplitterFactory().newSentenceSplitter();
    List<String> words = Lists.newArrayList();
    for (List<String> sentence : splitter.extractSentences(input)) {
      words.add(String.format("%x", sentence.hashCode()));
    }
    String hashText = Joiner.on(" ").join(words);

    Document document = new Document();
    document.add(new Field("hashes", hashText, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
    document.add(new Field("filename", file.getAbsolutePath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    return document;
  }

  public static List<List<File>> getClusters(List<File> files) throws IOException {
    RAMDirectory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

    for (File file : files) {
      writer.addDocument(convertToDocument(file));
    }

    writer.close();

    IndexReader reader = IndexReader.open(directory, true);
    IndexSearcher searcher = new IndexSearcher(reader);
    MoreLikeThis mlt = new MoreLikeThis(reader);
    mlt.setFieldNames(null);
    mlt.setMinTermFreq(0);
    mlt.setMinDocFreq(2);

    TopDocs allDocs = searcher.search(new MatchAllDocsQuery(), files.size());
    for (ScoreDoc doc : allDocs.scoreDocs) {
      Document document = searcher.doc(doc.doc);
      System.out.println(document.get("filename"));

      TopDocs mostSimilar = searcher.search(mlt.like(doc.doc), files.size());
      System.out.println(mostSimilar.totalHits + " results");
      for (ScoreDoc similar : mostSimilar.scoreDocs) {
        Document similarDoc = searcher.doc(similar.doc);
        System.out.println(similarDoc.get("filename") + ": " + similar.score);
      }

      System.out.println("\n");
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] argv) {
    File directory = new File("/var/greplin/src/textstructure/documents");

    try {
      getClusters((List<File>) FileUtils.listFiles(directory, new String[] {"txt"}, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
