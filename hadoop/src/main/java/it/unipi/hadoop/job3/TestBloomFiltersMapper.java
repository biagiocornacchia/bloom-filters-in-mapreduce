package it.unipi.hadoop.job3;

import it.unipi.hadoop.model.BloomFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.net.URI;

public class TestBloomFiltersMapper extends Mapper<Object, Text, IntWritable, Text> {

    private static final String  BLOOM_FILTERS_PATH = "";
    private static final int N_FILTERS = 10;

    private BloomFilter[] bloomFilters = new BloomFilter[N_FILTERS];
    private int[] counterFP = new int[N_FILTERS];
    private int[] counterFN = new int[N_FILTERS];
    private int[] counterTP = new int[N_FILTERS];
    private int[] counterTN = new int[N_FILTERS];
    private int counterMultiPositiveResults = 0;
    private final IntWritable key = new IntWritable();
    private final Text value = new Text();

    public void setup(Context ctx) throws IOException {

        URI[] cachedFiles = ctx.getCacheFiles();
        IntWritable key = new IntWritable();
        BloomFilter filter = new BloomFilter();

        for (URI fileStatus : cachedFiles) {
            if (!fileStatus.getPath().toString().endsWith("_SUCCESS")) {
                SequenceFile.Reader reader = new SequenceFile.Reader(new Configuration(), SequenceFile.Reader.file(new Path(fileStatus.getPath())));

                boolean hasNext;
                do {
                    hasNext = reader.next(key, filter);
                    if(key == null || filter == null || filter.getK() == 0){
                        continue;
                    }
                    // System.out.println(key.get() + " | bloom filter: " + filter.toString());
                    this.bloomFilters[key.get() - 1] = new BloomFilter(filter);
                } while (hasNext);
            }
        }
    }

    public void map(Object key, Text value, Context context){

        String record = value.toString();
        if(record == null || record.length() == 0)
            return;

        String[] tags = value.toString().split("\t");

        if(tags.length != 3)
            return;

        int roundedRating = Math.round(Float.parseFloat(tags[1]));
        if (roundedRating == 0)
            return;

        int i = 1;
        int positive_result_counter=0;
        boolean valid = false;
        try {
            for (BloomFilter bloomFilter : bloomFilters) {
                if (bloomFilter != null && bloomFilter.isInitialized()) {
                    if (bloomFilter.check(tags[0])) {
                        positive_result_counter += 1;
                        if (i == roundedRating) {
                            counterTP[i - 1] += 1;
                            valid = true;
                        } else
                            counterFP[i - 1] += 1;
                    } else {
                        if (i != roundedRating)
                            counterTN[i - 1] += 1;
                        else
                            counterFN[i - 1] += 1;
                    }
                }
                i += 1;
            }

            if (!valid){
                throw new Exception("The current row (" + record + ") was not found inside its bloom filter!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(positive_result_counter > 1)
            this.counterMultiPositiveResults +=1;
    }

    public void cleanup(Context ctx) throws IOException, InterruptedException {
        for (int i = 0; i < N_FILTERS; i++) {
            value.set(String.valueOf(counterFP[i]) + ',' +
                    String.valueOf(counterFN[i]) + ',' +
                    String.valueOf(counterTP[i]) + ',' +
                    String.valueOf(counterTN[i]));

            key.set(i+1);
            ctx.write(key, value);
        }

        value.set(String.valueOf(counterMultiPositiveResults));
        key.set(N_FILTERS + 1);
        ctx.write(key, value);
    }
}

