import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt 
from pyecharts import Map

data = pd.read_csv("../../preprocess/data.csv")
plt.rc("font", family="SimHei", size="12")

sns.boxplot(x="big_category", y="salary", data=data, width=0.5, linewidth=1.0, palette="Set3") 
plt.show()

temp = data.loc[data['big_category'] == '数据']
sns.boxplot(x="small_category", y="salary", data=temp, width=0.5, linewidth=1.0, palette="Set3") 
plt.show()

sns.boxplot(x="city", y="salary", data=data, width=0.5, linewidth=1.0, palette="Set3") 
plt.show()

temp = data.loc[data['city'] == '上海']
temp = temp.loc[temp['work_area'] != "None"]
sns.boxplot(x="work_area", y="salary", data=temp, width=0.5, linewidth=1.0, palette="Set3") 
plt.show()

map2 = Map("上海地图", '上海', width=1200, height=600)
city = ['浦东新区', '徐汇区', '闵行区', '杨浦区', '静安区', '长宁区', '黄浦区', '普陀区', '嘉定区', '宝山区', '虹口区', '松江区', '青浦区', '奉贤区', '金山区', '崇明区']
values2 = [5986, 2355, 1557, 842, 756, 744, 672, 668, 411, 410, 407, 315, 259, 92, 25, 4]
map2.add('上海', city, values2, visual_range=[4, 5986], maptype='上海', is_visualmap=True, visual_text_color='#001')
 
map2.render(path="上海.html")

temp = data.loc[data['city'] == '上海']
temp = temp.loc[temp['work_area'] != "None"]
sns.boxplot(x="work_area", y="salary", data=temp, width=0.5, linewidth=1.0, palette="Set3") 
plt.show()


data=data.corr()
plt.subplots(figsize=(9,9),dpi=1080,facecolor='w')# 设置画布大小，分辨率，和底色

sns.heatmap(data,annot=True, vmax=1, square=True, cmap="Blues", fmt='.2g')#annot为热力图上显示数据；fmt='.2g'为数据保留两位有效数字,square呈现正方形，vmax最大值为1
plt.show()