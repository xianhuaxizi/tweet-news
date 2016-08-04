package tarun.learning.org.TwitterKafkaProducer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import tarun.learning.org.TwitterKafkaProducer.impl.ProducerTask;
import tarun.learning.org.TwitterKafkaProducer.impl.SingleTopicKafkaProducer;
import tarun.learning.org.TwitterKafkaProducer.impl.Twitter4jSource;
import tarun.learning.org.TwitterKafkaProducer.impl.TwtterStatusAvroTransformer;
import tarun.learning.org.tweet.core.AvroBinaryEncoder;
import tarun.learning.org.tweet.core.Constants;
import tarun.learning.org.tweet.core.Encoder;
import tarun.learning.org.tweet.core.schema.Tweet;

import twitter4j.Status;


public class App 
{
    public static void main( String[] args ) 
    {
        int seconds = 60;
    	if (args.length > 0) {
        	seconds = Integer.parseInt(args[0]);
        }
    	// Tweets message queue;
    	BlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(10000);
    	Encoder<Tweet> encoder = new AvroBinaryEncoder<Tweet>(Tweet.class);
    	TwtterStatusAvroTransformer transformer = new TwtterStatusAvroTransformer();
    	
    	// Worker thread poll which will send events to kafka
    	int nThreads = 1;
    	ExecutorService executor = Executors.newFixedThreadPool(nThreads+1);
    	for(int i=0; i< nThreads; i++) {
    		SingleTopicKafkaProducer producer = new SingleTopicKafkaProducer(Constants.KAFKA_TWEET_TOPIC);
    		ProducerTask<Status, Tweet> proTask = new ProducerTask<Status, Tweet>(queue, producer,encoder, transformer);
    		executor.submit(proTask);
    	}
    	
    	// Source to get tweet stream from Twitter
    	Twitter4jSource source = new Twitter4jSource(queue);
    	source.start();
    	
    	System.out.println(">>>Started All");
    	try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
    	System.out.println(">>>Time to shutdown");
    	
    	source.close();
    	executor.shutdownNow();
    	try {
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	System.out.println(">>>Shutdown All");
    }
}
