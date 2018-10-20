import pandas as pd
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.naive_bayes import BernoulliNB
from sklearn.model_selection import train_test_split
from sklearn import metrics
import pickle

dataset=pd.read_csv('tweets.csv')
size=len(dataset)+4
train=dataset[:int(size*0.8)]
test=dataset[int(size*0.8):]

vectorizer = HashingVectorizer(stop_words='english', alternate_sign=False,n_features=2**16)
X_train = vectorizer.transform(train['text'])
X_test =vectorizer.transform(test['text'])

y_train=[]
y_test=[]
for item in train['choose_one']:
    if item=="Relevant":
        y_train.append(1)
        # print(1)
    else:
        y_train.append(0)
for item in test['choose_one']:
    if item=="Relevant":
        y_test.append(1)
        # print(1)
    else:
        y_test.append(0)

clf=BernoulliNB(alpha=.01)

clf.fit(X_train,y_train)
pred = clf.predict(X_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)
file_h=open('diff.pkl','wb')
pickle.dump(clf,file_h)
