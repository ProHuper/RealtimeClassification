# encoding:utf-8

import sys
from pyspark import SparkConf
from pyspark import SparkContext
from pyspark.mllib.classification import LogisticRegressionWithLBFGS, NaiveBayes
from pyspark.mllib.regression import LabeledPoint
from pyspark.mllib.tree import RandomForest
from pyspark.sql import HiveContext
from pyspark.streaming import StreamingContext
from pyspark.ml.feature import Tokenizer, HashingTF, IDF


def split_train(line):
    info = line.split(',')
    return info[0], info[1]


def my_print(info):
    sys.stdout.write(info + '\n')
    sys.stdout.flush()


if __name__ == '__main__':

    model_name = sys.argv[1]
    model_dict = {'NaiveBayes': 'E:/finalwork/model/NB_model',
                  'LogisticRegression': 'E:/finalwork/model/LR_model',
                  'RandomForest': 'E:/finalwork/model/RF_model'}

    stop_path = 'E:/finalwork/other_file/stopwords.txt'
    train_path = 'E:/finalwork/train_file/'
    model_path = model_dict[model_name]

    my_print('staring......')
    sconf = SparkConf()
    sconf.set('spark.cores.max', 4)
    sc = SparkContext(appName='TextClassification', conf=sconf)

    sc.setLogLevel("ERROR")
    hiveCtx = HiveContext(sc)
    ssc = StreamingContext(sc, 2)
    my_print('Spark环境加载完成......')

    input_rdd = sc.textFile(train_path).map(split_train)
    train_hive_info = hiveCtx.createDataFrame(input_rdd, ['label', 'text'])
    split = Tokenizer(inputCol="text", outputCol="words")
    wordsData = split.transform(train_hive_info)
    my_print('分词完成.......')

    # 增加TF特征列
    hashingTF = HashingTF(inputCol="words", outputCol="rawFeatures", numFeatures=2 ** 10)
    TF_data = hashingTF.transform(wordsData)
    my_print('TF特征构造完成.......')

    # 增加IDF特征列
    idf = IDF(inputCol="rawFeatures", outputCol="features").fit(TF_data)
    final_input_data = idf.transform(TF_data)
    my_print('IDF特征构造完成.......')

    train_rdd = final_input_data.select("label", "features") \
        .rdd.map(lambda (label, features): (LabeledPoint(label, features.toArray())))

    if model_name == 'LogisticRegression':
        model = LogisticRegressionWithLBFGS.train(train_rdd, numClasses=10)
        model.save(sc, model_path)

    elif model_name == 'NaiveBayes':
        model = NaiveBayes.train(train_rdd)
        model.save(sc, model_path)

    else:
        model = RandomForest.trainClassifier(train_rdd, 10, {}, 10, seed=42)
        model.save(sc, model_path)
    my_print('模型训练完成.......')