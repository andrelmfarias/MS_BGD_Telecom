package com.sparkProject

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature._
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator


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

    // 1. Loading dataset

    val df = spark.read.load("/Users/andre.farias/Desktop/MSBigData_GitHub/INF729_Spark/TPs_Spark/TP2_and_TP3/ProjectTP_Spark/prepared_trainingset")

    println("hello world ! from Trainer")

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


    // 2.d. 4th stage: IDF

    val idf = new IDF()
      .setInputCol(vectorizer.getOutputCol)
      .setOutputCol("tfidf")
    // .setMinDocFreq()

    // 3.e. 5th stage: convert category variable country2 to indexes

    val indexer_country = new StringIndexer()
      .setInputCol("country2")
      .setOutputCol("country_indexed")
      .setHandleInvalid("skip")

    // 3.f. 6th stage: convert category variable currency2 to indexes

    val indexer_currency = new StringIndexer()
      .setInputCol("currency2")
      .setOutputCol("currency_indexed")
      .setHandleInvalid("skip")

    // 3.g. 6th stage: convert category variable country2 to to one-hot-encoder

    val encoder_country = new OneHotEncoder()
      .setInputCol(indexer_country.getOutputCol)
      .setOutputCol("country_indexed_one_hot")

    // 3.g. 7th stage: convert category variable currency2 to to one-hot-encoder

    val encoder_currency = new OneHotEncoder()
      .setInputCol(indexer_currency.getOutputCol)
      .setOutputCol("currency_indexed_one_hot")

    // 4.h. 8th stage: assembly columns of features into one column to use ML algorithms

    val assembler = new VectorAssembler()
      .setInputCols(Array("tfidf", "days_campaign", "hours_prepa", "goal", "country_indexed_one_hot", "currency_indexed_one_hot"))
      .setOutputCol("features")

    // 4.i. 9th stage: classification model -- logistic regression

    val lr = new LogisticRegression()
      .setElasticNetParam(0.0)
      .setFitIntercept(true)
      .setFeaturesCol("features")
      .setLabelCol("final_status")
      .setStandardization(true)
      .setPredictionCol("predictions")
      .setRawPredictionCol("raw_predictions")
      .setThresholds(Array(0.7, 0.3))
      .setTol(1.0e-6)
      .setMaxIter(300)

    // 4.j. 10th stage: create pipeline

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, remover, vectorizer, idf, indexer_country, indexer_currency,
                       encoder_country, encoder_currency, assembler, lr))

    // 5.k. Split train and test set

    val splits = df.randomSplit(Array(0.9, 0.1), 0)
    val training = splits(0)
    val test = splits(1)


    // 5.i. Create grid of parameters

    val paramGrid = new ParamGridBuilder()
      .addGrid(lr.regParam, Array(10e-8, 10e-6, 10e-4, 10e-2))
      .addGrid(vectorizer.minDF, Array(55.0,75.0,95.0))
      .build()

    // Train-validation split using 70% as training and F1-score as evaluator

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("final_status")
      .setPredictionCol("predictions")
      .setMetricName("f1")

    val trainValidationSplit = new TrainValidationSplit()
      .setEstimator(pipeline)
      .setEvaluator(evaluator)
      .setEstimatorParamMaps(paramGrid)
      .setTrainRatio(0.7)

    // 5.m Train model and apply on test set

    val model = trainValidationSplit.fit(training)

    val df_WithPredictions = model.transform(test)
      .select("features", "final_status", "predictions")

    val f1_score = evaluator.evaluate(df_WithPredictions)

    println("f1-score on test set: " + f1_score)

    df_WithPredictions.groupBy("final_status", "predictions").count.show()



  }
}
