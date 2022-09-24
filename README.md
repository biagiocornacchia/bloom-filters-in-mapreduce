# Bloom Filters in MapReduce

Project for Cloud Computing course at University of Pisa (MSc in Computer Engineering).

The aim of the project was to  build a Bloom filter over the ratings of movies listed in the [IMDb datasets](https://datasets.imdbws.com/). In paricular, we have to build one Bloom filter per average rating where the latter are rounded to the closest integer value. 

The project consists in :
1. an implementation of the MapReduce Bloom filter construction algorithm using the `Hadoop framework`
2. an implementaton the MapReduce Bloom filter construction algorithm using the `Spark framework`

## Some requirements
In the Hadoop implementation, we had to use the following classes:
- `org.apache.hadoop.mapreduce.lib.input.NLineInputFormat`: splits N lines of input as one split;
- `org.apache.hadoop.util.hash.Hash.MURMUR_HASH`: the hash function family to use.

In the Spark implementation, we had to use/implement analogous classes.

## Hadoop Execution
To start the execution on the namenode:

``` 
hadoop jar hadoop-bloom-filters-1.0-SNAPSHOT.jar it.unipi.hadoop.Main data.tsv output_dir 100000 0.0001 1
``` 

where:
- `data.tsv` is the path of the input file on HDFS
- `output_dir` is the name of the output directory
- `100000` is the number of lines of each split
- `0.0001` is the p value chosen
- `1`  is the version to put into execution for the job2. In the version `1` the Mapper emits an array of bloom filters exploiting the in-mapper combiner pattern. In the version version `2` the Mapper emits the bit positions (set to one) of the bloom filter.

## Spark Execution

To start the execution of Spark:

``` 
spark-submit --master yarn main.py data.tsv aggregate_by_key false 0
``` 

where:
- `aggregate_by_key` is the type of job2 to put into execution

## Authors

* Biagio Cornacchia, b.cornacchia@studenti.unipi.it
* Giacomo Pacini, g.pacini14@studenti.unipi.it
* Elisa De Filomeno, e.defilomeno@studenti.unipi.it
* Matteo Pierucci, m.pierucci5@studenti.unipi.it