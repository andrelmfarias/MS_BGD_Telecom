package com.sparkProject

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature._


object Trainer {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAll(Map(
      "spark.scheduler.mode" -> "FIFO",
      "spark.speculation" -> "false",
      "spark.reducer.maxSizeInFlight" -> "48m",
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer",
      "spark.kryoserializer.buffer.max" -> "1g",
      "spark.shuffle.file.buffer" -> "32k",
      "spark.default.parallelism" -> "12",
      "spark.sql.shuffle.partitions" -> "12",
      "spark.driver.maxResultSize" -> "2g"
    ))

    val spark = SparkSession
      .builder
      .config(conf)
      .appName("TP_spark")
      .getOrCreate()


    /*******************************************************************************
      *
      *       TP 3
      *
      *       - lire le fichier sauvegarder précédemment
      *       - construire les Stages du pipeline, puis les assembler
      *       - trouver les meilleurs hyperparamètres pour l'entraînement du pipeline avec une grid-search
      *       - Sauvegarder le pipeline entraîné
      *
      *       if problems with unimported modules => sbt plugins update
      *
      ********************************************************************************/

    // 2.a. 1st stage: Building tokenizer

    val tokenizer = new RegexTokenizer()
      .setPattern("\\W+")
      .setGaps(true)
      .setInputCol("text")
      .setOutputCol("tokens")


    // 2.b. 2nd stage: Stop words

    val remover = new StopWordsRemover()
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("words_removed")

    // 2.c. 3rd stage: CountVectorizer

    val vectorizer = new CountVectorizer()
      .setInputCol(remover.getOutputCol)
      .setOutputCol("text_vectorized")
    // .setMinTf()
    // .setVocabSize()

    // 2.d. 4th stage: IDF

    val idf = new IDF()
      .setInputCol(vectorizer.getOutputCol)
      .setOutputCol("tfidf")
    // .setMinDocFreq()

    // 3.e. 5th stage: convert category variable country2 to indexes

    val indexer_country = new StringIndexer()
      .setInputCol("country2")
      .setOutputCol("country_indexed")

    // 3.f. 6th stage: convert category variable currency2 to indexes

    val indexer_currency = new StringIndexer()
      .setInputCol("currency2")
      .setOutputCol("currency_indexed")

    // 3.g. 6th stage: convert category variable country2 to to one-hot-encoder

    val encoder_country = new OneHotEncoder()
      .setInputCol(indexer_country.getOutputCol)
      .setOutputCol("country_indexed")

    // 3.g. 7th stage: convert category variable currency2 to to one-hot-encoder

    val encoder_country = new OneHotEncoder()
      .setInputCol(indexer_currency.getOutputCol)
      .setOutputCol("currency_indexed")

    // 4.h. 8th stage: assembly columns of features into one column to use ML algorithms

    val assembler = new VectorAssembler()
      .setInputCols(Array("tfidf", "days_campaign", "hours_prepa", "goal", "country_indexed", "currency_indexed"))
      .setOutputCol("features")


    println("hello world ! from Trainer")

  }
}
