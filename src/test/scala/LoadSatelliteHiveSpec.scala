import diamond.load.HiveDataLoader

/**
  * Created by markmo on 23/01/2016.
  */
class LoadSatelliteHiveSpec extends UnitSpec {

  val BASE_URI = "hdfs://localhost:9000"

  val hiveLoader = new HiveDataLoader

  "Customers" should "load customers into a satellite table using Hive" in {
    val demo = sqlContext.read.load("hdfs://localhost:9000/base/Customer_Demographics.parquet")

    hiveLoader.loadSatellite(demo,
      isDelta = false,
      tableName = "customer_demo",
      idField = "cust_id",
      idType = "id1",
      partitionKeys = None,
      newNames = Map(
        "age25to29" -> "age_25_29",
        "age30to34" -> "age_30_34"
      )
    )

    val customers = sqlContext.sql(
      """
        |select *
        |from customer_demo
      """.stripMargin)

    customers.count() should be (20000)
  }

  it should "load deltas into a satellite table using Hive" in {
    val delta = sqlContext.read.load("hdfs://localhost:9000/base/Customer_Demographics_Delta.parquet")

    hiveLoader.loadSatellite(delta,
      isDelta = true,
      tableName = "customer_demo",
      idField = "cust_id",
      idType = "id1",
      partitionKeys = None,
      newNames = Map(
        "age25to29" -> "age_25_29",
        "age30to34" -> "age_30_34"
      )
    )

    val customers = sqlContext.sql(
      """
        |select *
        |from customer_demo
      """.stripMargin)

    customers.count() should be (20010)
  }

  it should "perform change data capture using Hive" in {
    val updates = sqlContext.read.load("hdfs://localhost:9000/base/Customer_Demographics_Delta_Updates.parquet")

    hiveLoader.loadSatellite(updates,
      isDelta = true,
      tableName = "customer_demo",
      idField = "cust_id",
      idType = "id1",
      partitionKeys = None,
      newNames = Map(
        "age25to29" -> "age_25_29",
        "age30to34" -> "age_30_34"
      )
    )

    val customers = sqlContext.sql(
      """
        |select *
        |from customer_demo
      """.stripMargin)

    customers.count() should be (20020)
  }

}