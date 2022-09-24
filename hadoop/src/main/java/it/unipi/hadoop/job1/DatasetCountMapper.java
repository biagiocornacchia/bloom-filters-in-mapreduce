package it.unipi.hadoop.job1;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;

import java.io.IOException;

public class DatasetCountMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
    private int[] counters;
    private final IntWritable key = new IntWritable();
    private final IntWritable value = new IntWritable();

    public void setup(Context ctx){
        this.counters = new int[10];
    }
    public void map(Object key, Text value, Context ctx){
        // Example of record: tt9916544   6.8   57
        String record = value.toString();
        if(record == null || record.length() == 0)
            return;

        String[] tags = value.toString().split("\t");
        if(tags.length != 3)
            return;

        int roundedRating = Math.round(Float.parseFloat(tags[1]));
        if (roundedRating == 0)
            return;

        this.counters[roundedRating - 1]++;
    }

    public void cleanup(Context ctx) throws IOException, InterruptedException {
        int i = 1;
        for (int c: this.counters) {
            key.set(i);
            value.set(c);
            ctx.write(key, value);
            i++;
        }
    }
}

