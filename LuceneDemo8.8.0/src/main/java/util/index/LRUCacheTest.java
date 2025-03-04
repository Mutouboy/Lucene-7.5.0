package util.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import util.FileOperation;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Lu Xugang
 * @date 2021/6/10 10:38 上午
 */
public class LRUCacheTest {
    private Directory directory;

    {
        try {
            FileOperation.deleteFile("./data");
            directory = new MMapDirectory(Paths.get("./data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Analyzer analyzer = new WhitespaceAnalyzer();
    private final IndexWriterConfig conf = new IndexWriterConfig(analyzer);
    private IndexWriter indexWriter;

    public void doSearch() throws Exception {
        conf.setUseCompoundFile(false);
        indexWriter = new IndexWriter(directory, conf);
        int count = 0;
        Document doc ;
        while (count++ < 10000){
            doc = new Document();
            doc.add(new TextField("author",  "ably lily baby andy lucy ably", Field.Store.YES));
            indexWriter.addDocument(doc);

            doc = new Document();
            doc.add(new TextField("author",  "lily andy tom good for you", Field.Store.YES));
            indexWriter.addDocument(doc);

            doc = new Document();
            doc.add(new TextField("author",  "you", Field.Store.YES));
            indexWriter.addDocument(doc);
        }
        indexWriter.commit();
        IndexReader reader = DirectoryReader.open(indexWriter);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new TermQuery(new Term("author", "lily")), BooleanClause.Occur.SHOULD);
        builder.add(new TermQuery(new Term("author", "lucy")), BooleanClause.Occur.SHOULD);
        builder.add(new TermQuery(new Term("author", "andy")), BooleanClause.Occur.SHOULD);
        builder.add(new TermQuery(new Term("author", "for")), BooleanClause.Occur.SHOULD);
        builder.setMinimumNumberShouldMatch(2);
        Query query = builder.build();
//        Query query = new TermQuery(new Term("author", new BytesRef("ably")));
        TotalHitCountCollector collector = new TotalHitCountCollector();
//        searcher.setQueryCache(null);
        int iter = 0;
        while (iter++ < 6){
            searcher.search(query, collector);
        }
        System.out.println(collector.getTotalHits());

        reader.close();
        indexWriter.close();

    }

    public static void main(String[] args) throws Exception{
        LRUCacheTest test = new LRUCacheTest();
        test.doSearch();
    }
}
