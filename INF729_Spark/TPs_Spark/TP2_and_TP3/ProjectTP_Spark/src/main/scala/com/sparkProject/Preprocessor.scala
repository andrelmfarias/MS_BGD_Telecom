package com.sparkProject


import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.IntegerType
import org.dmg.pmml.False
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Preprocessor {

  def main(args: Array[String]): Unit = {

    // Des réglages optionels du job spark. Les réglages par défaut fonctionnent très bien pour ce TP
    // on vous donne un exemple de setting quand même
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

    // Initialisation de la SparkSession qui est le point d'entrée vers Spark SQL (donne accès aux dataframes, aux RDD,
    // création de tables temporaires, etc et donc aux mécanismes de distribution des calculs.)
    val spark = SparkSession
      .builder
      .config(conf)
      .appName("TP_spark")
      .getOrCreate()

    import spark.implicits._


    /*******************************************************************************
      *
      *       TP 2
      *
      *       - Charger un fichier csv dans un dataFrame
      *       - Pre-processing: cleaning, filters, feature engineering => filter, select, drop, na.fill, join, udf, distinct, count, describe, collect
      *       - Sauver le dataframe au format parquet
      *
      *       if problems with unimported modules => sbt plugins update
      *
      ********************************************************************************/

    val df = spark.read
        .format("csv")
        .option("header", "true")
        //.option("mode","DROPMALFORMED")
        .load("train_clean.csv")

    println(s"Number of rows: ${df.count()}")
    println(s"Number of columns: ${df.columns.size}")
    println("\n")

    df.show()
    println("\n")

    val df_int = df
      .withColumn("goal",df("goal").cast(IntegerType))
      .withColumn("backers_count",df("backers_count").cast(IntegerType))
      .withColumn("deadline",df("deadline").cast(IntegerType))
      .withColumn("state_changed_at",df("state_changed_at").cast(IntegerType))
      .withColumn("created_at",df("created_at").cast(IntegerType))
      .withColumn("launched_at",df("launched_at").cast(IntegerType))
      .withColumn("final_status",df("final_status").cast(IntegerType))

    df_int.printSchema()
    println("\n")


    df_int.select("goal","backers_count","deadline","state_changed_at","launched_at","final_status")
        .describe().show()


    val df2 = df_int.drop("disable_communication","backers_count","state_changed_at")

    df2.show()
    println("\n")

    // df2.groupBy("country").count.orderBy($"count".desc).show(50)

    // Check currency values where country === false
    df2.filter($"country"==="False").groupBy("currency").count.orderBy($"count".desc).show(50)


    // Defining functions for UDFs

    def Udf_country = udf((country: String, currency: String) => {
      if(country != null && country == "False") {
         currency;
      }
      else if(country != null && country.length != 2){
         null;
      }
      else {
        country;
      }
    })

    def Udf_currency = udf((currency: String) => {
      if(currency != null && currency.length != 3) null;
      else currency;
    })
    //


    val df3 = df2
        .withColumn("country2",Udf_country(df2("country"),df2("currency")))
        .withColumn("currency2",Udf_currency($"currency"))

    // df3.select("country2","currency2").describe().show()

    // println("Country \ndf2:")
    // df2.groupBy("country","currency").count.orderBy($"count".desc).show()
    // println("Country \ndf3:")
    // df3.groupBy("country2","currency2").count.orderBy($"count".desc).show()

    // df3.groupBy("final_status").count.orderBy($"count".desc).show()

    val df4 = df3.filter($"final_status" === 1 || $"final_status" === 0)

    df4.groupBy("final_status").count.orderBy($"count".desc).show()

    val df5 = df4
        .withColumn("days_campaign",datediff(from_unixtime(df4("deadline")),from_unixtime(df4("launched_at"))))
        .withColumn("hours_prepa",round((df4("launched_at")-df4("created_at"))/3600D,3))
        .drop("launched_at","created_at","deadline")
        .withColumn("text",concat(df4("name"),lit(" "),df4("desc"),lit(" "),df4("keywords")))
    // or .withColumn("text",concat_ws(" ",df4("name"),df4("desc"),df4("keywords")))

    // df5.select("deadline","launched_at","created_at","days_campaign","hours_prepa").show(20)

    // df5.printSchema()

    df5.select("text","name","desc","keywords").show(5)

    /////////////////////// Udf function to deal with null values ////////////////
    // def Udf_nullNum = udf((num: Int) => {
    //   if(num == null) -1;
    //  else num;
    // })
    //
    // def Udf_nullStr = udf((str: String) => {
    //   if(str == null) "unknown";
    //   else str;
    // })
    //
    // val df6 = df5
    //     .withColumn("days_campaign",Udf_nullNum($"days_campaign"))
    //     .withColumn("hours_prepa",Udf_nullNum($"hours_prepa"))
    //     .withColumn("goal",Udf_nullNum($"goal"))
    //     .withColumn("country2",Udf_nullStr($"country2"))
    //     .withColumn("currency2",Udf_nullStr($"currency2"))
    /////////////////////////////////////////////////////////////////////////////

    val df6 = df5.na.fill(Map(
      "days_campaign" -> -1,
      "hours_prepa" -> -1,
      "goal" -> -1,
      "country2" -> "unknown",
      "currency2" -> "unknown"
    ))

    // df6.printSchema()

    // df6.select("days_campaign","hours_prepa","goal").describe().show()
    // df6.groupBy("country2","currency2").count.show()

    df6.filter($"hours_prepa" < 0).show()

    // deleting rows with "hours_prepa" < 0
    val df7 = df6.filter($"hours_prepa" >= 0)

    df7.select("days_campaign","hours_prepa","goal").describe().show()
    df7.groupBy("country2","currency2").count.show()


    df7.write.parquet("Kick_Starter_cleanedDF")

    println("hello world ! from Preprocessor")

  }

}
