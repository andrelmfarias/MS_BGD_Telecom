package com.sparkProject

import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.{sum, lower, split, explode}
import org.apache.spark.sql.{DataFrame, SparkSession}


object WordCount {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAll(Map(
      "spark.scheduler.mode" -> "FIFO",
      "spark.speculation" -> "false",
      "spark.reducer.maxSizeInFlight" -> "48m",
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer",
      "spark.kryoserializer.buffer.max" -> "1g",
      "spark.shuffle.file.buffer" -> "32k",
      "spark.default.parallelism" -> "12",
      "spark.sql.shuffle.partitions" -> "12"
    ))

    val spark = SparkSession
      .builder
      .config(conf)
      .appName("TP_spark")
      .getOrCreate()

    import spark.implicits._

    val sc = spark.sparkContext


    /** ******************************************************************************
      *
      * TP 1
      *        - Lecture de données
      *        - Word count , Map Reduce
      * *******************************************************************************/


    // a)
    val rdd = sc.textFile("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md")

    // b)
    println("5 first rows of the RDD")
    rdd.take(5).foreach(println)

    // c)
    val wordCount = rdd
      .flatMap { line: String => line.split(" ") }
      .map { word: String => (word, 1) }
      .reduceByKey { (i: Int, j: Int) => i + j }
      .toDF("word", "count")

    wordCount.show()

    // d)
    wordCount.orderBy($"count".desc).show()

    // e)
    val df_lower = wordCount.withColumn("word_lower", lower($"word"))

    df_lower.show()

    // f)
    val df_grouped = df_lower
      .groupBy("word_lower")
      .agg(sum("count").as("new_count"))

    df_grouped.orderBy($"new_count".desc).show()


    // Une Autre version de l'exercice, en n'utilisant que les dataFrame et des operations sur les colonnes
    println("With dataFrame only")

    val df2 = spark
      .read
      .text("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md") // read.text retourne un dataFrame avec une seule colonne, nommée "value"
      .withColumn("words", split($"value", " "))
      .select("words")
      .withColumn("words", explode($"words"))
      .withColumn("words", lower($"words"))
      .groupBy("words")
      .count

    df2.orderBy($"count".desc)show()

    // ou encore, de façon plus concise:
    spark
      .read
      .text("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md") // read.text retourne un dataFrame avec une seule colonne, nommée "value"
      .withColumn("words", lower(explode(split($"value", " "))))
      .groupBy("words")
      .count
      .orderBy($"count".desc)
      .show()


    // ----------------- word count ------------------------

    // Plusieurs exemples de syntaxes, de la plus lourde à la plus légère.
    // Préférez la deuxième syntaxe: les types assurent la consistence des données, et les noms de variables permettent
    // le lire le code plus facilement.

    val df_wordCount = sc.textFile("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md")
      .flatMap { case (line: String) => line.split(" ") }
      .map { case (word: String) => (word, 1) }
      .reduceByKey { case (i: Int, j: Int) => i + j }
      .toDF("word", "count")

    df_wordCount.orderBy($"count".desc).show()


    val df_wordCount_light = sc.textFile("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md")
      .flatMap { line: String => line.split(" ") }
      .map { word: String => (word, 1) }
      .reduceByKey { (i: Int, j: Int) => i + j }
      .toDF("word", "count")

    df_wordCount_light.orderBy($"count".desc).show()


    val df_wordCount_lighter = sc.textFile("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md") // output RDD of lines : RDD[String]
      .flatMap(line => line.split(" ")) // output RDD of words : RDD[String]
      .map(word => (word, 1)) // output RDD of (Key, Value) pairs : RDD[(String, Int)]
      .reduceByKey((i, j) => i + j) // output RDD of (Key, ValueTot) pairs, where ValueTot is the sum of all value associated with the Key
      .toDF("word", "count") // transform RDD to DataFrame with columns names "word" and "count"

    df_wordCount_lighter.orderBy($"count".desc).show()


    val df_wordCount_lightest = sc.textFile("/Users/maxime/spark-2.2.0-bin-hadoop2.7/README.md")
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
      .toDF("word", "count")

    df_wordCount_lightest.orderBy($"count".desc).show()
  }
}
