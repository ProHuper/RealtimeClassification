# encoding:utf-8

import sys
import jieba
import jieba.analyse
from pyspark import SparkConf
from pyspark import SparkContext
from pyspark.sql import HiveContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from pyspark.mllib.tree import RandomForestModel
from pyspark.ml.feature import Tokenizer, HashingTF, IDF
from pyspark.mllib.classification import LogisticRegressionModel, NaiveBayesModel


def split_train(line):
    info = line.split(',')
    return info[0], info[1]


def make_stop_list(doc_path):
    doc_info = []
    with open(doc_path, 'r') as stop_file:
        for eachWord in stop_file:
            doc_info.append(eachWord.decode('utf8').strip('\n'))
    doc_info.append('\n')
    return doc_info


def token_map(line):
    seg_list = jieba.cut(line, cut_all=True)
    seg_filter = [word for word in list(seg_list) if word not in stop_list and word != '']
    return seg_filter


def divide_and_token(line):
    infolist = line[1].split('\1')
    token_list = token_map(infolist[1])
    return infolist[0], token_list


def predict_by_rdd(time, rdd):
    length = rdd.count()
    if length > 0:
        my_print("-------------------------------------------")
        my_print("Time: %s" % time)
        my_print("-------------------------------------------")
        rdd = rdd.map(divide_and_token)
        new_hive_info = hiveCtx.createDataFrame(rdd, ['url', 'words'])
        tf_data = hashingTF.transform(new_hive_info)
        final_input_data = idf.transform(tf_data)
        prediction = final_input_data.select('url', 'features').collect()
        result = map(lambda x: (x[0], label_dict[model.predict(x[1].toArray())]), prediction)
        for item in result:
            _my_print(item[0])
            _my_print(' ---------------> ')
            _my_print(item[1])
            _my_print('\n')


def my_print(info):
    sys.stdout.write(info + '\n')
    sys.stdout.flush()


def _my_print(info):
    sys.stdout.write(info)
    sys.stdout.flush()


if __name__ == '__main__':

    model_name = sys.argv[1]
    label_dict = {0: '汽车', 1: '财经', 2: '科技', 3: '健康', 4: '体育',
                  5: '旅游', 6: '教育', 7: '招聘', 8: '文化', 9: '军事'}

    model_dict = {'NaiveBayes': 'E:/finalwork/model/NB_model',
                  'LogisticRegression': 'E:/finalwork/model/LR_model',
                  'RandomForest': 'E:/finalwork/model/RF_model'}

    stop_path = 'E:/finalwork/other_file/stopwords.txt'
    train_path = 'E:/finalwork/train_file/'
    model_path = model_dict[model_name]

    my_print('staring......')
    # sconf.set('spark.streaming.blockInterval', '100')
    sconf = SparkConf()
    sconf.set('spark.cores.max', 4)
    sc = SparkContext(appName='TextClassification', conf=sconf)

    # sc = SparkContext("local[2]", 'TextClassification')
    sc.setLogLevel("ERROR")
    hiveCtx = HiveContext(sc)
    ssc = StreamingContext(sc, 2)
    my_print('Spark环境加载完成......')

    stop_list = set(make_stop_list(stop_path))
    input_rdd = sc.textFile(train_path).map(split_train)
    train_hive_info = hiveCtx.createDataFrame(input_rdd, ['label', 'text'])
    split = Tokenizer(inputCol="text", outputCol="words")
    wordsData = split.transform(train_hive_info)

    # 增加TF特征列
    hashingTF = HashingTF(inputCol="words", outputCol="rawFeatures", numFeatures=2 ** 10)
    TF_data = hashingTF.transform(wordsData)

    # 增加IDF特征列
    idf = IDF(inputCol="rawFeatures", outputCol="features").fit(TF_data)

    if model_name == 'NaiveBayes':
        model = NaiveBayesModel.load(sc, model_path)
    elif model_name == 'LogisticRegression':
        model = LogisticRegressionModel.load(sc, model_path)
    else:
        model = RandomForestModel.load(sc, model_path)

    my_print('模型加载完成.......')

    # 1.以D_stream方式创建kafka数据源
    numStreams = 3
    kafkaStreams = [KafkaUtils.createStream(ssc, "localhost:2181", b"streaming_group", {b"streaming": 1}) for _ in range(numStreams)]
    kafkaStreams = ssc.union(*kafkaStreams)
    my_print('Kafka数据源链接成功......')

    # 2.以Direct方式创建kafka数据源
    # brokers = "localhost:9092"
    # kafkaStreams = KafkaUtils.createDirectStream(ssc, ['streaming'], kafkaParams={"metadata.broker.list": brokers})

    # 3.从文件系统加载数据源
    # ins = ssc.textFileStream('file:///C:/Users/Huper/Desktop/files/stream')

    # 分RDD处理
    kafkaStreams.foreachRDD(predict_by_rdd)
    ssc.start()
    ssc.awaitTermination()


