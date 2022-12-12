package org.example

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.immutable.TreeSet
import scala.collection.mutable._

object App {
  def main(args: Array[String]): Unit = {

    //1.初始化
    val conf=new SparkConf().setAppName("SimpleKnn")
    val sc=new SparkContext(conf)
    val K=args(0).toInt
    val t1 = System.nanoTime
    val path = "hdfs://master:9000/knn/input/"

    //2.读取数据,封装数据
    val traindata: RDD[LabelPoint] = sc.textFile(path + "train_random1.csv")
        .map( line => {
        val arr = line.split(",")
        LabelPoint(arr.last, arr.init.map(_.toDouble))
      })

    val testdata: RDD[LabelPoint] = sc.textFile(path + "test_random1.csv")
      .map( line => {
      val arr = line.split(",")
      LabelPoint(arr.last, arr.init.map(_.toDouble))
    })

    val t2 = System.nanoTime
    println("init time: " + (t2 - t1) / 1e9d)

    //3.过滤出样本数据和测试数据
    val testdata_noLabel=testdata.map(_.point).collect()
    val testdata_Label=testdata.map(x=>(x.point.head, x.label)).collect()

    //4.将testData封装到广播变量做一个优化
    val bc_testData = sc.broadcast(testdata_noLabel)

    //5.求每一条测试数据与样本数据的距离----使用mapPartitions应对大量数据集进行优化
    val distance: RDD[(Double, (Double, String))] = traindata.mapPartitions(iter => {
      val bc_points = bc_testData.value
      iter.flatMap(x => bc_points.map(point2 => (point2.head, (getDistance(point2.drop(1), x.point), x.label))))
    })

    //6.求距离最小的k个点,使用aggregateByKey---先分局内聚合,再全局聚合
    val labelOut = distance.aggregateByKey(TreeSet[(Double, String)]())(
      (splitSet: TreeSet[(Double, String)], elem: (Double, String)) => {
        val newSet = splitSet + elem //TreeSet默认是有序的(升序)
        newSet.take(K)
      },
      (splitSet1: TreeSet[(Double, String)], splitSet2: TreeSet[(Double, String)]) => {
        (splitSet1 ++ splitSet2).take(K)
      }
      )
      //7.取出距离最小的k个点中出现次数最多的label---即为样本数据的label
      .map(x => {
        (
          x._1,
          x._2
            .toArray
            .map(_._2)
            .groupBy(y => y)
            .map(z=>(z._1,z._2.length))
            .toList
            .sortBy(_._2)
            .reverse
            .take(1)
            .map(_._1)
            .head
        )
      })
      .sortBy(_._1)
    labelOut.saveAsTextFile("hdfs://master:9000/knn/output/part-r-00000")
    val labelOutList = labelOut.collect()

    //8.计算准确率与运算时间
    println("K: " + K)
    var i = 0
    var correct = 0.0
    while (i < testdata_Label.length){
      if (testdata_Label(i)._2==labelOutList(i)._2){
        correct += 1.0
      }
      i = i+1
    }
    val accuracy = correct / testdata_Label.length
    println("accuracy: " + accuracy)

    val t3 = System.nanoTime
    println("init time: " + (t2 - t1) / 1e9d)
    println("exec time: " + (t3 - t2) / 1e9d)
    println("total time: " + (t3 - t1) / 1e9d)
    sc.stop()
  }

  case class LabelPoint(label:String,point:Array[Double])

  import scala.math._

  def getDistance(x:Array[Double],y:Array[Double]):Double={
    sqrt(x.zip(y).map(z=>pow(z._1-z._2,2)).sum)
  }
}
